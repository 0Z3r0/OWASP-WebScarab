/***********************************************************************
 *
 * $CVSHeader$
 *
 * This file is part of WebScarab, an Open Web Application Security
 * Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2004 Rogan Dawes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Getting Source
 * ==============
 *
 * Source for this application is maintained at Sourceforge.net, a
 * repository for free software projects.
 * 
 * For details, please see http://www.sourceforge.net/projects/owasp
 *
 */

/*
 * Main.java
 *
 * Created on June 16, 2004, 10:11 AM
 */

package org.owasp.webscarab.ui.swing;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.owasp.webscarab.model.FileSystemStore;
import org.owasp.webscarab.model.StoreException;

import org.owasp.webscarab.model.Preferences;
import org.owasp.webscarab.model.SiteModel;

import org.owasp.webscarab.plugin.Framework;

import org.owasp.webscarab.plugin.fragments.Fragments;
import org.owasp.webscarab.plugin.fragments.swing.FragmentsPanel;

import org.owasp.webscarab.plugin.manualrequest.ManualRequest;
import org.owasp.webscarab.plugin.manualrequest.swing.ManualRequestPanel;

import org.owasp.webscarab.plugin.proxy.Proxy;
import org.owasp.webscarab.plugin.proxy.BeanShell;
import org.owasp.webscarab.plugin.proxy.BrowserCache;
import org.owasp.webscarab.plugin.proxy.CookieTracker;
import org.owasp.webscarab.plugin.proxy.ManualEdit;
import org.owasp.webscarab.plugin.proxy.NTLMFilter;
import org.owasp.webscarab.plugin.proxy.RevealHidden;
import org.owasp.webscarab.plugin.proxy.swing.ProxyPanel;
import org.owasp.webscarab.plugin.proxy.swing.BeanShellPanel;
import org.owasp.webscarab.plugin.proxy.swing.ManualEditPanel;
import org.owasp.webscarab.plugin.proxy.swing.MiscPanel;

import org.owasp.webscarab.plugin.sessionid.SessionIDAnalysis;
import org.owasp.webscarab.plugin.sessionid.swing.SessionIDPanel;

import org.owasp.webscarab.plugin.spider.Spider;
import org.owasp.webscarab.plugin.spider.swing.SpiderPanel;

import org.owasp.webscarab.util.TextFormatter;

/**
 *
 * @author  knoppix
 */
public class Main {
    
    private static Framework _framework = null;
    private static UIFramework _uif = null;
    
    /** Creates a new instance of Main */
    private Main() {
    }
    
    public static void main(String[] args) {
        initLogging();
        
        try {
            Preferences.loadPreferences(null);
        } catch (IOException ioe) {
            System.err.println("Error loading preferences: " + ioe);
            System.exit(1);
        }
        
        _framework = new Framework();
        _uif = new UIFramework(_framework);
        
        loadPlugins();
        
        _uif.setSessionLoader(new SessionLoader() {
            public void newSession(Component parent, Framework framework) {
                String defaultDir = Preferences.getPreference("WebScarab.defaultDirectory", null);
                JFileChooser jfc = new JFileChooser(defaultDir);
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("Select a directory to write the session into");
                int returnVal = jfc.showOpenDialog(_uif);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File dir = jfc.getSelectedFile();
                    // String dir = file.toString() + System.getProperty("file.separator");
                    try {
                        if (! FileSystemStore.isExistingSession(dir)) {
                            FileSystemStore store = new FileSystemStore(dir);
                            framework.setSession(new SiteModel(store), "FileSystem", dir);
                        } else {
                            // FIXME to change this to prompt to open it if it already exists
                            JOptionPane.showMessageDialog(null, new String[] {dir + " already contains a session ", }, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (StoreException se) {
                        JOptionPane.showMessageDialog(null, new String[] {"Error creating Session : ", se.toString()}, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    Preferences.setPreference("WebScarab.defaultDirectory", jfc.getCurrentDirectory().toString());
                }
            }
            
            public void openSession(Component parent, Framework framework) {
                String defaultDir = Preferences.getPreference("WebScarab.defaultDirectory", null);
                JFileChooser jfc = new JFileChooser(defaultDir);
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("Choose a directory that contains a previous session");
                int returnVal = jfc.showOpenDialog(_uif);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File dir = jfc.getSelectedFile();
                    try {
                        if (FileSystemStore.isExistingSession(dir)) {
                            FileSystemStore store = new FileSystemStore(dir);
                            framework.setSession(new SiteModel(store), "FileSystem", dir);
                        } else {
                            // FIXME to change this to prompt to create it if it does not already exist
                            JOptionPane.showMessageDialog(null, new String[] {dir + " does not contain a session ", }, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (StoreException se) {
                        JOptionPane.showMessageDialog(null, new String[] {"Error loading Session : ", se.toString()}, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    Preferences.setPreference("WebScarab.defaultDirectory", jfc.getCurrentDirectory().toString());
                }
            }
        });
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _uif.show();
                _uif.toFront();
                _uif.requestFocus();
            }
        });
        
    }
    
    private static void initLogging() {
        Logger logger = Logger.getLogger("org.owasp.webscarab");
        logger.setUseParentHandlers(false);
        Handler ch = new ConsoleHandler();
        ch.setFormatter(new TextFormatter());
        logger.addHandler(ch);
        ch.setLevel(Level.INFO);
    }

    public static void loadPlugins() {
        Fragments fragments = new Fragments(_framework);
        _framework.addPlugin(fragments);
        _uif.addPlugin(new FragmentsPanel(fragments));
        
        Proxy proxy = new Proxy(_framework);
        _framework.addPlugin(proxy);
        ProxyPanel proxyPanel = new ProxyPanel(proxy);
        _uif.addPlugin(proxyPanel);
        
        loadProxyPlugins(proxy, proxyPanel);
        
        Spider spider = new Spider(_framework);
        _framework.addPlugin(spider);
        _uif.addPlugin(new SpiderPanel(spider));
        
        ManualRequest manualRequest = new ManualRequest(_framework);
        _framework.addPlugin(manualRequest);
        _uif.addPlugin(new ManualRequestPanel(manualRequest));
        
        SessionIDAnalysis sessionIDAnalysis = new SessionIDAnalysis(_framework);
        _framework.addPlugin(sessionIDAnalysis);
        _uif.addPlugin(new SessionIDPanel(sessionIDAnalysis));
    }
    
    public static void loadProxyPlugins(Proxy proxy, ProxyPanel proxyPanel) {
        ManualEdit me = new ManualEdit();
        proxy.addPlugin(me);
        proxyPanel.addPlugin(new ManualEditPanel(me));
        
        BeanShell bs = new BeanShell();
        proxy.addPlugin(bs);
        proxyPanel.addPlugin(new BeanShellPanel(bs));
        
        RevealHidden rh = new RevealHidden();
        proxy.addPlugin(rh);
        BrowserCache bc = new BrowserCache();
        proxy.addPlugin(bc);
        CookieTracker ct = new CookieTracker();
        proxy.addPlugin(ct);
        NTLMFilter nf = new NTLMFilter();
        proxy.addPlugin(nf);
        proxyPanel.addPlugin(new MiscPanel(rh, bc, ct, nf));
    }
    
}
