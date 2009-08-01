/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
   * Read a stream into a Byte-Array
   *
   * @param in The stream that should be read
   * @return The bytes of this stream
   * @throws IOException Forwarded from the stream-read-operations
   * @throws NullPointerException If {@code in == null}
   */
  public static byte[] toBytes(InputStream in) throws IOException {
    if (in == null) {
      throw new NullPointerException("in");
    }

    InputStream bufIn = new BufferedInputStream(in);
    ByteArrayOutputStream out = new ByteArrayOutputStream(256);

    int bytesRead;
    byte[] buffer = new byte[256];
    while ((bytesRead = bufIn.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);
    }
    return out.toByteArray();
  }

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