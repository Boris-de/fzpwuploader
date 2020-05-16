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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Utility-class that provides methods to handle Streams.
 *
 * @author boris
 */
public final class Streams {
  /**
   * Read a stream into a String
   *
   * @param in The stream that should be read
   * @param charset The charset of the stream
   * @return The String from this stream
   * @throws IOException Forwarded from the stream-read-operations
   * @throws NullPointerException If {@code in} or {@code cs} is {@code null}
   */
  public static String toString(InputStream in, String charset) throws IOException {
    if (charset == null) {
      throw new NullPointerException("charset");
    }
    return toString(in, Charset.forName(charset));
  }

  /**
   * Read a stream into a String
   *
   * @param in The stream that should be read
   * @param cs The charset of the stream
   * @return The String from this stream
   * @throws IOException Forwarded from the stream-read-operations
   * @throws NullPointerException If {@code in} or {@code cs} is {@code null}
   */
  public static String toString(InputStream in, Charset cs) throws IOException {
    StringBuilder builder = new StringBuilder(1024);
    Reader r = new BufferedReader(new InputStreamReader(in, cs));

    int charsRead;
    char[] buffer = new char[256];
    while ((charsRead = r.read(buffer)) != -1) {
      builder.append(new String(buffer, 0, charsRead));
    }

    return builder.toString();
  }

  private Streams() {
    throw new AssertionError("Utility class");
  }
}
