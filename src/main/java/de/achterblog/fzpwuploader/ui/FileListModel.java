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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.swing.*;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A ListModel for file-names that allows access to the corresponding File-object of an item.
 *
 * @author boris
 */
class FileListModel extends AbstractListModel implements Iterable<Path> {
  private static final long serialVersionUID = 1L;

  private final List<Path> files;
  static final FileListModel EMPTY_MODEL = new FileListModel(Collections.<Path>emptyList());

  /**
   *
   * @throws NullPointerException if {@code files == null}
   * @throws IllegalStateException if {@code files} contains null
   */
  public FileListModel(List<Path> files) {
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
    return Objects.toString(files.get(index).getFileName());
  }

  @NonNull
  @Override
  public Iterator<Path> iterator() {
    return files.iterator();
  }
}
