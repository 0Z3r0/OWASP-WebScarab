/*
 * FuzzerModel.java
 *
 * Created on 06 February 2005, 08:36
 */

package org.owasp.webscarab.plugin.fuzz;

import org.owasp.webscarab.model.FrameworkModel;
import org.owasp.webscarab.model.FrameworkEvent;
import org.owasp.webscarab.model.FrameworkListener;
import org.owasp.webscarab.model.FilteredUrlModel;
import org.owasp.webscarab.model.UrlModel;
import org.owasp.webscarab.model.HttpUrl;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.ConversationID;

import org.owasp.webscarab.plugin.AbstractPluginModel;

import java.util.logging.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author  rogan
 */
public class FuzzerModel extends AbstractPluginModel {
    
    private FrameworkModel _model = null;
    
    private Logger _logger = Logger.getLogger(getClass().getName());
    
    private Map _signatures = new HashMap();
    
    private List _queuedUrls = new LinkedList();
    
    private HttpUrl _url = null;
    private Signature _signature = null;
    
    /** Creates a new instance of FuzzerModel */
    public FuzzerModel(FrameworkModel model) {
        super(model);
        _model = model;
    }
    
    public boolean isAppCandidate(HttpUrl url) {
        List signatures = (List) _signatures.get(url);
        String blank = _model.getUrlProperty(url, "BLANKREQUEST");
        String cookie = _model.getUrlProperty(url, "SET-COOKIE");
        if (cookie != null && !cookie.equals("")) return true;
        if (signatures != null && (blank == null || blank.equals(""))) {
            return true;
        }
        return false;
    }
    
    public UrlModel getUrlModel() {
        return null;
    }
    
    public void setBlankRequest(HttpUrl url) {
        _model.setUrlProperty(url, "BLANKREQUEST", "true");
    }
    
    public boolean hasBlankRequest(HttpUrl url) {
        String blank = _model.getUrlProperty(url, "BLANKREQUEST");
        if (blank == null) return false;
        return (Boolean.valueOf(blank).equals(Boolean.TRUE));
    }
    
    public void setAuthenticationRequired(HttpUrl url, boolean required) {
        _model.setUrlProperty(url,  "AUTHREQUIRED", Boolean.toString(required));
    }
    
    public boolean isAuthenticationRequired(HttpUrl url) {
        String auth = _model.getUrlProperty(url, "AUTHREQUIRED");
        if (auth == null) return false;
        return (Boolean.valueOf(auth).equals(Boolean.TRUE));
    }
    
    public boolean hasErrors(HttpUrl url) {
        String error = _model.getUrlProperty(url, "ERRORS");
        if (error == null) return false;
        return (Boolean.valueOf(error).equals(Boolean.TRUE));
    }
    
    public boolean hasDynamicContent(HttpUrl url) {
        return _model.getUrlProperties(url, "CHECKSUM").length > 1;
    }
    
    public void addCheckSum(HttpUrl url, String checksum) {
        _model.addUrlProperty(url, "CHECKSUM", checksum);
    }
    
    public void setUrl(HttpUrl url) {
        setSignature(null);
        _url = url;
        //fireSignaturesChanged();
    }
    
    public void addSignature(HttpUrl url, Signature signature, ConversationID id) {
        List signatures = (List) _signatures.get(url);
        if (signatures == null) {
            signatures = new ArrayList();
            _signatures.put(url, signatures);
        }
        if (signatures.indexOf(signature)<0) {
            signatures.add(signature);
            fireSignatureAdded(url, signatures.size()-1);
        }
    }
    
    public int getSignatureCount(HttpUrl url) {
        if (url == null) return 0;
        List signatures = (List) _signatures.get(url);
        if (signatures == null) return 0;
        return signatures.size();
    }
    
    public Signature getSignature(HttpUrl url, int i) {
        List signatures = (List) _signatures.get(url);
        if (signatures == null) return null;
        return (Signature) signatures.get(i);
    }
    
    public void setConversationError(ConversationID id) {
        _model.setConversationProperty(id, "ERRORS", "true");
        _model.setUrlProperty(_model.getRequestUrl(id), "ERRORS", "true");
    }
    
    public void queueUrl(HttpUrl url) {
        _queuedUrls.add(url);
    }
    
    public int getQueuedUrlCount() {
        return _queuedUrls.size();
    }
    
    public HttpUrl getQueuedUrl() {
        if (_queuedUrls.size() > 0) return (HttpUrl) _queuedUrls.remove(0);
        _logger.warning("Requested a non-existent url");
        return null;
    }
    
    public void clearUrlQueue() {
        _queuedUrls.clear();
    }
    
    public void setSignature(Signature signature) {
        _signature = null;
        // fireConversationsChanged();
    }
    
    public int getConversationCount() {
        return 0;
    }
    
    public ConversationID getConversationAt() {
        return null;
    }
    
    /**
     * tells listeners that the url's app status has changed
     * @param url the url
     */
    protected void fireAppStatusChanged(HttpUrl url) {
        // Guaranteed to return a non-null array
        Object[] listeners = _listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        FuzzerEvent evt = new FuzzerEvent(this, FuzzerEvent.URL_APPSTATUS_CHANGED, url);
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==FuzzerListener.class) {
                try {
                    ((FuzzerListener)listeners[i+1]).appStatusChanged(evt);
                } catch (Exception e) {
                    _logger.severe("Unhandled exception: " + e);
                }
            }
        }
    }
    
    public void addModelListener(FuzzerListener listener) {
        super.addModelListener(listener);
        _listenerList.add(FuzzerListener.class, listener);
    }
    
    /**
     * tells listeners that the url's app status has changed
     * @param url the url
     */
    protected void fireSignatureAdded(HttpUrl url, int position) {
        // Guaranteed to return a non-null array
        Object[] listeners = _listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        FuzzerEvent evt = new FuzzerEvent(this, FuzzerEvent.URL_SIGNATURE_ADDED, url);
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==FuzzerListener.class) {
                try {
                    ((FuzzerListener)listeners[i+1]).signatureAdded(evt);
                } catch (Exception e) {
                    _logger.severe("Unhandled exception: " + e);
                }
            }
        }
    }
    
    /**
     * tells listeners that the url's app status has changed
     * @param url the url
     */
    protected void fireAuthenticationRequired(HttpUrl url) {
        // Guaranteed to return a non-null array
        Object[] listeners = _listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        FuzzerEvent evt = new FuzzerEvent(this, FuzzerEvent.URL_AUTHENTICATION_REQUIRED, url);
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==FuzzerListener.class) {
                try {
                    ((FuzzerListener)listeners[i+1]).authenticationRequired(evt);
                } catch (Exception e) {
                    _logger.severe("Unhandled exception: " + e);
                }
            }
        }
    }
    
    /**
     * tells listeners that the url's app status has changed
     * @param url the url
     */
    protected void fireUrlError(HttpUrl url) {
        // Guaranteed to return a non-null array
        Object[] listeners = _listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        FuzzerEvent evt = new FuzzerEvent(this, FuzzerEvent.URL_ERROR, url);
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==FuzzerListener.class) {
                try {
                    ((FuzzerListener)listeners[i+1]).urlError(evt);
                } catch (Exception e) {
                    _logger.severe("Unhandled exception: " + e);
                }
            }
        }
    }
    
    private class Listener implements FrameworkListener {
        
        public void conversationPropertyChanged(FrameworkEvent evt) {
        }
        
        public void cookieAdded(FrameworkEvent evt) {
        }
        
        public void cookieRemoved(FrameworkEvent evt) {
        }
        
        public void cookiesChanged() {
        }
        
        public void urlPropertyChanged(FrameworkEvent evt) {
            HttpUrl url = evt.getUrl();
            String property = evt.getPropertyName();
            if (property == null) return;
            if (property.equals("SET-COOKIE")) fireAppStatusChanged(url);
            if (property.equals("BLANKREQUEST")) fireAppStatusChanged(url);
            if (property.equals("CHECKSUM")) fireAppStatusChanged(url);
            if (property.equals("ERRORS")) fireUrlError(url);
            if (property.equals("AUTHREQUIRED")) fireAuthenticationRequired(url);
        }
        
    }

}
