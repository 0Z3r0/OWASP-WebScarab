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
 * WebScarab.java
 *
 * Created on July 13, 2003, 7:11 PM
 */

package org.owasp.webscarab.ui.swing;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;

import org.owasp.webscarab.model.Preferences;
import org.owasp.webscarab.model.SiteModel;
import org.owasp.webscarab.model.StoreException;
import org.owasp.webscarab.plugin.Framework;
import org.owasp.webscarab.plugin.FrameworkUI;
import org.owasp.webscarab.util.TextFormatter;
import org.owasp.webscarab.util.swing.DocumentHandler;

/**
 *
 * @author  rdawes
 */
public class UIFramework extends JFrame implements FrameworkUI {
    
    private Framework _framework;
    private SiteModel _model;
    private ArrayList _plugins;
    private SessionLoader _loader;
    
    private CookieJarViewer _cookieJarViewer;
    private SummaryPanel _summaryPanel;
    
    private TranscoderFrame _transcoder = null;
    
    private Logger _logger = Logger.getLogger("org.owasp.webscarab");
    
    private DocumentHandler _dh;
    
    /** Creates new form WebScarab */
    public UIFramework(Framework framework) {
        _framework = framework;
        
        initComponents();
        setPreferredSize();
        
        _summaryPanel = new SummaryPanel();
        mainTabbedPane.add(_summaryPanel, "Summary");
        
        _cookieJarViewer = new CookieJarViewer();
        
        initLogging();
        
        framework.setUI(this);
        
        setModel(framework.getModel());
    }
    
    public void setModel(final SiteModel model) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _model = model;
                _summaryPanel.setModel(model);
                _cookieJarViewer.setModel(model);
            }
        });
    }
    
    public void setSessionLoader(SessionLoader loader) {
        _loader = loader;
    }
    
    private void initLogging() {
        
        _dh = new DocumentHandler();
        _dh.setFormatter(new TextFormatter());
        _logger.addHandler(_dh);
        
        final Document doc = _dh.getDocument();
        logTextArea.setDocument(doc);
        doc.addDocumentListener(new TextScroller(logTextArea));
        
        String level = Preferences.getPreference("UI.logLevel","INFO");
        if (level.equals("SEVERE")) { severeLogRadioButtonMenuItem.setSelected(true); }
        else if (level.equals("INFO")) { infoLogRadioButtonMenuItem.setSelected(true); }
        else if (level.equals("FINE")) { fineLogRadioButtonMenuItem.setSelected(true); }
        else if (level.equals("FINER")) { finerLogRadioButtonMenuItem.setSelected(true); }
        else if (level.equals("FINEST")) { finestLogRadioButtonMenuItem.setSelected(true); }
    }
    
    private void setPreferredSize() {
        try {
            int xpos = Integer.parseInt(Preferences.getPreference("WebScarab.position.x").trim());
            int ypos = Integer.parseInt(Preferences.getPreference("WebScarab.position.y").trim());
            int width = Integer.parseInt(Preferences.getPreference("WebScarab.size.x").trim());
            int height = Integer.parseInt(Preferences.getPreference("WebScarab.size.y").trim());
            setBounds(xpos,ypos,width,height);
        } catch (NumberFormatException nfe) {
            setSize(800,600);
            setExtendedState(MAXIMIZED_BOTH);
        } catch (NullPointerException npe) {
            setSize(800,600);
            setExtendedState(MAXIMIZED_BOTH);
        }
    }
    
    public void addPlugin(final SwingPluginUI plugin) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JPanel panel = plugin.getPanel();
                if (panel != null) mainTabbedPane.add(panel, plugin.getPluginName());
                _summaryPanel.addUrlActions(plugin.getUrlActions());
                _summaryPanel.addUrlColumns(plugin.getUrlColumns());
                _summaryPanel.addConversationActions(plugin.getConversationActions());
                _summaryPanel.addConversationColumns(plugin.getConversationColumns());
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        logLevelButtonGroup = new javax.swing.ButtonGroup();
        mainSplitPane = new javax.swing.JSplitPane();
        mainTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        proxyMenuItem = new javax.swing.JMenuItem();
        certsMenuItem = new javax.swing.JMenuItem();
        cookieJarMenuItem = new javax.swing.JMenuItem();
        transcoderMenuItem = new javax.swing.JMenuItem();
        conversationSearchMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        logMenu = new javax.swing.JMenu();
        severeLogRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        infoLogRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        fineLogRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        finerLogRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        finestLogRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("WebScarab");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                UIFramework.this.windowClosing(evt);
            }
        });

        mainSplitPane.setBorder(null);
        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(1.0);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setAutoscrolls(true);
        mainTabbedPane.setMinimumSize(new java.awt.Dimension(300, 100));
        mainTabbedPane.setPreferredSize(new java.awt.Dimension(800, 600));
        mainSplitPane.setLeftComponent(mainTabbedPane);

        jScrollPane1.setToolTipText("");
        jScrollPane1.setMinimumSize(new java.awt.Dimension(22, 40));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(3, 64));
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setOpaque(false);
        logTextArea.setBackground(new java.awt.Color(204, 204, 204));
        logTextArea.setEditable(false);
        logTextArea.setToolTipText("");
        jScrollPane1.setViewportView(logTextArea);

        mainSplitPane.setRightComponent(jScrollPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(mainSplitPane, gridBagConstraints);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        newMenuItem.setMnemonic('N');
        newMenuItem.setText("New");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newMenuItem);

        openMenuItem.setMnemonic('O');
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        exitMenuItem.setMnemonic('X');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        mainMenuBar.add(fileMenu);

        toolsMenu.setMnemonic('T');
        toolsMenu.setText("Tools");
        proxyMenuItem.setText("Proxies");
        proxyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proxyMenuItemActionPerformed(evt);
            }
        });

        toolsMenu.add(proxyMenuItem);

        certsMenuItem.setText("Certificates");
        certsMenuItem.setToolTipText("Allows configuration of client certificates");
        certsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certsMenuItemActionPerformed(evt);
            }
        });

        toolsMenu.add(certsMenuItem);

        cookieJarMenuItem.setText("Shared Cookies");
        cookieJarMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cookieJarMenuItemActionPerformed(evt);
            }
        });

        toolsMenu.add(cookieJarMenuItem);

        transcoderMenuItem.setText("Transcoder");
        transcoderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transcoderMenuItemActionPerformed(evt);
            }
        });

        toolsMenu.add(transcoderMenuItem);

        conversationSearchMenuItem.setText("Search Conversations");
        conversationSearchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conversationSearchMenuItemActionPerformed(evt);
            }
        });

        toolsMenu.add(conversationSearchMenuItem);

        mainMenuBar.add(toolsMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutMenuItem);

        logMenu.setText("Log level");
        logMenu.setToolTipText("Configures the level of logging output displayed");
        severeLogRadioButtonMenuItem.setText("SEVERE");
        logLevelButtonGroup.add(severeLogRadioButtonMenuItem);
        severeLogRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelActionPerformed(evt);
            }
        });

        logMenu.add(severeLogRadioButtonMenuItem);

        infoLogRadioButtonMenuItem.setText("INFO");
        logLevelButtonGroup.add(infoLogRadioButtonMenuItem);
        infoLogRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelActionPerformed(evt);
            }
        });

        logMenu.add(infoLogRadioButtonMenuItem);

        fineLogRadioButtonMenuItem.setText("FINE");
        logLevelButtonGroup.add(fineLogRadioButtonMenuItem);
        fineLogRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelActionPerformed(evt);
            }
        });

        logMenu.add(fineLogRadioButtonMenuItem);

        finerLogRadioButtonMenuItem.setText("FINER");
        logLevelButtonGroup.add(finerLogRadioButtonMenuItem);
        finerLogRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelActionPerformed(evt);
            }
        });

        logMenu.add(finerLogRadioButtonMenuItem);

        finestLogRadioButtonMenuItem.setText("FINEST");
        logLevelButtonGroup.add(finestLogRadioButtonMenuItem);
        finestLogRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelActionPerformed(evt);
            }
        });

        logMenu.add(finestLogRadioButtonMenuItem);

        helpMenu.add(logMenu);

        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

    }//GEN-END:initComponents
    
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        if (_loader != null) {
            _loader.openSession(this, _framework);
        } else {
            _logger.severe("OPEN called, but loader is null");
        }
    }//GEN-LAST:event_openMenuItemActionPerformed
    
    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        if (_loader != null) {
            _loader.newSession(this, _framework);
        } else {
            _logger.severe("NEW called, but loader is null");
        }
    }//GEN-LAST:event_newMenuItemActionPerformed
    
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        if (! isShowing()) return;
        Preferences.getPreferences().setProperty("WebScarab.size.x",Integer.toString(getWidth()));
        Preferences.getPreferences().setProperty("WebScarab.size.y",Integer.toString(getHeight()));
    }//GEN-LAST:event_formComponentResized
    
    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        if (! isShowing()) return;
        Preferences.getPreferences().setProperty("WebScarab.position.x",Integer.toString(getX()));
        Preferences.getPreferences().setProperty("WebScarab.position.y",Integer.toString(getY()));
    }//GEN-LAST:event_formComponentMoved
    
    private void conversationSearchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conversationSearchMenuItemActionPerformed
        _logger.warning("Searching is not implemented at the moment");
        // new ConversationSearchFrame(_framework.getSiteModel()).show();
    }//GEN-LAST:event_conversationSearchMenuItemActionPerformed
    
    private void logLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logLevelActionPerformed
        String cmd = evt.getActionCommand().toUpperCase();
        if (cmd.equals("SEVERE")) { _dh.setLevel(Level.SEVERE); }
        else if (cmd.equals("INFO")) { _dh.setLevel(Level.INFO); }
        else if (cmd.equals("FINE")) { _dh.setLevel(Level.FINE); }
        else if (cmd.equals("FINER")) { _dh.setLevel(Level.FINER); }
        else if (cmd.equals("FINEST")) { _dh.setLevel(Level.FINEST); }
        else {
            System.err.println("Unknown log level: '" + cmd + "'");
            return;
        }
        Preferences.setPreference("UI.logLevel", cmd);
    }//GEN-LAST:event_logLevelActionPerformed
    
    private void certsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certsMenuItemActionPerformed
        new CertificateDialog(this, _framework).show();
    }//GEN-LAST:event_certsMenuItemActionPerformed
    
    private void transcoderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transcoderMenuItemActionPerformed
        if (_transcoder == null) {
            _transcoder = new TranscoderFrame();
        }
        _transcoder.show();
    }//GEN-LAST:event_transcoderMenuItemActionPerformed
    
    private void cookieJarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cookieJarMenuItemActionPerformed
        _cookieJarViewer.show();
        _cookieJarViewer.toFront();
        _cookieJarViewer.requestFocus();
    }//GEN-LAST:event_cookieJarMenuItemActionPerformed
    
    private void proxyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyMenuItemActionPerformed
        new ProxyConfig(this, _framework).show();
    }//GEN-LAST:event_proxyMenuItemActionPerformed
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exit();
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        String[] message = new String[] {
            "OWASP WebScarab - version " + _framework.getVersion(),
            " - part of the Open Web Application Security Project",
            "See http://www.owasp.org/software/webscarab.html",
            "", "Primary Developer : ",
            "         Rogan Dawes (rogan at dawes.za.net)",
            //            "         Ingo Struck (ingo at ingostruck.de)"
        };
        JOptionPane.showMessageDialog(this, message, "About WebScarab", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    /** Exit the Application */
    private void windowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_windowClosing
        exit();
    }//GEN-LAST:event_windowClosing
    
    private void exit() {
        if (_framework.isModified()) {
            if (_framework.isRunning() && !_framework.stopPlugins()) {
                String[] status = _framework.getStatus();
                int count = status.length;
                String[] message = new String[count+2];
                System.arraycopy(status, 0, message, 0, count);
                message[count] = "";
                message[count+1] = "Force data save anyway?";
                int choice = JOptionPane.showOptionDialog(this, message, "Error - Plugins are busy", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
                if (choice != JOptionPane.YES_OPTION) return;
            }
            try {
                _framework.saveSessionData();
            } catch (Exception e) {
                int choice = JOptionPane.showOptionDialog(this, new String[] {"Error saving session!", e.toString(), "Quit anyway?"}, "Error!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
                if (choice != JOptionPane.YES_OPTION) return;
            }
        }
        _framework.exit();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem certsMenuItem;
    private javax.swing.JMenuItem conversationSearchMenuItem;
    private javax.swing.JMenuItem cookieJarMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JRadioButtonMenuItem fineLogRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem finerLogRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem finestLogRadioButtonMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JRadioButtonMenuItem infoLogRadioButtonMenuItem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.ButtonGroup logLevelButtonGroup;
    private javax.swing.JMenu logMenu;
    private javax.swing.JTextArea logTextArea;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem proxyMenuItem;
    private javax.swing.JRadioButtonMenuItem severeLogRadioButtonMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem transcoderMenuItem;
    // End of variables declaration//GEN-END:variables
    
    private class TextScroller implements DocumentListener {
        
        private JTextComponent _component;
        private TextUI _mapper;
        
        public TextScroller(JTextComponent component) {
            _component = component;
            _mapper = _component.getUI();
        }
        
        public void removeUpdate(DocumentEvent e) {}
        
        public void changedUpdate(DocumentEvent e) {
            if (_mapper != null) {
                try {
                    Rectangle newLoc = _mapper.modelToView(_component, e.getOffset(), Bias.Forward);
                    adjustVisibility(newLoc);
                } catch (BadLocationException ble) {
                }
            }
        }
        
        public void insertUpdate(DocumentEvent e) {
            if (_mapper != null) {
                try {
                    Rectangle newLoc = _mapper.modelToView(_component, e.getOffset(), Bias.Forward);
                    adjustVisibility(newLoc);
                } catch (BadLocationException ble) {
                }
            }
        }
        
        private void adjustVisibility(final Rectangle location) {
            if (location != null) {
                if (SwingUtilities.isEventDispatchThread()) {
                    _component.scrollRectToVisible(location);
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            _component.scrollRectToVisible(location);
                        }
                    });
                }
            }
        }
        
    }
    
}
