/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.achterblog.util;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.status.ErrorStatus;

/**
 *
 * @author boris
 */
public class StringBufferAppender extends AppenderBase {
  private int maxSize = 1024 * 1024;
  private static final StringBuffer buffer = new StringBuffer();

  public static String getBuffer() {
    return buffer.toString();
  }

  @Override
  protected void append(Object eventObject) {
    buffer.append(this.layout.doLayout(eventObject));
    if (buffer.length() > maxSize) {
      synchronized (buffer) {
        int size = buffer.length();
        // check if it's still this size
        if (size > maxSize) {
          int cutIndex = buffer.indexOf("\n", size - maxSize);
          cutIndex = (cutIndex < 0) ? size - maxSize : cutIndex;
          buffer.delete(0, cutIndex);
        }
      }
    }
  }

  @Override
  public void start() {
    int errors = 0;
    if (this.layout == null) {
      addStatus(new ErrorStatus("No layout set for the appender named \"" + name + "\".", this));
      errors++;
    }

    // only error free appenders should be activated
    if (errors == 0) {
      super.start();
    }
  }

  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  public int getMaxSize() {
    return maxSize;
  }
}
