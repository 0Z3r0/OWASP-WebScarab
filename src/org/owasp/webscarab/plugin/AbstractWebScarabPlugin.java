/*
 * WebScarabPlugin.java
 *
 * Created on July 10, 2003, 12:21 PM
 */

package org.owasp.webscarab.plugin;

import java.util.Iterator;
import java.util.Properties;

import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.model.Conversation;
import org.owasp.webscarab.model.URLInfo;
import org.owasp.webscarab.model.StoreException;

/** This interface describes the requirements that a WebScarab plugin must implement
 * @author rdawes
 */
public abstract class AbstractWebScarabPlugin implements WebScarabPlugin {
    
    /** This variable is intended to hold any plugin specific properties that could be
     * written to a config file, or read from a config file.
     */    
    protected Properties _prop = new Properties();
    
    /** Configures the plugin, based on any properties read from a configuration file.
     * If any plugin specific properties were not set in the configuration file, copies
     * the default values into the supplied Prop instance.
     * @param prop The properties read from a configuration file, or similar
     */    
    public void mergeProperties(Properties prop) {
        // This just allows us to copy our defaults over into
        // the main properties class, if they are not set already
        Iterator it = _prop.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String value = prop.getProperty(key);
            if (null == value) {
                prop.put(key,_prop.getProperty(key));
            }
        }
        // any future changes are made directly into the system wide props
        _prop = prop;
    }
    
    /** Called by the WebScarab data model once the {@link Response} has been parsed. It
     * is called for all Conversations seen by the model (submitted by all plugins, not
     * just this one).
     * Any information gathered by this module should also be summarised into the
     * supplied URLInfo, since only this analysis procedure will know how to do so!
     * @param request The Request that caused this analysis
     * @param response The Response that was received
     * @param conversation The parsed Conversation to be analysed.
     * @param urlinfo The class instance that contains the summarised information about this
     * particular URL
     * @param parsed A parsed representation of the Response content. Check to see if this is a type
     * that you recognise, e.g. NodeList for HTML content
     */    
    public void analyse(Request request, Response response, Conversation conversation, URLInfo urlinfo, Object parsed) {
    }
    
    /** called to instruct the plugin to save its current state to the specified directory.
     * @throws StoreException if there is any problem saving the session data
     */    
    public void saveSessionData() throws StoreException {
    }
    
    /** Configures a session store for the plugin to use to save any persistent data.
     * The Plugin defines the interface for the store, the store implements the
     * interfaces of each plugin, so that it can be cast to each type in each plugin.
     * This allows us to define the methods that the plugin needs to save its data,
     * without specifying how or where that data is saved. That detail is implemented
     * in a concrete implementation of the various interfaces.
     * The plugin is expected to read any existing data from the store as part of this
     * method, or at any other time that the plugin prefers
     * @param store Store is an object that implements the interface specified by each plugin
     * @throws StoreException if there are any problems reading the existing data out of the store
     */    
    public void setSessionStore(Object store) throws org.owasp.webscarab.model.StoreException {
    }
    
}
