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
package de.achterblog.fzpwuploader;

import de.achterblog.fzpwuploader.ui.Uploader;
import java.awt.HeadlessException;
import java.math.BigDecimal;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.slf4j.LoggerFactory;

/**
 * Seperate Launcher too have as few dependencies on classes as possible at
 * launch-time to check the java-version <b>before</b> a ClassNotFound is thrown...
 *
 * @author boris
 */
public class Launcher {
  public static void main(String args[]) throws InterruptedException {
    setLookAndFeel();

    checkJavaVersion();

    java.awt.EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        new Uploader().setVisible(true);
      }
    });
  }

  private static void checkJavaVersion() throws HeadlessException {
    String javaVersionStr = System.getProperty("java.version");
    if (javaVersionStr == null || javaVersionStr.length() < 3) {
      javaVersionStr = "0.0";
    }

    BigDecimal javaVersion = new BigDecimal(javaVersionStr.substring(0, 3));
    if (javaVersion.compareTo(new BigDecimal("1.6")) < 0) {
      String javaVendor = System.getProperty("java.vendor");
      JOptionPane.showMessageDialog(null, "Your java maybe too old, this tool may not start.\n"
              + "Your Java-version is " + javaVersionStr + " from " + javaVendor, "Java too old?", JOptionPane.WARNING_MESSAGE);
    }
  }

  private static void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      LoggerFactory.getLogger(Launcher.class).warn("Error setting native LaF", e);
    }
  }
}
