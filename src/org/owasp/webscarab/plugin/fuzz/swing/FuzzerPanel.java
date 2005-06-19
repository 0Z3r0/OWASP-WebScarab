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
 * FuzzerPanel.java
 *
 * Created on 26 October 2004, 04:41
 */

package org.owasp.webscarab.plugin.fuzz.swing;

import org.owasp.webscarab.model.ConversationID;
import org.owasp.webscarab.model.HttpUrl;
import org.owasp.webscarab.model.NamedValue;

import org.owasp.webscarab.plugin.fuzz.Fuzzer;
import org.owasp.webscarab.plugin.fuzz.FuzzerEvent;
import org.owasp.webscarab.plugin.fuzz.FuzzerListener;
import org.owasp.webscarab.plugin.fuzz.FuzzerModel;
import org.owasp.webscarab.plugin.fuzz.FuzzSource;
import org.owasp.webscarab.plugin.fuzz.Parameter;
import org.owasp.webscarab.plugin.fuzz.Signature;
import org.owasp.webscarab.plugin.fuzz.FuzzFactory;

import org.owasp.webscarab.ui.swing.SwingPluginUI;
import org.owasp.webscarab.util.swing.ColumnDataModel;

import javax.swing.Action;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.SwingUtilities;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.util.logging.Logger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.io.File;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.IOException;

/**
 *
 * @author  rogan
 */
public class FuzzerPanel extends javax.swing.JPanel implements SwingPluginUI {
    
    private Fuzzer _fuzzer;
    private FuzzerModel _model;
    private HeaderTableModel _headerTableModel;
    private ParameterTableModel _parameterTableModel;
    private FuzzFactory _fuzzFactory;
    private DefaultComboBoxModel _fuzzSources;
    private DefaultListModel _fuzzItems;
    
    private Logger _logger = Logger.getLogger(getClass().getName());
    
    /** Creates new form FuzzerPanel */
    public FuzzerPanel(Fuzzer fuzzer) {
        _fuzzer = fuzzer;
        _model = fuzzer.getModel();
        initComponents();
        initFields();
        
        _fuzzFactory = _fuzzer.getFuzzFactory();
        configureTables();
        configureFuzzDialog();
        
        Listener listener = new Listener();
        _model.addPropertyChangeListener(listener);
        _model.addModelListener(listener);
        _fuzzFactory.addPropertyChangeListener(listener);
    }
    
    private void configureTables() {
        _headerTableModel = new HeaderTableModel();
        headerTable.setModel(_headerTableModel);
        
        _parameterTableModel = new ParameterTableModel();
        paramTable.setModel(_parameterTableModel);
        DefaultComboBoxModel paramTypes = new DefaultComboBoxModel(Parameter.getParameterLocations());
        DefaultCellEditor dce = new DefaultCellEditor(new JComboBox(paramTypes));
        TableColumn col = paramTable.getColumnModel().getColumn(0);
        col.setCellEditor(dce);
        _fuzzSources = new DefaultComboBoxModel(_fuzzFactory.getSourceDescriptions());
        _fuzzSources.insertElementAt("", 0);
        dce = new DefaultCellEditor(new JComboBox(_fuzzSources));
        col = paramTable.getColumnModel().getColumn(5);
        col.setCellEditor(dce);
        paramTable.setRowHeight((int)dce.getComponent().getPreferredSize().getHeight());
    }
    
    private void configureFuzzDialog() {
        fuzzDialog.setBounds(200, 200, 600, 400);
        fuzzDialog.setResizable(false);
        _fuzzItems = new DefaultListModel();
        valueList.setModel(_fuzzItems);
        nameList.setModel(_fuzzSources);
        nameList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                Object value = nameList.getSelectedValue();
                if (value != null && !"".equals(value)) {
                    FuzzSource source = _fuzzFactory.getSource((String)value);
                    _fuzzItems.clear();
                    if (source != null) {
                        itemsLabel.setText("Items : " + source.size());
                        while (source.hasNext()) {
                            _fuzzItems.addElement(source.current());
                            source.increment();
                        }
                    } else {
                        itemsLabel.setText("Items : ");
                    }
                }
            }
        });
    }
    
    private void initFields() {
        methodTextField.setText(_model.getFuzzMethod());
        urlTextField.setText(_model.getFuzzUrl());
        versionTextField.setText(_model.getFuzzVersion());
        statusLabel.setText(_model.getStatus());
        totalTextField.setText(Integer.toString(_model.getTotalRequests()));
        currentTextField.setText(Integer.toString(_model.getRequestIndex()));
    }
    
    private void updateFields(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        Object value = evt.getNewValue();
        if (evt.getSource() == _fuzzFactory) {
            _fuzzSources.removeAllElements();
            _fuzzSources.addElement("");
            String[] names = _fuzzFactory.getSourceDescriptions();
            for (int i=0; i< names.length; i++) {
                _fuzzSources.addElement(names[i]);
            }
        } else if (property.equals(FuzzerModel.PROPERTY_FUZZMETHOD) && !value.equals(methodTextField.getText())) {
            methodTextField.setText(value.toString());
        } else if (property.equals(FuzzerModel.PROPERTY_FUZZURL) && !value.toString().equals(urlTextField.getText())) {
            urlTextField.setText(value.toString());
        } else if (property.equals(FuzzerModel.PROPERTY_FUZZVERSION) && !value.equals(versionTextField.getText())) {
            versionTextField.setText(value.toString());
        } else if (property.equals(FuzzerModel.PROPERTY_REQUESTINDEX) && !value.equals(currentTextField.getText())) {
            currentTextField.setText(value.toString());
        } else if (property.equals(FuzzerModel.PROPERTY_TOTALREQUESTS) && !value.equals(totalTextField.getText())) {
            totalTextField.setText(value.toString());
        } else if (property.equals(FuzzerModel.PROPERTY_STATUS)) {
            statusLabel.setText(value.toString());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        fuzzDialog = new javax.swing.JDialog();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        nameList = new javax.swing.JList();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        valueList = new javax.swing.JList();
        itemsLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        descriptionTextField = new javax.swing.JTextField();
        fileNameTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        loadButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        fuzzPanel = new javax.swing.JPanel();
        requestPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        methodTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        versionTextField = new javax.swing.JTextField();
        headerPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        headerTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        addHeaderButton = new javax.swing.JButton();
        deleteHeaderButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        parameterPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        paramTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        addParameterButton = new javax.swing.JButton();
        deleteParameterButton = new javax.swing.JButton();
        statusPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        totalTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        currentTextField = new javax.swing.JTextField();
        actionPanel = new javax.swing.JPanel();
        sourcesButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();

        fuzzDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        fuzzDialog.setTitle("Fuzz Sources");
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel8.setText("Fuzz Sources");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel4.add(jLabel8, gridBagConstraints);

        jScrollPane3.setMaximumSize(new java.awt.Dimension(100, 32767));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(100, 50));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(100, 131));
        nameList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(nameList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(jScrollPane3, gridBagConstraints);

        jLabel10.setText("Items");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel4.add(jLabel10, gridBagConstraints);

        jScrollPane4.setViewportView(valueList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(jScrollPane4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        fuzzDialog.getContentPane().add(jPanel4, gridBagConstraints);

        itemsLabel.setText("Items : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        fuzzDialog.getContentPane().add(itemsLabel, gridBagConstraints);

        jLabel9.setText("Description : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        fuzzDialog.getContentPane().add(jLabel9, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        fuzzDialog.getContentPane().add(descriptionTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.weightx = 1.0;
        fuzzDialog.getContentPane().add(fileNameTextField, gridBagConstraints);

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        fuzzDialog.getContentPane().add(browseButton, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridLayout(1, 2));

        loadButton.setText("Load");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        jPanel3.add(loadButton);

        deleteButton.setText("Remove");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        jPanel3.add(deleteButton);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jPanel3.add(closeButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        fuzzDialog.getContentPane().add(jPanel3, gridBagConstraints);

        jLabel4.setText("File : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        fuzzDialog.getContentPane().add(jLabel4, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());

        fuzzPanel.setLayout(new java.awt.GridBagLayout());

        requestPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Method");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        requestPanel.add(jLabel3, gridBagConstraints);

        methodTextField.setText("GET");
        methodTextField.setPreferredSize(new java.awt.Dimension(50, 19));
        methodTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                methodTextFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        requestPanel.add(methodTextField, gridBagConstraints);

        jLabel5.setText("URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        requestPanel.add(jLabel5, gridBagConstraints);

        urlTextField.setMinimumSize(new java.awt.Dimension(100, 19));
        urlTextField.setPreferredSize(new java.awt.Dimension(200, 19));
        urlTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlTextFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        requestPanel.add(urlTextField, gridBagConstraints);

        jLabel6.setText("Version");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        requestPanel.add(jLabel6, gridBagConstraints);

        versionTextField.setText("HTTP/1.1");
        versionTextField.setMinimumSize(new java.awt.Dimension(70, 19));
        versionTextField.setPreferredSize(new java.awt.Dimension(70, 19));
        versionTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionTextFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        requestPanel.add(versionTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        fuzzPanel.add(requestPanel, gridBagConstraints);

        headerPanel.setLayout(new java.awt.GridBagLayout());

        headerPanel.setMinimumSize(new java.awt.Dimension(22, 50));
        headerPanel.setPreferredSize(new java.awt.Dimension(527, 100));
        headerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(headerTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        headerPanel.add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridLayout(2, 1));

        addHeaderButton.setText("Add");
        addHeaderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addHeaderButtonActionPerformed(evt);
            }
        });

        jPanel1.add(addHeaderButton);

        deleteHeaderButton.setText("Delete");
        deleteHeaderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteHeaderButtonActionPerformed(evt);
            }
        });

        jPanel1.add(deleteHeaderButton);

        headerPanel.add(jPanel1, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.2;
        fuzzPanel.add(headerPanel, gridBagConstraints);

        jLabel7.setText("Parameters");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        fuzzPanel.add(jLabel7, gridBagConstraints);

        parameterPanel.setLayout(new java.awt.GridBagLayout());

        paramTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(paramTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        parameterPanel.add(jScrollPane2, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridLayout(2, 1));

        addParameterButton.setText("Add");
        addParameterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addParameterButtonActionPerformed(evt);
            }
        });

        jPanel2.add(addParameterButton);

        deleteParameterButton.setText("Delete");
        deleteParameterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteParameterButtonActionPerformed(evt);
            }
        });

        jPanel2.add(deleteParameterButton);

        parameterPanel.add(jPanel2, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        fuzzPanel.add(parameterPanel, gridBagConstraints);

        statusPanel.setLayout(new java.awt.GridLayout(2, 2));

        jLabel1.setText("Total Requests : ");
        statusPanel.add(jLabel1);

        totalTextField.setColumns(5);
        totalTextField.setEditable(false);
        totalTextField.setMinimumSize(new java.awt.Dimension(50, 19));
        statusPanel.add(totalTextField);

        jLabel2.setText("Current Request : ");
        statusPanel.add(jLabel2);

        currentTextField.setColumns(5);
        currentTextField.setEditable(false);
        currentTextField.setMinimumSize(new java.awt.Dimension(50, 19));
        statusPanel.add(currentTextField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        fuzzPanel.add(statusPanel, gridBagConstraints);

        actionPanel.setLayout(new java.awt.GridLayout());

        sourcesButton.setText("Sources");
        sourcesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourcesButtonActionPerformed(evt);
            }
        });

        actionPanel.add(sourcesButton);

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        actionPanel.add(startButton);

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        actionPanel.add(stopButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        fuzzPanel.add(actionPanel, gridBagConstraints);

        add(fuzzPanel, java.awt.BorderLayout.CENTER);

        statusLabel.setMaximumSize(new java.awt.Dimension(200, 15));
        statusLabel.setMinimumSize(new java.awt.Dimension(200, 15));
        statusLabel.setPreferredSize(new java.awt.Dimension(150, 15));
        add(statusLabel, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        fuzzDialog.hide();
    }//GEN-LAST:event_closeButtonActionPerformed
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser jfc = new JFileChooser(fileNameTextField.getText());
        jfc.setDialogTitle("Select a file to load");
        int returnVal = jfc.showOpenDialog(fuzzDialog);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            if (file != null && !file.isDirectory()) {
                fileNameTextField.setText(file.toString());
            }
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        String name = (String) nameList.getSelectedValue();
        if (name != null) {
            _fuzzFactory.removeSource(name);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed
    
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        try {
            String description = descriptionTextField.getText();
            if (description.equals("")) {
                JOptionPane.showMessageDialog(null, new String[] {"Description cannot be empty", }, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            File file = new File(fileNameTextField.getText());
            if (file.isDirectory()) {
                JOptionPane.showMessageDialog(null, new String[] {file.toString() + " is a directory", }, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            _fuzzFactory.loadFuzzStrings(description, file);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, new String[] {"Error load fuzz strings!", ioe.getMessage() }, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_loadButtonActionPerformed
    
    private void sourcesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourcesButtonActionPerformed
        fuzzDialog.show();
    }//GEN-LAST:event_sourcesButtonActionPerformed
    
    private void versionTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionTextFieldActionPerformed
        _model.setFuzzVersion(versionTextField.getText());
    }//GEN-LAST:event_versionTextFieldActionPerformed
    
    private void methodTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodTextFieldActionPerformed
        _model.setFuzzMethod(methodTextField.getText());
    }//GEN-LAST:event_methodTextFieldActionPerformed
    
    private void urlTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlTextFieldActionPerformed
        _model.setFuzzUrl(urlTextField.getText());
    }//GEN-LAST:event_urlTextFieldActionPerformed
    
    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        _model.setBusyFuzzing(false);
    }//GEN-LAST:event_stopButtonActionPerformed
    
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        _fuzzer.startFuzzing();
    }//GEN-LAST:event_startButtonActionPerformed
    
    private void deleteParameterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteParameterButtonActionPerformed
        int row = paramTable.getSelectedRow();
        if (row == -1) return;
        _model.removeFuzzParameter(row);
    }//GEN-LAST:event_deleteParameterButtonActionPerformed
    
    private void addParameterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addParameterButtonActionPerformed
        int row = paramTable.getSelectedRow();
        if (row == -1) {
            row = paramTable.getRowCount();
        }
        _model.addFuzzParameter(row, new Parameter(Parameter.LOCATION_QUERY, "v"+row, "String"), "a" + row, null, 0);
    }//GEN-LAST:event_addParameterButtonActionPerformed
    
    private void deleteHeaderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteHeaderButtonActionPerformed
        int row = headerTable.getSelectedRow();
        if (row == -1) return;
        _model.removeFuzzHeader(row);
    }//GEN-LAST:event_deleteHeaderButtonActionPerformed
    
    private void addHeaderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addHeaderButtonActionPerformed
        int row = headerTable.getSelectedRow();
        if (row == -1) {
            row = headerTable.getRowCount();
        }
        _model.addFuzzHeader(row, new NamedValue("Header", "Value"));
    }//GEN-LAST:event_addHeaderButtonActionPerformed
    
    public Action[] getConversationActions() {
        return new Action[0];
    }
    
    public ColumnDataModel[] getConversationColumns() {
        return new ColumnDataModel[0];
    }
    
    public javax.swing.JPanel getPanel() {
        return this;
    }
    
    public String getPluginName() {
        return "Fuzzer";
    }
    
    public Action[] getUrlActions() {
        return new Action[0];
    }
    
    public ColumnDataModel[] getUrlColumns() {
        return new ColumnDataModel[0];
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JButton addHeaderButton;
    private javax.swing.JButton addParameterButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JTextField currentTextField;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton deleteHeaderButton;
    private javax.swing.JButton deleteParameterButton;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JDialog fuzzDialog;
    private javax.swing.JPanel fuzzPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JTable headerTable;
    private javax.swing.JLabel itemsLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton loadButton;
    private javax.swing.JTextField methodTextField;
    private javax.swing.JList nameList;
    private javax.swing.JTable paramTable;
    private javax.swing.JPanel parameterPanel;
    private javax.swing.JPanel requestPanel;
    private javax.swing.JButton sourcesButton;
    private javax.swing.JButton startButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton stopButton;
    private javax.swing.JTextField totalTextField;
    private javax.swing.JTextField urlTextField;
    private javax.swing.JList valueList;
    private javax.swing.JTextField versionTextField;
    // End of variables declaration//GEN-END:variables
    
    private class Listener implements PropertyChangeListener, FuzzerListener {
        
        public void propertyChange(final PropertyChangeEvent evt) {
            Runnable runner = new Runnable() {
                public void run() {
                    updateFields(evt);
                }
            };
            runOnEDT(runner);
        }
        
        public void fuzzHeaderAdded(final FuzzerEvent evt) {
            Runnable runner = new Runnable() {
                public void run() {
                    _headerTableModel.fireTableRowsInserted(evt.getRow(), evt.getRow());
                }
            };
            runOnEDT(runner);
        }
        
        public void fuzzHeaderChanged(final FuzzerEvent evt) {
            Runnable runner = new Runnable() {
                public void run() {
                    _headerTableModel.fireTableRowsUpdated(evt.getRow(), evt.getRow());
                }
            };
            runOnEDT(runner);
        }
        
        public void fuzzHeaderRemoved(final FuzzerEvent evt) {
            Runnable runner = new Runnable() {
                public void run() {
                    _headerTableModel.fireTableRowsDeleted(evt.getRow(), evt.getRow());
                }
            };
            runOnEDT(runner);
        }
        
        public void fuzzParameterAdded(final FuzzerEvent evt) {
            Runnable runner = new Runnable() {
                public void run() {
                    _parameterTableModel.fireTableRowsInserted(evt.getRow(), evt.getRow());
                }
            };
            runOnEDT(runner);
        }
        
        public void fuzzParameterChanged(final FuzzerEvent evt) {
            Runnable runner = new Runnable() {
                public void run() {
                    _parameterTableModel.fireTableRowsUpdated(evt.getRow(), evt.getRow());
                }
            };
            runOnEDT(runner);
        }
        
        public void fuzzParameterRemoved(final FuzzerEvent evt) {
            Runnable runner = new Runnable() {
                public void run() {
                    _parameterTableModel.fireTableRowsDeleted(evt.getRow(), evt.getRow());
                }
            };
            runOnEDT(runner);
        }
        
        private void runOnEDT(Runnable runner) {
            if (SwingUtilities.isEventDispatchThread()) {
                runner.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(runner);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    private class HeaderTableModel extends AbstractTableModel {
        
        private String[] _columnNames = new String[] { "Header", "Value" };
        
        public String getColumnName(int columnIndex) {
            return _columnNames[columnIndex];
        }
        
        public int getColumnCount() {
            return _columnNames.length;
        }
        
        public int getRowCount() {
            return _model.getFuzzHeaderCount();
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            NamedValue header = _model.getFuzzHeader(rowIndex);
            if (columnIndex == 0) {
                return header.getName();
            } else {
                return header.getValue();
            }
        }
        
        public boolean isCellEditable(int rowIndex, int ColumnIndex) {
            return true;
        }
        
        public void setValueAt(Object aValue, int rowIndex, int colIndex) {
            NamedValue header = _model.getFuzzHeader(rowIndex);
            switch (colIndex) {
                case 0: header = new NamedValue((String) aValue, header.getValue()); break;
                case 1: header = new NamedValue(header.getName(), (String) aValue); break;
            }
            _model.setFuzzHeader(rowIndex, header);
        }
        
    }
    
    private class ParameterTableModel extends AbstractTableModel {
        
        private String[] _columnNames = new String[] {"Location", "Name", "Type", "Value", "Priority", "Fuzz Source"};
        
        public String getColumnName(int columnIndex) {
            return _columnNames[columnIndex];
        }
        
        public int getColumnCount() {
            return _columnNames.length;
        }
        
        public int getRowCount() {
            return _model.getFuzzParameterCount();
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            Parameter param = _model.getFuzzParameter(rowIndex);
            if (param == null) return "null";
            switch (columnIndex) {
                case 0: return param.getLocation();
                case 1: return param.getName();
                case 2: return param.getType();
                case 3: return _model.getDefaultParameterValue(rowIndex);
                case 4: return new Integer(_model.getFuzzParameterPriority(rowIndex));
                case 5:
                    FuzzSource source = _model.getParameterFuzzSource(rowIndex);
                    if (source != null) {
                        return source.getDescription();
                    } else {
                        return null;
                    }
            }
            return null;
        }
        
        
        public boolean isCellEditable(int rowIndex, int ColumnIndex) {
            return true;
        }
        
        public void setValueAt(Object aValue, int rowIndex, int colIndex) {
            Parameter parameter = _model.getFuzzParameter(rowIndex);
            Object defValue = _model.getDefaultParameterValue(rowIndex);
            int priority = _model.getFuzzParameterPriority(rowIndex);
            FuzzSource source = _model.getParameterFuzzSource(rowIndex);
            switch (colIndex) {
                case 0: parameter = new Parameter((String) aValue, parameter.getName(), parameter.getType()); break;
                case 1: parameter = new Parameter(parameter.getLocation(), (String) aValue, parameter.getType()); break;
                case 2: parameter = new Parameter(parameter.getLocation(), parameter.getName(), (String) aValue); break;
                case 3: defValue = aValue; break;
                case 4: priority = Integer.parseInt(aValue.toString()); break;
                case 5: source = _fuzzFactory.getSource((String) aValue); break;
            }
            _model.setFuzzParameter(rowIndex, parameter, defValue, source, priority);
        }
        
    }
}
