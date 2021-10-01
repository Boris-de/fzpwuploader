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

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.*;

import de.achterblog.util.ApplicationProperties;

/**
 * UI implementation of an ugly AboutBox
 *
 * (this was initially created with the Netbeans Form editor, but is now just this java file)
 */
public class AboutBox extends JDialog {
  /** Creates new form AboutBox */
  public AboutBox(Frame parent) {
    super(parent, true);

    final var tabbedPane = new JTabbedPane();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("About");
    setResizable(false);

    tabbedPane.addTab("General", createGeneralPanel());
    tabbedPane.addTab("License", createLicensePanel("/COPYING", "Could not read License. Visit https://www.gnu.org/licenses/gpl-3.0.html"));
    tabbedPane.addTab("Bundled Licenses", createLicensePanel("/bundled-licenses.txt", "Could not read bundled licenses. Perhaps this package does not contain any?"));

    final var layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
        .addContainerGap())
    );

    pack();
  }

  private JPanel createGeneralPanel() {
    final var versionLabel = createSimpleLabel("fzpwuploader " + ApplicationProperties.INSTANCE.getVersion());
    final var javaLabel = createSimpleLabel("Running Java " + getProperty("java.version") + " by " + getProperty("java.vendor"));
    final var osLabel = createSimpleLabel("System: " + getProperty("os.name") + " " + getProperty("os.version") + " running on " + getProperty("os.arch"));

    final var copyrightLabel = createSimpleLabel("<html>© 2009-2020 achterblog.de<br>This tool is published under the terms of the GPLv3 or later.<br>See tabs \"License\" and \"Bundled Licenses\" for details.");

    final var panel = new JPanel();
    final var layout = new GroupLayout(panel);
    panel.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(versionLabel)
                                .addComponent(javaLabel)
                                .addComponent(osLabel)
                                .addComponent(copyrightLabel))
                    .addContainerGap(13, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(versionLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(javaLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(osLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(copyrightLabel)
                    .addContainerGap(17, Short.MAX_VALUE))
    );

    return panel;
  }

  private JPanel createLicensePanel(final String resource, final String alternativeText) {
    final var bundledLicensesTextArea = new JTextArea();
    bundledLicensesTextArea.setColumns(20);
    bundledLicensesTextArea.setEditable(false);
    bundledLicensesTextArea.setLineWrap(true);
    bundledLicensesTextArea.setRows(5);
    bundledLicensesTextArea.setText(getLicenseText(resource, alternativeText));

    final var scrollPane = new JScrollPane();
    scrollPane.setViewportView(bundledLicensesTextArea);

    final var panel = new JPanel();
    final var layout = new GroupLayout(panel);
    panel.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .addContainerGap())
    );
    return panel;
  }

  private String getLicenseText(final String resource, final String alternativeText) {
    try {
      return getResource(resource);
    } catch (IOException e) {
      return alternativeText;
    }
  }

  private String getResource(String s) throws IOException {
    try (var in = AboutBox.class.getResourceAsStream(s)) {
      Objects.requireNonNull(in, () -> "Could not read resource " + s);
      try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        return r.lines().collect(Collectors.joining("\n"));
      }
    }
  }

  private static String getProperty(String name) {
    return System.getProperty(name, "unknown");
  }

  private static JLabel createSimpleLabel(String text) {
    final var versionLabel = new JLabel();
    versionLabel.setText(text);
    return versionLabel;
  }
}
