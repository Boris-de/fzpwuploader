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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author boris
 */
class FileNameExtensionFilter extends FileFilter {
  private final String description;
  private final List<String> extensions;

  public FileNameExtensionFilter(String desc, String ... extensions) {
    description = desc;
    this.extensions = new ArrayList<String>(extensions.length);
    for (String extension : extensions) {
      this.extensions.add(extension.toLowerCase());
    }
  }

  @Override
  public boolean accept(File f) {
    for (String extension : extensions) {
      if (f.getName().toLowerCase().endsWith(extension)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getDescription() {
    return description;
  }
}
