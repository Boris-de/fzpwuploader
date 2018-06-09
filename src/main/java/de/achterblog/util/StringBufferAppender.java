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

import java.io.IOException;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;

/**
 * A simple appender that logs Strings to a static StringBuffer.
 *
 * @param <E> The param used for the {@link ch.qos.logback.core.Appender}
 * @author boris
 */
public class StringBufferAppender<E> extends UnsynchronizedAppenderBase<E> {
  private static final MaxLengthStringBufferWriter writer = new MaxLengthStringBufferWriter(1024 * 1024);

  public static String getBuffer() {
    return writer.toString();
  }

  @Override
  protected void append(E event) {
    if (this.isStarted()) {
      try {
        if (event instanceof DeferredProcessingAware) {
          ((DeferredProcessingAware) event).prepareForDeferredProcessing();
        }

        writer.write(event + CoreConstants.LINE_SEPARATOR);
      } catch (IOException e) {
        throw new IllegalStateException("IOException while writing to memory stream: " + e.getMessage(), e);
      }
    }
  }
}
