/*
 * WebScarabPlugin.java
 *
 * Created on July 10, 2003, 12:21 PM
 */

package src.org.owasp.webscarab.plugins;

import java.util.Properties;
import java.util.Enumeration;

import org.owasp.webscarab.model.*;

/** This interface describes the requirements that a WebScarab plugin must implement
 * @author rdawes
 */
public abstract class AbstractWebScarabPlugin implements WebScarabPlugin {
    
    protected Properties _props = new Properties();
    
    /** Configures the plugin, based on any properties read from a configuration file.
     * If any plugin specific properties were not set in the configuration file, copies
     * the default values into the supplied Properties class.
     * @param properties The properties read from a configuration file, or similar
     */    
    public void setProperties(Properties properties) {
        // This just allows us to copy our defaults over into
        // the main properties class, if they are not set already
        Enumeration propnames = _props.keys();
        while (propnames.hasMoreElements()) {
            String key = (String) propnames.nextElement();
            String value = properties.getProperty(key);
            if (value == null) {
                properties.setProperty(key,_props.getProperty(key));
            }
        }
        _props = properties;
        // Now perform plugin-specific configuration
        configure();
    }
    
    protected void configure() {
    }
    
    /** Called by the WebScarab data model once the {@link Response} has been parsed. It
     * is called for all Conversations seen by the model (submitted by all plugins, not
     * just this one).
     * Any information gathered by this module should also be summarised into the
     * supplied URLInfo, since only this analysis procedure will know how to do so!
     * @param conversation The parsed Conversation to be analysed.
     * @param urlinfo The class instance that contains the summarised information about this
     * particular URL
     */    
    public void analyse(Conversation conversation, URLInfo urlinfo) {
    }
    
    /** called to create any directory structures required by this plugin.
     * @param dir a String representing the base directory under which this session's 
     * data will be saved
     */    
    public void initDirectory(String dir) {
    }
    
    /** Instructs the plugin to discard any session specific data that it may hold */
    public void discardSessionData() {
    }
    
    /** called to instruct the plugin to save its current state to the specified directory.
     * @param dir a String representing the base directory under which this plugin can save its data
     */    
    public void saveSessionData(String dir) {
    }
    
    /** called to instruct the plugin to read any saved state data from the specified directory.
     * @param dir a String representing the base directory under which this plugin can save its data
     */    
    public void loadSessionData(String dir) {
    }
    
}
