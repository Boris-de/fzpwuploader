/*
 * This file is part of the FZPWUploader
 *
 * Copyright (C) 2009-2020 achterblog.de
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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MaxLengthStringBufferWriterTest {
  @Test
  public void testZeroLength() {
    try (MaxLengthStringBufferWriter w = new MaxLengthStringBufferWriter(0)) {
      w.append("x");
      assertThat(w.toString(), is(""));
    }
  }

  @Test
  public void testSingleCharacter() {
    try (MaxLengthStringBufferWriter w = new MaxLengthStringBufferWriter(1)) {
      w.append("x");
      assertThat(w.toString(), is("x"));
      w.append("y");
      assertThat(w.toString(), is("y"));
    }
  }

  @Test
  public void testCutOffIsLineBased() {
    try (MaxLengthStringBufferWriter w = new MaxLengthStringBufferWriter(10)) {
      w.append("12345\n6789");
      assertThat(w.toString(), is("12345\n6789"));
      w.append("y");
      assertThat(w.toString(), is("6789y"));
    }
  }

  @Test
  public void testClose() {
    try (MaxLengthStringBufferWriter w = new MaxLengthStringBufferWriter(10)) {
      w.append("1234");
      assertThat(w.toString(), is("1234"));
      w.close();
      assertThat(w.toString(), is(""));
    }
  }

  @Test
  public void testInvalidMaxSize() {
    var e = assertThrows(IllegalArgumentException.class,
                         () -> IOUtils.close(new MaxLengthStringBufferWriter(-1)));
    assertThat(e.getMessage(), is("maxSize must not be negative"));
  }
}
