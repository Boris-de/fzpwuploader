/*
 * This file is part of the FZPWUploader
 *
 * Copyright (C) 2009-2020 achterblog.de
 *
 * FZPWUploader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FZPWUploader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FZPWUploader.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.achterblog.fzpwuploader.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.achterblog.fzpwuploader.UploadBatch;
import de.achterblog.fzpwuploader.UploadBatch.UploadBatchCallback;
import de.achterblog.util.ApplicationProperties;
import de.achterblog.util.log.Level;
import de.achterblog.util.log.Logger;

/**
 * UI implementation of Main window.
 *
 * (this was initially created with the Netbeans Form editor, but is now just this java file)
 */
public class Uploader extends JFrame {
  private final JProgressBar activityProgressBar;
  private final JList<String> fileList = new JList<>();
  private final JProgressBar progressBar = new JProgressBar();
  private final JPasswordField textFieldPassword = new JPasswordField();
  private final JTextField textFieldUsername = new JTextField();
  private final JTextArea urlOutputArea = new JTextArea();

  /** Creates new form Uploader */
  public Uploader() {
    activityProgressBar = new JProgressBar();
    activityProgressBar.setIndeterminate(true);

    final var scrollPane = new JScrollPane();

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setTitle("fzpwuploader " + ApplicationProperties.INSTANCE.getVersion());

    fileList.setModel(FileListModel.EMPTY_MODEL);
    scrollPane.setViewportView(fileList);

    final JLabel lableUser = createLabel(textFieldUsername, "User:");
    final JLabel labelPassword = createLabel(textFieldPassword, "Password:");

    progressBar.setStringPainted(true);

    final JButton buttonSelect = createButton("Select...", this::buttonSelectActionPerformed);
    final JButton buttonUpload = createButton("Upload", this::buttonUploadActionPerformed);

    final var scrollPaneUrlOutput = new JScrollPane();
    urlOutputArea.setColumns(20);
    urlOutputArea.setEditable(false);
    urlOutputArea.setRows(5);
    scrollPaneUrlOutput.setViewportView(urlOutputArea);

    setJMenuBar(createMenuBar());

    final var layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(lableUser)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(textFieldUsername, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
            .addGap(14, 14, 14)
            .addComponent(labelPassword)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(textFieldPassword, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addComponent(scrollPaneUrlOutput, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
              .addGroup(layout.createSequentialGroup()
                .addComponent(buttonSelect)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonUpload)
                .addGap(18, 18, 18)
                .addComponent(activityProgressBar, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
              .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
          .addComponent(lableUser)
          .addComponent(textFieldUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addComponent(labelPassword)
          .addComponent(textFieldPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(scrollPaneUrlOutput, GroupLayout.PREFERRED_SIZE, 183, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(buttonSelect)
                .addComponent(buttonUpload))
              .addComponent(activityProgressBar, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
            .addGap(7, 7, 7))
          .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
        .addContainerGap())
    );

    pack();

    progressBar.setValue(0);
    activityProgressBar.setVisible(false);
  }

  private JLabel createLabel(JTextField textFieldPassword, String s) {
    final var labelPassword = new JLabel();
    labelPassword.setLabelFor(textFieldPassword);
    labelPassword.setText(s);
    return labelPassword;
  }

  private JButton createButton(String text, ActionListener actionListener) {
    final var button = new JButton();
    button.setText(text);
    button.addActionListener(actionListener);
    return button;
  }

  private JMenuBar createMenuBar() {
    final var menuBar = new JMenuBar();

    final var menuFile = new JMenu();
    menuFile.setText("File");

    final var menuItemExit = createMenuItem("Exit", this::menuItemExitActionPerformed);
    menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
    menuFile.add(menuItemExit);

    menuBar.add(menuFile);

    final var menuQuestionMark = new JMenu();
    menuQuestionMark.setText("?");
    menuQuestionMark.add(createMenuItem("Log", this::menuItemLogActionPerformed));
    menuQuestionMark.add(createMenuItem("About", this::menuItemAboutActionPerformed));

    menuBar.add(menuQuestionMark);
    return menuBar;
  }

  private JMenuItem createMenuItem(String text, ActionListener actionListener) {
    final var menuItem = new JMenuItem();
    menuItem.setText(text);
    menuItem.addActionListener(actionListener);
    return menuItem;
  }

  private void buttonSelectActionPerformed(ActionEvent evt) {
    final var chooser = new JFileChooser();
    final var filter = new FileNameExtensionFilter("Images", "jpg", "jpeg");
    chooser.setFileFilter(filter);
    chooser.setMultiSelectionEnabled(true);

    final var preview = new ImagePreviewAccessory();
    chooser.setAccessory(preview);
    chooser.addPropertyChangeListener(preview);

    final var files = getFilesWithOpenDialog(chooser);
    fileList.setModel(new FileListModel(files));
  }

  private List<Path> getFilesWithOpenDialog(JFileChooser chooser) {
    final int returnVal = chooser.showOpenDialog(null);
    if (returnVal != JFileChooser.APPROVE_OPTION) {
      Logger.log(Level.DEBUG, "No files selected");
      return List.of();
    }

    final var files = getSelectedPaths(chooser);
    Logger.log(Level.DEBUG, "Selected " + files.size() + " files");
    return files;
  }

  private void buttonUploadActionPerformed(ActionEvent evt) {
    if (textFieldUsername.getText().isEmpty() || textFieldPassword.getPassword().length == 0) {
      JOptionPane.showMessageDialog(this, "Please enter the login details", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    final var list = (FileListModel) this.fileList.getModel();
    this.fileList.setModel(FileListModel.EMPTY_MODEL);

    if (list.getSize() == 0 || list.getSize() > 20) {
      JOptionPane.showMessageDialog(this, "Only 1-20 file are allowed", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    progressBar.setValue(0);
    progressBar.setMaximum(list.getSize());
    Uploader.this.setEnabled(false);

    SwingWorker<String, Integer> task = new BackgroundUpload(list);
    task.addPropertyChangeListener(evt1 -> {
      if ("progress".equals(evt1.getPropertyName())) {
        progressBar.setValue((Integer) evt1.getNewValue());
      }
    });
    activityProgressBar.setVisible(true);
    task.execute();
  }

  private void menuItemExitActionPerformed(ActionEvent evt) {
    System.exit(0);
  }

  private void menuItemAboutActionPerformed(ActionEvent evt) {
    new AboutBox(this).setVisible(true);
  }

  private void menuItemLogActionPerformed(ActionEvent evt) {
    new LogView(this).setVisible(true);
  }

  private static List<Path> getSelectedPaths(JFileChooser chooser) {
    return Arrays.stream(chooser.getSelectedFiles())
      .map(File::toPath)
      .collect(Collectors.toList());
  }

  private final class BackgroundUpload extends SwingWorker<String, Integer> {
    private final FileListModel fileList;
    private final AtomicInteger uploadCount = new AtomicInteger(0);

    BackgroundUpload(FileListModel fileList) {
      this.fileList = fileList;
    }

    @Override
    public String doInBackground() {
      final var username = textFieldUsername.getText();
      final var password = new String(textFieldPassword.getPassword());
      return new UploadBatch(username, password, new UploadBatchCallbackImpl()).upload(fileList);
    }

    @Override
    protected void done() {
      try {
        urlOutputArea.setText(get());
        activityProgressBar.setVisible(false);
      } catch (InterruptedException | ExecutionException e) {
        Logger.log(Level.ERROR, "Exception in Future.get()", e);
      }
      Uploader.this.setEnabled(true);
    }

    private final class UploadBatchCallbackImpl implements UploadBatchCallback {
      @Override
      public void uploaded(Path uploaded) {
        setProgress(uploadCount.incrementAndGet());
      }

      @Override
      public void failed(Path cur) {
        // TODO: show an error somewhere
        setProgress(uploadCount.incrementAndGet());
      }
    }
  }
}
