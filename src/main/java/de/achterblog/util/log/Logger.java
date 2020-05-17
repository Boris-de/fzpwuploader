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
package de.achterblog.util.log;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

public final class Logger {
  private static final MaxLengthStringBufferWriter writer = new MaxLengthStringBufferWriter(1024 * 1024);
  private static final Level LOG_LEVEL = Level.valueOf(System.getProperty("de.achterblog.log.level", "INFO"));

  public static void log(Level level, String message) {
    log(level, message, null);
  }

  public static void log(Level level, String message, Throwable e) {
    log(level, () -> message, e);
  }

  public static void log(Level level, Supplier<String> messageSupplier) {
    log(level, messageSupplier, null);
  }

  public static void log(Level level, Supplier<String> messageSupplier, Throwable e) {
    if (level.compareTo(LOG_LEVEL) >= 0) {
      final var newLine = System.lineSeparator();
      final PrintStream target = level.compareTo(Level.WARN) >= 0 ? System.err : System.out;
      final StringBuilder messageBuilder = new StringBuilder();
      messageBuilder.append(ZonedDateTime.now())
                    .append(" [").append(Thread.currentThread().getName()).append("] ")
                    .append(level).append(" - ")
                    .append(messageSupplier.get()).append(newLine);
      if (e != null) {
        final StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        messageBuilder.append(stringWriter).append(newLine);
      }
      final String message = messageBuilder.toString();
      target.println(message);
      writer.append(message).append(newLine);
    }
  }

  public static String getBuffer() {
    return writer.toString();
  }

  static void resetBuffer() {
    writer.reset();
  }
}
