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
