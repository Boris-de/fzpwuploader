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

import static org.hamcrest.Matchers.is;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class MaxLengthStringBufferWriterTest {
  @Test
  public void testZeroLength() throws IOException {
    MaxLengthStringBufferWriter w = new MaxLengthStringBufferWriter(0);
    w.append("x");
    Assert.assertThat(w.toString(), is(""));
  }

  @Test
  public void testSingleCharacter() throws IOException {
    MaxLengthStringBufferWriter w = new MaxLengthStringBufferWriter(1);
    w.append("x");
    Assert.assertThat(w.toString(), is("x"));
    w.append("y");
    Assert.assertThat(w.toString(), is("y"));
  }

  @Test
  public void testCutOffIsLineBased() throws IOException {
    MaxLengthStringBufferWriter w = new MaxLengthStringBufferWriter(10);
    w.append("12345\n6789");
    Assert.assertThat(w.toString(), is("12345\n6789"));
    w.append("y");
    Assert.assertThat(w.toString(), is("6789y"));
  }

  @Test
  public void testClose() throws IOException {
    MaxLengthStringBufferWriter w = new MaxLengthStringBufferWriter(10);
    w.append("1234");
    Assert.assertThat(w.toString(), is("1234"));
    w.close();
    Assert.assertThat(w.toString(), is(""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMaxSize() throws IOException {
    new MaxLengthStringBufferWriter(-1);
  }
}
