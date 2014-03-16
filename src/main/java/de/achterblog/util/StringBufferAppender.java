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
 * along with FZPWUploader.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.achterblog.util;

import ch.qos.logback.core.OutputStreamAppender;
import java.nio.charset.Charset;
import org.apache.commons.io.output.WriterOutputStream;

/**
 * A simple appender that logs Strings to a static StringBuffer.
 *
 * @author boris
 * @param <E> The param used for the {@link ch.qos.logback.core.Appender}
 */
public class StringBufferAppender<E> extends OutputStreamAppender<E> {
  private static final MaxLengthStringBufferWriter writer = new MaxLengthStringBufferWriter(1024 * 1024);

  public static String getBuffer() {
    return writer.toString();
  }

  @Override
  public void start() {
    setOutputStream(new WriterOutputStream(writer, Charset.defaultCharset()));
    super.start();
  }
}
