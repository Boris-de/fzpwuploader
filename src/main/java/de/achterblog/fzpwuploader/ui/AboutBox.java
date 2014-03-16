/*
 * This file is part of the FZPWUploader
 *
 * Copyright (C) 2009 achterblog.de
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
 */
package de.achterblog.fzpwuploader.ui;

import de.achterblog.util.Streams;
import java.io.IOException;
import javax.swing.JDialog;

/**
 * An ugly AboutBox
 *
 * @author boris
 */
public class AboutBox extends JDialog {
  /** Creates new form AboutBox */
  public AboutBox(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    initComponents();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel1 = new javax.swing.JPanel();
    copyrightLabel = new javax.swing.JLabel();
    apacheNoticeLabel = new javax.swing.JLabel();
    qosNoticeLabel = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    licenseTextArea = new javax.swing.JTextArea();
    jPanel3 = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    bundledLicensesTextArea = new javax.swing.JTextArea();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("About");
    setResizable(false);

    javaLabel.setText("Running Java " + System.getProperty("java.version") + " by " + System.getProperty("java.vendor"));

    osLabel.setText("System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " running on " + System.getProperty("os.arch"));

    copyrightLabel.setText("<html>© 2009-2014 achterblog.de<br>This tool is published under the terms of the GPLv2 or later.<br>See tabs \"License\" and \"Bundled Licenses\" for details.");

    apacheNoticeLabel.setText("<html>This product includes software developed by<br>The Apache Software Foundation (http://www.apache.org/).");

    qosNoticeLabel.setText("<html>This product includes software developed by qos.ch<br>which is Copyright (c) 2004-2013 QOS.ch  All rights reserved.");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(javaLabel)
          .addComponent(osLabel)
          .addComponent(copyrightLabel)
          .addComponent(apacheNoticeLabel)
          .addComponent(qosNoticeLabel))
        .addContainerGap(13, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(javaLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(osLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(copyrightLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(apacheNoticeLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(qosNoticeLabel)
        .addContainerGap(17, Short.MAX_VALUE))
    );

    jTabbedPane1.addTab("General", jPanel1);

    licenseTextArea.setColumns(20);
    licenseTextArea.setEditable(false);
    licenseTextArea.setLineWrap(true);
    licenseTextArea.setRows(5);
    licenseTextArea.setText(getLicenseText());
    jScrollPane1.setViewportView(licenseTextArea);

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
        .addContainerGap())
    );

    jTabbedPane1.addTab("License", jPanel2);

    bundledLicensesTextArea.setColumns(20);
    bundledLicensesTextArea.setRows(5);
    bundledLicensesTextArea.setText(getBundledLicensesText());
    jScrollPane2.setViewportView(bundledLicensesTextArea);

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
        .addContainerGap())
    );

    jTabbedPane1.addTab("Bundled Licenses", jPanel3);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        AboutBox dialog = new AboutBox(new javax.swing.JFrame(), true);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            System.exit(0);
          }
        });
        dialog.setVisible(true);
      }
    });
  }

  private String getLicenseText() {
    try {
      return Streams.toString(AboutBox.class.getResourceAsStream("/COPYING"), "US-ASCII");
    } catch (IOException e) {
      return "Could not read License. Visit http://www.gnu.org/licenses/old-licenses/gpl-2.0.html";
    }
  }

  private String getBundledLicensesText() {
    try {
      final String LINE = "\n---------------------------------------\n";
      Class<?> clazz = AboutBox.class;
      StringBuilder b = new StringBuilder();
      b.append(Streams.toString(clazz.getResourceAsStream("/bundled-licenses.txt"), "US-ASCII"));
      b.append(LINE).append("Full text of the Apache 2.0 license follows\n").append(LINE);
      b.append(Streams.toString(clazz.getResourceAsStream("/apache-2.0.txt"), "US-ASCII"));
      b.append(LINE).append("Full text of the LGPL 2.1 follows\n").append(LINE);
      b.append(Streams.toString(clazz.getResourceAsStream("/lgpl-2.1.txt"), "US-ASCII"));
      return b.toString();
    } catch (IOException e) {
      return "Could not read bundled licenses. Perhaps this package does not contain any?";
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel apacheNoticeLabel;
  private javax.swing.JTextArea bundledLicensesTextArea;
  private javax.swing.JLabel copyrightLabel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JTabbedPane jTabbedPane1;
  private final javax.swing.JLabel javaLabel = new javax.swing.JLabel();
  private javax.swing.JTextArea licenseTextArea;
  private final javax.swing.JLabel osLabel = new javax.swing.JLabel();
  private javax.swing.JLabel qosNoticeLabel;
  // End of variables declaration//GEN-END:variables
}
