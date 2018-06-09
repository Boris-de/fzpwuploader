/*
 * This file is part of the FZPWUploader
 *
 * Copyright (C) 2009-2014 achterblog.de
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractListModel;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A ListModel for file-names that allows access to the corresponding File-object of an item.
 *
 * @author boris
 */
class FileListModel extends AbstractListModel implements Iterable<File> {
  private static final long serialVersionUID = 1L;

  private final List<File> files;
  static final FileListModel EMPTY_MODEL;


  static {
    List<File> temp = Collections.emptyList();
    EMPTY_MODEL = new FileListModel(temp);
  }

  /**
   *
   * @throws NullPointerException if {@code files == null}
   * @throws IllegalStateException if {@code files} contains null
   */
  public FileListModel(List<File> files) {
    super();
    this.files = Collections.unmodifiableList(new ArrayList<>(files)); // defensive copy

    if (this.files.contains(null)) {
      throw new IllegalStateException("files contains null");
    }
  }

  @Override
  public int getSize() {
    return files.size();
  }

  /**
   * Get the name of the file in this position
   *
   * @param index The index in the list-view
   * @return The filename
   * @throws IndexOutOfBoundsException If {@code (index < 0 || index >= getSize())}
   */
  @Override
  public String getElementAt(int index) throws IndexOutOfBoundsException {
    return files.get(index).getName();
  }

  @NonNull
  @Override
  public Iterator<File> iterator() {
    return files.iterator();
  }
}
