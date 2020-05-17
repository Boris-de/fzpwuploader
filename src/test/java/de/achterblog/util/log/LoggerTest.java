package de.achterblog.util.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class LoggerTest {
  @BeforeEach
  void setUp() {
    Logger.resetBuffer();
  }

  @ParameterizedTest
  @EnumSource(value = Level.class, mode = EnumSource.Mode.EXCLUDE, names = "DEBUG")
  void testWithString(Level level) {
    Logger.log(level, "test");
    assertThat(Logger.getBuffer().trim(), endsWith("] " + level + " - test"));
  }

  @ParameterizedTest
  @EnumSource(value = Level.class, mode = EnumSource.Mode.EXCLUDE, names = "DEBUG")
  void testWithSupplier(Level level) {
    Logger.log(level, () -> "test" + 1);
    assertThat(Logger.getBuffer().trim(), endsWith("] " + level + " - test1"));
  }

  @Test
  void testWithException() {
    Logger.log(Level.INFO, "test", new LoggerTestException());
    assertThat(Logger.getBuffer().trim(), containsString("] INFO - test" + System.lineSeparator()));
    assertThat(Logger.getBuffer().trim(), containsString("LoggerTestException"));
  }

  @Test
  void testFiltered() {
    Logger.log(Level.DEBUG, "test");
    Logger.log(Level.DEBUG, () -> {
      throw new IllegalStateException("test");
    });
    assertThat(Logger.getBuffer(), is(""));
  }

  private static final class LoggerTestException extends Exception {

  }
}