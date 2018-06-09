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
package de.achterblog.util;

import java.io.Writer;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A simple StringWriter that tries to keep the size of the String below a given maximum.
 *
 * @author boris
 */
final class MaxLengthStringBufferWriter extends Writer {
  private final int maxSize;
  private final StringBuffer buffer = new StringBuffer();

  /**
   * Create a new instance with a given maximal size.
   *
   * @param maxSize The maximal size of the buffer
   */
  MaxLengthStringBufferWriter(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("maxSize must not be negative");
    }
    this.maxSize = maxSize;
  }

  @Override
  public void write(@NonNull char[] chars, int offset, int len) {
    synchronized (buffer) {
      buffer.append(chars, offset, len);
      final int size = buffer.length();
      if (size > maxSize) {
        int cutIndex = buffer.indexOf("\n", size - maxSize);
        cutIndex = (cutIndex < 0) ? size - maxSize : cutIndex + 1;
        buffer.delete(0, cutIndex);
      }
    }
  }

  @Override
  public String toString() {
    return buffer.toString();
  }

  @Override
  public void flush() {
    // nothing to do (in memory)
  }

  @Override
  public void close() {
    buffer.setLength(0);
  }
}
