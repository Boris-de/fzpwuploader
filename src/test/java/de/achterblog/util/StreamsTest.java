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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.NonNull;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author boris
 */
public class StreamsTest {
  @Test
  public void testToString_InputStream_String() throws Exception {
    String charset = "UTF-8";

    String test = "test\n";
    InputStream in = new ByteArrayInputStream(test.getBytes());
    assertEquals(test, Streams.toString(in, charset));

    test = "test\r\n";
    in = new ByteArrayInputStream(test.getBytes());
    assertEquals(test, Streams.toString(in, charset));

    test = "";
    in = new ByteArrayInputStream(test.getBytes());
    assertEquals(test, Streams.toString(in, charset));
  }

  @Test(expected = NullPointerException.class)
  public void testToString_InputStream_String_NPE1() throws Exception {
    Streams.toString(null, "UTF-8");
  }

  @Test(expected = NullPointerException.class)
  public void testToString_InputStream_String_NPE2() throws Exception {
    Streams.toString(new ByteArrayInputStream(new byte[0]), (String) null);
  }

  @Test(expected = UnsupportedCharsetException.class)
  public void testToString_InputStream_String_UCE() throws Exception {
    Streams.toString(new ByteArrayInputStream(new byte[0]), "asdfasdfadsf");
  }

  @Test(expected = IOException.class)
  public void testToString_InputStream_String_IOE() throws Exception {
    Streams.toString(new IOExceptionThrowingInputStream(), Charset.defaultCharset());
  }

  @Test(expected = NullPointerException.class)
  public void testToString_InputStream_Charset_NPE1() throws Exception {
    Streams.toString(null, Charset.defaultCharset());
  }

  @Test(expected = NullPointerException.class)
  public void testToString_InputStream_Charset_NPE2() throws Exception {
    Streams.toString(new ByteArrayInputStream(new byte[0]), (Charset) null);
  }

  @Test(expected = IOException.class)
  public void testToString_InputStream_Charset_IOE() throws Exception {
    Streams.toString(new IOExceptionThrowingInputStream(), "UTF-8");
  }

  @Test
  public void testToBytes() throws Exception {
    String test = "abcdefg";
    byte[] result = Streams.toBytes(new ByteArrayInputStream(test.getBytes()));
    assertArrayEquals(test.getBytes(), result);
  }

  @Test(expected = NullPointerException.class)
  public void testToBytesNPE() throws Exception {
    Streams.toBytes(null);
  }

  @Test(expected = IOException.class)
  public void testToBytesIOE() throws Exception {
    Streams.toBytes(new IOExceptionThrowingInputStream());
  }

  @Test
  public void testConstructor() throws Exception {
    assertThat(Streams.class.getDeclaredConstructors().length, is(1));
    Constructor<Streams> constructor = Streams.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    try {
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("constructor should not work");
    } catch (InvocationTargetException e) {
      assertTrue(e.getCause() instanceof AssertionError);
    }
  }

  private static class IOExceptionThrowingInputStream extends InputStream {
    @Override
    public synchronized int read() throws IOException {
      throw new IOException();
    }

    @Override
    public int read(@NonNull byte[] b) throws IOException {
      throw new IOException();
    }

    @Override
    public synchronized int read(@NonNull byte[] b, int off, int len) throws IOException {
      throw new IOException();
    }
  }
}