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
 * MessagePanel.java
 *
 * Created on November 6, 2003, 8:43 AM
 */

package org.owasp.webscarab.ui.swing.editors;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

import java.awt.Component;
import javax.swing.CellEditor;

import org.owasp.webscarab.ui.swing.TranscoderFrame;

/**
 *
 * @author  rdawes
 */
public class UrlEncodedPanel extends javax.swing.JPanel implements ByteArrayEditor {
    
    private boolean _editable = false;
    private boolean _modified = false;
    private Vector _columns;
    private DefaultTableModel _tableModel;
    private String _data = null;
    
    /** Creates new form MessagePanel */
    public UrlEncodedPanel() {
        initComponents();
        setName("URLEncoded");
        _columns = new Vector();
        _columns.add("Variable");
        _columns.add("Value");
        _tableModel  = new DefaultTableModel(_columns.toArray(), 0);
        _tableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                _modified = true;
            }
        });
        headerTable.setModel(_tableModel);
        headerTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        headerTable.getColumnModel().getColumn(1).setPreferredWidth(500);
        setEditable(_editable);
    }
    
    public String[] getContentTypes() {
        return new String[] { "application/x-www-form-urlencoded" };
    }
    
    public void setBytes(byte[] bytes) {
        if (bytes == null) {
            System.err.println("Got null");
            _data = null;
            _tableModel.setDataVector(null, _columns);
            headerTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            headerTable.getColumnModel().getColumn(1).setPreferredWidth(500);
        } else {
            System.err.println("Got " + bytes.length);
            _data = new String(bytes);
            String[] variables = _data.split("&");
            String[][] pairs = new String[variables.length][2];
            for (int i=0; i<variables.length; i++) {
                String[] parts = variables[i].split("=",2);
                if (parts.length > 0) {
                    pairs[i][0] = parts[0];
                }
                if (parts.length > 1) {
                    pairs[i][1] = TranscoderFrame.urlDecode(parts[1]);
                }
            }
            _tableModel.setDataVector(pairs, _columns.toArray());
            headerTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            headerTable.getColumnModel().getColumn(1).setPreferredWidth(500);
        }
        _modified = false;
    }
    
    private void stopEditing() {
        Component comp = headerTable.getEditorComponent();
        if (comp != null && comp instanceof CellEditor) {
            ((CellEditor)comp).stopCellEditing();
        }
    }
    
    public byte[] getBytes() {
        if (_editable) {
            stopEditing();
            if (_modified) {
                StringBuffer buff = new StringBuffer();
                Vector pairs = _tableModel.getDataVector();
                for (int i=0; i<pairs.size(); i++) {
                    Vector v = (Vector) pairs.elementAt(i);
                    String name = (String) v.elementAt(0);
                    if (name == null || name.equals("")) continue;
                    String value = (String) v.elementAt(1);
                    if (value == null) value = "";
                    if (i>0) buff.append("&");
                    buff.append(name).append("=").append(TranscoderFrame.urlEncode(value));
                }
                _data = buff.toString();
            }
        }
        if (_data == null) {
            return new byte[0];
        } else {
            return _data.getBytes();
        }
    }
    
    public void setEditable(boolean editable) {
        _editable = editable;
        buttonPanel.setVisible(_editable);
        java.awt.Color color;
        if (_editable) {
            color = new java.awt.Color(255, 255, 255);
        } else {
            color = new java.awt.Color(204, 204, 204);
        }
        headerTable.setBackground(color);
    }
    
    public boolean isModified() {
        if (_editable) stopEditing();
        return _editable && _modified;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        headerTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        insertButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(402, 102));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 50));
        headerTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(headerTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        insertButton.setText("Insert");
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        buttonPanel.add(insertButton, gridBagConstraints);

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        buttonPanel.add(deleteButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(buttonPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int rowIndex = headerTable.getSelectedRow();
        if (rowIndex > -1) {
            _tableModel.removeRow(rowIndex);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed
    
    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        int rowIndex = headerTable.getSelectedRow();
        if (rowIndex > -1) {
            _tableModel.insertRow(rowIndex, new Object[2]);
        } else {
            _tableModel.insertRow(_tableModel.getRowCount(), new Object[2]);
        }
    }//GEN-LAST:event_insertButtonActionPerformed
    
    public static void main(String[] args) {
        byte[] content = new byte[0];
        org.owasp.webscarab.model.Request request = new org.owasp.webscarab.model.Request();
        try {
            String req = "/home/rdawes/santam/webscarab/conversations/147-request";
            if (args.length == 1) {
                req = args[0];
            }
            java.io.FileInputStream fis = new java.io.FileInputStream(req);
            request.read(fis);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        
        final UrlEncodedPanel panel = new UrlEncodedPanel();
        
        javax.swing.JFrame top = new javax.swing.JFrame(panel.getName());
        top.getContentPane().setLayout(new java.awt.BorderLayout());
        top.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                System.exit(0);
            }
        });
        
        javax.swing.JButton button = new javax.swing.JButton("GET");
        top.getContentPane().add(panel);
        top.getContentPane().add(button, java.awt.BorderLayout.SOUTH);
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.out.println(new String(panel.getBytes()));
            }
        });
        top.setBounds(100,100,600,400);
        top.show();
        try {
            panel.setEditable(true);
            panel.setBytes(request.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTable headerTable;
    private javax.swing.JButton insertButton;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
}
