package de.achterblog.util.log;

import java.io.Serial;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoggerTest {
  @BeforeEach
  public void setUp() {
    Logger.resetBuffer();
  }

  @ParameterizedTest
  @EnumSource(value = Level.class, mode = EnumSource.Mode.EXCLUDE, names = "DEBUG")
  public void testWithString(Level level) {
    Logger.log(level, "test");
    assertThat(Logger.getBuffer().trim(), endsWith("] " + level + " - test"));
  }

  @ParameterizedTest
  @EnumSource(value = Level.class, mode = EnumSource.Mode.EXCLUDE, names = "DEBUG")
  public void testWithSupplier(Level level) {
    Logger.log(level, () -> "test" + 1);
    assertThat(Logger.getBuffer().trim(), endsWith("] " + level + " - test1"));
  }

  @Test
  public void testWithException() {
    Logger.log(Level.INFO, "test", new LoggerTestException());
    assertThat(Logger.getBuffer().trim(), containsString("] INFO - test" + System.lineSeparator()));
    assertThat(Logger.getBuffer().trim(), containsString("LoggerTestException"));
  }

  @Test
  public void testFiltered() {
    Logger.log(Level.DEBUG, "test");
    Logger.log(Level.DEBUG, () -> {
      throw new IllegalStateException("test");
    });
    assertThat(Logger.getBuffer(), is(""));
  }

  private static final class LoggerTestException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
  }
}