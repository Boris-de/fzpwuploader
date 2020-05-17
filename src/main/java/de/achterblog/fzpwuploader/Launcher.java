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
package de.achterblog.fzpwuploader;

import javax.swing.*;

import de.achterblog.fzpwuploader.ui.Uploader;
import de.achterblog.util.log.Level;
import de.achterblog.util.log.Logger;

/**
 * Separate Launcher too have as few dependencies on classes as possible at
 * launch-time to check the java-version <b>before</b> a ClassNotFound is thrown...
 *
 * @author boris
 */
public class Launcher {
  public static void main(String[] args) {
    setLookAndFeel();

    java.awt.EventQueue.invokeLater(() -> new Uploader().setVisible(true));
  }

  private static void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
      Logger.log(Level.WARN, "Error setting native LaF", e);
    }
  }
}
