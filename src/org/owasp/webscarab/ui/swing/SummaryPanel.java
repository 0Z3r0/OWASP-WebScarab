/*
 * SummaryPanel.java
 *
 * Created on December 16, 2003, 10:35 AM
 */

package org.owasp.webscarab.ui.swing;

import org.owasp.webscarab.model.SiteModel;
import org.owasp.webscarab.model.SiteModelAdapter;
import org.owasp.webscarab.model.HttpUrl;
import org.owasp.webscarab.model.ConversationID;

import org.owasp.webscarab.util.swing.JTreeTable;
import org.owasp.webscarab.util.swing.treetable.TreeTableModel;
import org.owasp.webscarab.util.swing.TableSorter;
import org.owasp.webscarab.util.swing.ColumnDataModel;

import javax.swing.JTree;
import javax.swing.table.TableModel;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.logging.Logger;

/**
 *
 * @author  rdawes
 */
public class SummaryPanel extends javax.swing.JPanel {
    
    private SiteModel _model;
    private JTreeTable _urlTreeTable;
    private ArrayList _urlActions = new ArrayList();
    private HttpUrl _treeURL = null;
    private ConversationTableModel _conversationTableModel;
    private SiteTreeTableModelAdapter _urlTreeTableModel;
    private ArrayList _conversationActions = new ArrayList();
    
    private Map _urlColumns = new HashMap();
    
    private ShowConversationAction _showConversationAction = new ShowConversationAction();
    
    private Listener _listener = new Listener();
    
    private Logger _logger = Logger.getLogger(getClass().getName());
    
    /** Creates new form SummaryPanel */
    public SummaryPanel() {
        initComponents();
        
        initTree();
        addTreeListeners();
        
        initTable();
        addTableListeners();
        addConversationActions(new Action[] {_showConversationAction});
    }
    
    public void setModel(SiteModel model) {
        if (_model != null) {
            _urlTreeTableModel.setModel(null);
            _conversationTableModel.setModel(null);
            _showConversationAction.setModel(null);
            _model.removeSiteModelListener(_listener);
        }
        _model = model;
        if (model != null) {
            _urlTreeTableModel.setModel(_model);
            _conversationTableModel.setModel(_model);
            _showConversationAction.setModel(_model);
            _model.addSiteModelListener(_listener);
        }
    }
    
    private void initTree() {
        _urlTreeTableModel = new SiteTreeTableModelAdapter() {
            public boolean isFiltered(HttpUrl url) {
                return ((SiteTreeTableModelAdapter)this)._model.getConversationCount(url) == 0;
            }
        };
        _urlTreeTable = new JTreeTable(_urlTreeTableModel);
        
        ColumnDataModel cdm = new ColumnDataModel() {
            public Object getValue(Object key) {
                if (_model == null) return null;
                return _model.getUrlProperty((HttpUrl) key, "METHODS");
            }
            public String getColumnName() { return "Methods"; }
            public Class getColumnClass() { return String.class; }
        };
        _urlColumns.put("METHODS", cdm);
        _urlTreeTableModel.addColumn(cdm);
        
        cdm = new ColumnDataModel() {
            public Object getValue(Object key) {
                if (_model == null) return null;
                return _model.getUrlProperty((HttpUrl) key, "STATUS");
            }
            public String getColumnName() { return "Status"; }
            public Class getColumnClass() { return String.class; }
        };
        _urlColumns.put("STATUS", cdm);
        _urlTreeTableModel.addColumn(cdm);
        
        JTree urlTree = _urlTreeTable.getTree();
        urlTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        urlTree.setRootVisible(false);
        urlTree.setShowsRootHandles(true);
        urlTree.setCellRenderer(new UrlTreeRenderer());
        
        int[] preferredColumnWidths = {
            400, 80, 80, 50, 30, 30, 30
        };
        
        javax.swing.table.TableColumnModel columnModel = _urlTreeTable.getColumnModel();
        for (int i=0; i<Math.min(preferredColumnWidths.length, columnModel.getColumnCount()); i++) {
            columnModel.getColumn(i).setPreferredWidth(preferredColumnWidths[i]);
        }
        
        treeScrollPane.setViewportView(_urlTreeTable);
    }
    
    private void addTreeListeners() {
        // Listen for when the selection changes.
        // We use this to set the selected URLInfo for each action, and
        // to filter the conversation list
        final JTree urlTree = _urlTreeTable.getTree();
        urlTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath selection = urlTree.getSelectionPath();
                _treeURL = null;
                if (selection != null) {
                    Object o = selection.getLastPathComponent();
                    if (o instanceof HttpUrl) {
                        _treeURL = (HttpUrl) o;
                    }
                }
                if (treeCheckBox.isSelected()) {
                    _conversationTableModel.setUrl(_treeURL);
                }
                synchronized (_urlActions) {
                    for (int i=0; i<_urlActions.size(); i++) {
                        AbstractAction action = (AbstractAction) _urlActions.get(i);
                        action.putValue("URL", _treeURL);
                    }
                }
            }
        });
        _urlTreeTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger() && _urlActions.size() > 0) {
                    urlPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    public void addUrlActions(Action[] actions) {
        if (actions == null) return;
        for (int i=0; i<actions.length; i++) {
            _urlActions.add(actions[i]);
        }
        for (int i=0; i<actions.length; i++) {
            urlPopupMenu.add(new JMenuItem(actions[i]));
        }
    }
    
    public void addUrlColumns(ColumnDataModel[] columns) {
        if (columns == null) return;
        for (int i=0; i<columns.length; i++) {
            _urlTreeTableModel.addColumn(columns[i]);
        }
    }
    
    private void initTable() {
        _conversationTableModel = new ConversationTableModel();
        
        ColumnDataModel cdm = new ColumnDataModel() {
            public Object getValue(Object key) {
                if (_model == null) return null;
                return _model.getConversationProperty((ConversationID) key, "METHOD");
            }
            public String getColumnName() { return "Method"; }
            public Class getColumnClass() { return String.class; }
        };
        _conversationTableModel.addColumn(cdm);
        
        cdm = new ColumnDataModel() {
            public Object getValue(Object key) {
                if (_model == null) return null;
                HttpUrl url = _model.getUrlOf((ConversationID) key);
                return url.getScheme() + "://" + url.getHost() + ":" + url.getPort();
            }
            public String getColumnName() { return "Host"; }
            public Class getColumnClass() { return String.class; }
        };
        _conversationTableModel.addColumn(cdm);
        
        cdm = new ColumnDataModel() {
            public Object getValue(Object key) {
                if (_model == null) return null;
                HttpUrl url = _model.getUrlOf((ConversationID) key);
                return url.getPath();
            }
            public String getColumnName() { return "Path"; }
            public Class getColumnClass() { return String.class; }
        };
        _conversationTableModel.addColumn(cdm);
        
        cdm = new ColumnDataModel() {
            public Object getValue(Object key) {
                if (_model == null) return null;
                HttpUrl url = _model.getUrlOf((ConversationID) key);
                return url.getParameters();
            }
            public String getColumnName() { return "Parameters"; }
            public Class getColumnClass() { return String.class; }
        };
        _conversationTableModel.addColumn(cdm);
        
        cdm = new ColumnDataModel() {
            public Object getValue(Object key) {
                if (_model == null) return null;
                return _model.getConversationProperty((ConversationID) key, "STATUS");
            }
            public String getColumnName() { return "Status"; }
            public Class getColumnClass() { return String.class; }
        };
        _conversationTableModel.addColumn(cdm);
        
        TableSorter ts = new TableSorter(_conversationTableModel, conversationTable.getTableHeader());
        conversationTable.setModel(ts);
        
        int[] preferredColumnWidths = {
            40, 60, 400, 250, 60, 60
        };
        
        javax.swing.table.TableColumnModel columnModel = conversationTable.getColumnModel();
        for (int i=0; i<Math.min(preferredColumnWidths.length, columnModel.getColumnCount()); i++) {
            columnModel.getColumn(i).setPreferredWidth(preferredColumnWidths[i]);
        }
    }
    
    private void addTableListeners() {
        // This listener updates the registered actions with the selected Conversation
        conversationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int row = conversationTable.getSelectedRow();
                TableModel tm = conversationTable.getModel();
                if (row >-1) {
                    ConversationID id = (ConversationID) tm.getValueAt(row, 0); // UGLY hack! FIXME!!!!
                    synchronized (_conversationActions) {
                        for (int i=0; i<_conversationActions.size(); i++) {
                            Action action = (Action) _conversationActions.get(i);
                            action.putValue("CONVERSATION", id);
                        }
                    }
                }
            }
        });
        
        conversationTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger() && _conversationActions.size() > 0) {
                    conversationPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    if (_conversationActions.size()>0) {
                        Action action = (Action) _conversationActions.get(0);
                        ActionEvent evt = new ActionEvent(conversationTable, 0, (String) action.getValue(Action.ACTION_COMMAND_KEY));
                        if (action.isEnabled()) {
                            action.actionPerformed(evt);
                        }
                    }
                }
            }
        });
        
    }
    
    public void addConversationActions(Action[] actions) {
        if (actions == null) return;
        for (int i=0; i<actions.length; i++) {
            _conversationActions.add(actions[i]);
        }
        for (int i=0; i<actions.length; i++) {
            conversationPopupMenu.add(new JMenuItem(actions[i]));
        }
    }
    
    public void addConversationColumns(ColumnDataModel[] columns) {
        if (columns == null) return;
        for (int i=0; i<columns.length; i++) {
            _conversationTableModel.addColumn(columns[i]);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        urlPopupMenu = new javax.swing.JPopupMenu();
        conversationPopupMenu = new javax.swing.JPopupMenu();
        summarySplitPane = new javax.swing.JSplitPane();
        urlPanel = new javax.swing.JPanel();
        treeCheckBox = new javax.swing.JCheckBox();
        treeScrollPane = new javax.swing.JScrollPane();
        conversationScrollPane = new javax.swing.JScrollPane();
        conversationTable = new javax.swing.JTable();

        urlPopupMenu.setLabel("URL Actions");
        conversationPopupMenu.setLabel("Conversation Actions");

        setLayout(new java.awt.BorderLayout());

        summarySplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        summarySplitPane.setResizeWeight(0.5);
        summarySplitPane.setOneTouchExpandable(true);
        urlPanel.setLayout(new java.awt.GridBagLayout());

        urlPanel.setMinimumSize(new java.awt.Dimension(283, 100));
        urlPanel.setPreferredSize(new java.awt.Dimension(264, 100));
        treeCheckBox.setText("Tree Selection filters conversation list");
        treeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        urlPanel.add(treeCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        urlPanel.add(treeScrollPane, gridBagConstraints);

        summarySplitPane.setLeftComponent(urlPanel);

        conversationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        conversationTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        conversationScrollPane.setViewportView(conversationTable);

        summarySplitPane.setRightComponent(conversationScrollPane);

        add(summarySplitPane, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents
    
    private void treeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeCheckBoxActionPerformed
        if (treeCheckBox.isSelected() && _treeURL != null) {
            _conversationTableModel.setUrl(_treeURL);
        } else {
            _conversationTableModel.setUrl(null);
        }
    }//GEN-LAST:event_treeCheckBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu conversationPopupMenu;
    private javax.swing.JScrollPane conversationScrollPane;
    private javax.swing.JTable conversationTable;
    private javax.swing.JSplitPane summarySplitPane;
    private javax.swing.JCheckBox treeCheckBox;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JPanel urlPanel;
    private javax.swing.JPopupMenu urlPopupMenu;
    // End of variables declaration//GEN-END:variables

    private class Listener extends SiteModelAdapter {
        
        public void urlChanged(HttpUrl url, String property) {
            ColumnDataModel cdm = (ColumnDataModel) _urlColumns.get(property);
            if (cdm != null) cdm.fireValueChanged(url);
        }
        
    }
    
}
