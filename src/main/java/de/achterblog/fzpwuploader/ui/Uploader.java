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

import de.achterblog.fzpwuploader.UploadBatch;
import de.achterblog.fzpwuploader.UploadBatch.UploadBatchCallback;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.swingworker.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author boris
 */
public class Uploader extends javax.swing.JFrame {
  private static final Logger logger = LoggerFactory.getLogger(Uploader.class);

  /** Creates new form Uploader */
  public Uploader() {
    initComponents();

    progressBar.setValue(0);
    activityProgressBar.setVisible(false);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jScrollPane1 = new javax.swing.JScrollPane();
    lableUser = new javax.swing.JLabel();
    labelPassword = new javax.swing.JLabel();
    buttonSelect = new javax.swing.JButton();
    buttonUpload = new javax.swing.JButton();
    jScrollPane2 = new javax.swing.JScrollPane();
    activityProgressBar = new javax.swing.JProgressBar();
    menuBar = new javax.swing.JMenuBar();
    menuFile = new javax.swing.JMenu();
    menuItemExit = new javax.swing.JMenuItem();
    menuQuestionMark = new javax.swing.JMenu();
    menuItemLog = new javax.swing.JMenuItem();
    menuItemAbout = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Uploader");
    getContentPane().setLayout(new java.awt.GridBagLayout());

    fileList.setModel(FileListModel.EMPTY_MODEL);
    jScrollPane1.setViewportView(fileList);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    getContentPane().add(jScrollPane1, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    getContentPane().add(textFieldUsername, gridBagConstraints);

    lableUser.setLabelFor(textFieldUsername);
    lableUser.setText("User:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    getContentPane().add(lableUser, gridBagConstraints);

    labelPassword.setLabelFor(textFieldPassword);
    labelPassword.setText("Password:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    getContentPane().add(labelPassword, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    getContentPane().add(textFieldPassword, gridBagConstraints);

    progressBar.setStringPainted(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 4;
    getContentPane().add(progressBar, gridBagConstraints);

    buttonSelect.setText("Select...");
    buttonSelect.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonSelectActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 8;
    getContentPane().add(buttonSelect, gridBagConstraints);

    buttonUpload.setText("Upload");
    buttonUpload.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonUploadActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 8;
    getContentPane().add(buttonUpload, gridBagConstraints);

    urlOutputArea.setColumns(20);
    urlOutputArea.setEditable(false);
    urlOutputArea.setRows(5);
    jScrollPane2.setViewportView(urlOutputArea);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.gridheight = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    getContentPane().add(jScrollPane2, gridBagConstraints);

    activityProgressBar.setIndeterminate(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 2;
    getContentPane().add(activityProgressBar, gridBagConstraints);

    menuFile.setText("File");

    menuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
    menuItemExit.setText("Exit");
    menuItemExit.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuItemExitActionPerformed(evt);
      }
    });
    menuFile.add(menuItemExit);

    menuBar.add(menuFile);

    menuQuestionMark.setText("?");

    menuItemLog.setText("Log");
    menuItemLog.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuItemLogActionPerformed(evt);
      }
    });
    menuQuestionMark.add(menuItemLog);

    menuItemAbout.setText("About");
    menuItemAbout.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuItemAboutActionPerformed(evt);
      }
    });
    menuQuestionMark.add(menuItemAbout);

    menuBar.add(menuQuestionMark);

    setJMenuBar(menuBar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void buttonSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectActionPerformed
    List<File> files;
    JFileChooser chooser = new JFileChooser();
    FileFilter filter = new FileNameExtensionFilter("Images", "jpg", "jpeg");
    chooser.setFileFilter(filter);
    chooser.setMultiSelectionEnabled(true);
    ImagePreviewAccessory preview = new ImagePreviewAccessory();
    chooser.setAccessory(preview);
    chooser.addPropertyChangeListener(preview);
    int returnVal = chooser.showOpenDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      files = Arrays.asList(chooser.getSelectedFiles());
      logger.debug("Selected {} files", files.size());
    } else {
      logger.debug("No files selected");
      files = Collections.emptyList();
    }
    fileList.setModel(new FileListModel(files));
}//GEN-LAST:event_buttonSelectActionPerformed

    private void buttonUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonUploadActionPerformed
      if (textFieldUsername.getText().length() == 0 || textFieldPassword.getPassword().length == 0) {
        JOptionPane.showMessageDialog(this, "Please enter the login details", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      FileListModel list = (FileListModel) this.fileList.getModel();
      this.fileList.setModel(FileListModel.EMPTY_MODEL);

      if (list.getSize() == 0 || list.getSize() > 20) {
        JOptionPane.showMessageDialog(this, "Only 1-20 file are allowed", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      progressBar.setValue(0);
      progressBar.setMaximum(list.getSize());
      Uploader.this.setEnabled(false);

      SwingWorker<String, Integer> task = new BackgroundUpload(list);
      task.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if ("progress".equals(evt.getPropertyName())) {
            progressBar.setValue((Integer) evt.getNewValue());
          }
        }
      });
      activityProgressBar.setVisible(true);
      task.execute();
}//GEN-LAST:event_buttonUploadActionPerformed

    private void menuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExitActionPerformed
      System.exit(0);
}//GEN-LAST:event_menuItemExitActionPerformed

    private void menuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemAboutActionPerformed
      new AboutBox(this, true).setVisible(true);
}//GEN-LAST:event_menuItemAboutActionPerformed

    private void menuItemLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemLogActionPerformed
      new LogView(this, true).setVisible(true);
}//GEN-LAST:event_menuItemLogActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) throws InterruptedException {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      logger.warn("Error setting native LaF", e);
    }
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new Uploader().setVisible(true);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JProgressBar activityProgressBar;
  private javax.swing.JButton buttonSelect;
  private javax.swing.JButton buttonUpload;
  private final javax.swing.JList fileList = new javax.swing.JList();
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JLabel labelPassword;
  private javax.swing.JLabel lableUser;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JMenu menuFile;
  private javax.swing.JMenuItem menuItemAbout;
  private javax.swing.JMenuItem menuItemExit;
  private javax.swing.JMenuItem menuItemLog;
  private javax.swing.JMenu menuQuestionMark;
  private final javax.swing.JProgressBar progressBar = new javax.swing.JProgressBar();
  private final javax.swing.JPasswordField textFieldPassword = new javax.swing.JPasswordField();
  private final javax.swing.JTextField textFieldUsername = new javax.swing.JTextField();
  private final javax.swing.JTextArea urlOutputArea = new javax.swing.JTextArea();
  // End of variables declaration//GEN-END:variables

  private class BackgroundUpload extends SwingWorker<String, Integer> {
    private final FileListModel fileList;
    private final AtomicInteger uploadCount = new AtomicInteger(0);

    public BackgroundUpload(FileListModel fileList) {
      this.fileList = fileList;
    }

    public String doInBackground() {
      final String username = textFieldUsername.getText();
      final String password = new String(textFieldPassword.getPassword());
      return new UploadBatch(username, password, new UploadBatchCallbackImpl()).upload(fileList);
    }

    @Override
    protected void done() {
      try {
        urlOutputArea.setText(get());
        activityProgressBar.setVisible(false);
      } catch (Exception e) {
        logger.error("Exception in Future.get()", e);
      }
      Uploader.this.setEnabled(true);
    }

    private final class UploadBatchCallbackImpl implements UploadBatchCallback {
      public void uploaded(File uploaded) {
        setProgress(uploadCount.incrementAndGet());
      }

      public void failed(File cur) {
        // TODO: show an error somewhere
        setProgress(uploadCount.incrementAndGet());
      }
    }
  }
}
