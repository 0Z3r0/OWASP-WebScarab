/*
 * SiteTreeModelAdapter.java
 *
 * Created on August 27, 2004, 4:19 AM
 */

package org.owasp.webscarab.ui.swing;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.owasp.webscarab.util.swing.AbstractTreeModel;

import org.owasp.webscarab.model.HttpUrl;
import org.owasp.webscarab.model.SiteModel;
import org.owasp.webscarab.model.SiteModelAdapter;

import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 *
 * @author  knoppix
 */
public class SiteTreeModelAdapter extends AbstractTreeModel {
    
    protected SiteModel _model;
    private Listener _listener = new Listener();
    
    private Set _filtered = new HashSet();
    private Set _implicit = new HashSet();
    
    protected Logger _logger = Logger.getLogger(getClass().getName());
    
    private Object _root = new String("RooT");
    
    public SiteTreeModelAdapter() {
        this(null);
    }
    
    public SiteTreeModelAdapter(SiteModel model) {
        setModel(model);
    }
    
    public void setModel(SiteModel model) {
        if (_model != null) {
            _model.removeSiteModelListener(_listener);
        }
        _filtered.clear();
        _implicit.clear();
        _model = model;
        if (_model != null) {
            try {
                _model.readLock().acquire();
                try {
                    recurseTree(null);
                } finally {
                    _model.readLock().release();
                }
            } catch (InterruptedException ie) {
                _logger.severe("Interrupted! " + ie);
            }
            _model.addSiteModelListener(_listener);
        }
        fireStructureChanged();
    }
    
    public Object getRoot() {
        return _root;
    }
    
    public Object getChild(Object parent, int index) {
        if (_model == null) throw new NullPointerException("Getting a child when the model is null!");
        if (parent == getRoot()) parent = null;
        int pos = -1;
        try {
            _model.readLock().acquire();
            try {
                int count = _model.getQueryCount((HttpUrl) parent);
                for (int i=0; i<count; i++) {
                    HttpUrl sibling = _model.getQueryAt((HttpUrl) parent, i);
                    if (! _filtered.contains(sibling) || _implicit.contains(sibling)) {
                        pos++;
                        if (pos == index) return sibling;
                    }
                }
                count = _model.getChildUrlCount((HttpUrl) parent);
                for (int i=0; i<count; i++) {
                    HttpUrl sibling = _model.getChildUrlAt((HttpUrl) parent, i);
                    if (! _filtered.contains(sibling) || _implicit.contains(sibling)) {
                        pos++;
                        if (pos == index) return sibling;
                    }
                }
                _logger.warning("Did not find an unfiltered child of " + parent + " at " + index);
                return null;
            } finally {
                _model.readLock().release();
            }
        } catch (InterruptedException ie) {
            _logger.severe("Interrupted! " + ie);
            return null;
        }
    }
    
    public int getChildCount(Object parent) {
        if (_model == null) return 0;
        if (parent == getRoot()) parent = null;
        int pos = 0;
        try {
            _model.readLock().acquire();
            try {
                int count = _model.getQueryCount((HttpUrl) parent);
                for (int i=0; i<count; i++) {
                    HttpUrl sibling = _model.getQueryAt((HttpUrl) parent, i);
                    if (! _filtered.contains(sibling) || _implicit.contains(sibling)) {
                        pos++;
                    }
                }
                count = _model.getChildUrlCount((HttpUrl) parent);
                for (int i=0; i<count; i++) {
                    HttpUrl sibling = _model.getChildUrlAt((HttpUrl) parent, i);
                    if (! _filtered.contains(sibling) || _implicit.contains(sibling)) {
                        pos++;
                    }
                }
                return pos;
            } finally {
                _model.readLock().release();
            }
        } catch (InterruptedException ie) {
            _logger.severe("Interrupted! " + ie);
            return -1;
        }
    }
    
    public boolean isLeaf(Object node) {
        if (node == getRoot()) return false;
        HttpUrl url = (HttpUrl) node;
        if (url.getParameters() != null) return true;
        if (url.getPath().endsWith("/")) return false;
        return getChildCount(url) == 0;
    }
    
    public boolean isImplicit(HttpUrl url) {
        return _implicit.contains(url);
    }
    
    private boolean isVisible(HttpUrl url) {
        return isImplicit(url) || ! _filtered.contains(url);
    }
    
    /**
     * This is called on the AWT-Thread. Override this method to filter out part
     * of the tree, and make it invisible.
     * @return false if the url should be visible in the tree
     */
    protected boolean isFiltered(HttpUrl url) {
        return false;
    }
    
    private void addedUrl(HttpUrl url) {
        if (! isFiltered(url)) {
            grow(url);
        } else {
            _filtered.add(url);
        }
    }
    
    private void changedUrl(HttpUrl url, String property) {
        if (isFiltered(url)) { // it is now filtered
            if (isVisible(url)) { // we could previously see it
                if (getChildCount(url)>0) { // it has children
                    _filtered.add(url);
                    _implicit.add(url);
                    HttpUrl parent = url.getParentUrl();
                    int index = getIndexOfChild(parent, url);
                    fireChildChanged(urlTreePath(parent), index, url);
                } else { // it has no children, hide it and any implicit parents
                    _filtered.add(url);
                    prune(url);
                }
            } // else there is nothing to do to an already invisible node
        } else { // it is now not filtered
            if (! isVisible(url)) { // it was previously hidden
                _filtered.remove(url);
                grow(url);
            } else {
                HttpUrl parent = url.getParentUrl();
                int index = getIndexOfChild(parent, url);
                fireChildChanged(urlTreePath(parent), index, url);
            }
        }
    }
    
    private void removedUrl(HttpUrl url, int position) { // only leaves are ever removed
        // we ignore position, as we may have filtered out parts of the tree
        if (isVisible(url)) {
            prune(url);
        } else {
            _filtered.remove(url);
        }
    }
    
    /* adds url, and marks any previously filtered intermediate nodes as implicit
     * fires only a single event for the topmost node that becomes visible
     */
    private void grow(HttpUrl url) {
        HttpUrl[] path = url.getUrlHierarchy();
        // FIXME this is a cheat - we only fire on the highest node added, not all the nodes
        do { // step up the tree looking for the first visible node to add a new child to
            HttpUrl parent = url.getParentUrl();
            if (! isVisible(parent)) {
                _implicit.add(parent);
            } else {
                fireChildAdded(urlTreePath(parent), getIndexOfChild(parent, url), url);
                break;
            }
            url = parent;
        } while (url != null);
    }
    
    /* removes url and any implicit parents. Fires only a single event for the
     * topmost url removed
     *
     */
    private void prune(HttpUrl url) {
        int count;
        boolean prune;
        do {
            HttpUrl parent = url.getParentUrl();
            int pos = 0;
            count = getChildCount(parent);
            for (int i=0; i<count; i++) {
                HttpUrl sibling = (HttpUrl) getChild(parent, i);
                if (url.compareTo(sibling)<0) {
                    break;
                } else {
                    pos++;
                }
            }
            fireChildRemoved(urlTreePath(parent), pos, url);
            prune = (count == 0) && isImplicit(parent);
            if (prune) {
                _implicit.remove(parent);
            }
            url = parent;
        } while (url != null && prune);
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        // we do not support editing
    }
    
    protected TreePath urlTreePath(HttpUrl url) {
        Object root = getRoot();
        if (url == null || url == root) {
            return new TreePath(root);
        } else {
            Object[] urlPath = url.getUrlHierarchy();
            Object[] path = new Object[urlPath.length+1];
            path[0] = root;
            System.arraycopy(urlPath, 0, path, 1, urlPath.length);
            return new TreePath(path);
        }
    }
    
    private void recurseTree(HttpUrl parent) {
        int count = _model.getQueryCount(parent);
        for (int i=0; i<count; i++) {
            HttpUrl url = _model.getQueryAt(parent, i);
            if (isFiltered(url)) {
                _filtered.add(url);
            } else {
                grow(url);
            }
        }
        count = _model.getChildUrlCount(parent);
        for (int i=0; i<count; i++) {
            HttpUrl url = _model.getChildUrlAt(parent, i);
            if (isFiltered(url)) {
                _filtered.add(url);
            } else {
                grow(url);
            }
            recurseTree(url);
        }
    }
    
    private class Listener extends SiteModelAdapter {
        
        public void urlAdded(final HttpUrl url) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        addedUrl(url);
                    }
                });
            } catch (Exception e) {
                _logger.warning("Exception adding " + url + " " + e);
                e.getCause().printStackTrace();
                // System.exit(1);
            }
        }
        
        public void urlChanged(final HttpUrl url, final String property) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        changedUrl(url, property);
                    }
                });
            } catch (Exception e) {
                _logger.warning("Exception changing " + url + " property " + property + " " + e);
                e.getCause().printStackTrace();
                // System.exit(1);
            }
        }
        
        public void urlRemoved(final HttpUrl url, final int position) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        removedUrl(url, position);
                    }
                });
            } catch (Exception e) {
                _logger.warning("Exception removing " + url + " " + e);
                e.getCause().printStackTrace();
                // System.exit(1);
            }
        }
        
    }
}
