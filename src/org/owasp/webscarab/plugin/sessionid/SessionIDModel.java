/*
 * SessionIDModel.java
 *
 * Created on 29 April 2005, 08:00
 */

package org.owasp.webscarab.plugin.sessionid;

import org.owasp.webscarab.model.FrameworkModel;
import org.owasp.webscarab.model.ConversationModel;
import org.owasp.webscarab.model.StoreException;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.ConversationID;
import org.owasp.webscarab.plugin.AbstractPluginModel;

import java.util.Map;
import java.util.TreeMap;

import java.math.BigInteger;
import java.util.logging.Logger;

/**
 *
 * @author  rogan
 */
public class SessionIDModel extends AbstractPluginModel {
    
    private FrameworkModel _model;
    private SessionIDStore _store = null;
    
    private Map _sessionIDs = new TreeMap();
    private Map _calculators = new TreeMap();
    
    private Logger _logger = Logger.getLogger(getClass().getName());
    
    /** Creates a new instance of SessionIDModel */
    public SessionIDModel(FrameworkModel model) {
        super(model);
        _model = model;
    }
    
    public ConversationModel getConversationModel() {
        return _model.getConversationModel();
    }
    
    public void setSession(String type, Object store, String session) throws StoreException {
        _calculators.clear();
        if (store instanceof SessionIDStore) {
            _store = (SessionIDStore) store;
        } else {
            throw new StoreException("Store is a " + store.getClass().getName());
        }
        for (int i=0; i<_store.getSessionIDNameCount(); i++) {
            String key = _store.getSessionIDName(i);
            Calculator calc = new DefaultCalculator();
            _calculators.put(key, calc);
            for (int j=0; j<_store.getSessionIDCount(key); j++) {
                calc.add(_store.getSessionIDAt(key, j));
            }
        }
        fireSessionIDsChanged();
        setModified(false);
    }
    
    public void setCalculator(String key, Calculator calc) {
        _calculators.put(key, calc);
        calc.reset();
        synchronized(_store) {
            int count = _store.getSessionIDCount(key);
            for (int i=0; i<count; i++) {
                calc.add(_store.getSessionIDAt(key, i));
            }
        }
        fireCalculatorChanged(key);
    }
    
    public void addSessionID(String key, SessionID id) {
        setModified(true);
        int insert = _store.addSessionID(key, id);
        Calculator calc = (Calculator) _calculators.get(key);
        if (calc == null) {
            calc = new DefaultCalculator();
            _calculators.put(key, calc);
        }
        boolean changed = calc.add(id);
        fireSessionIDAdded(key, insert);
        if (changed) fireCalculatorChanged(key);
    }
    
    public int getSessionIDNameCount() {
        if (_store == null) return 0;
        return _store.getSessionIDNameCount();
    }
    
    public String getSessionIDName(int index) {
        return _store.getSessionIDName(index);
    }
    
    public int getSessionIDCount(String key) {
        return _store.getSessionIDCount(key);
    }
    
    public SessionID getSessionIDAt(String key, int index) {
        return _store.getSessionIDAt(key, index);
    }
    
    public BigInteger getSessionIDValue(String key, SessionID id) {
        Calculator calc = (Calculator) _calculators.get(key);
        if (calc == null) return null;
        return calc.calculate(id);
    }
    
    public Request getRequest(ConversationID id) {
        return _model.getRequest(id);
    }
    
    public void flush() throws StoreException {
        if (_store != null && isModified()) _store.flush();
        setModified(false);
    }
    
    public void addModelListener(SessionIDListener listener) {
        super.addModelListener(listener);
        synchronized(_listenerList) {
            _listenerList.add(SessionIDListener.class, listener);
        }
    }
    
    public void removeModelListener(SessionIDListener listener) {
        super.removeModelListener(listener);
        synchronized(_listenerList) {
            _listenerList.remove(SessionIDListener.class, listener);
        }
    }
    
    protected void fireSessionIDAdded(String key, int index) {
        // Guaranteed to return a non-null array
        Object[] listeners = _listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SessionIDListener.class) {
                try {
                    ((SessionIDListener)listeners[i+1]).sessionIDAdded(key, index);
                } catch (Exception e) {
                    _logger.severe("Unhandled exception: " + e);
                }
            }
        }
    }

    protected void fireCalculatorChanged(String key) {
        // Guaranteed to return a non-null array
        Object[] listeners = _listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SessionIDListener.class) {
                try {
                    ((SessionIDListener)listeners[i+1]).calculatorChanged(key);
                } catch (Exception e) {
                    _logger.severe("Unhandled exception: " + e);
                }
            }
        }
    }

    protected void fireSessionIDsChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = _listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SessionIDListener.class) {
                try {
                    ((SessionIDListener)listeners[i+1]).sessionIDsChanged();
                } catch (Exception e) {
                    _logger.severe("Unhandled exception: " + e);
                }
            }
        }
    }

}
