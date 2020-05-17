package de.achterblog.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ApplicationPropertiesTest {
  @Test
  public void test() {
    assertThat(new ApplicationProperties("/test-application.properties").getVersion(), is("1.0"));
    assertThat(ApplicationProperties.INSTANCE.getVersion(), allOf(notNullValue(), not(is("unknown"))));
  }

  @Test
  public void testMissingResource() {
    assertThat(new ApplicationProperties("/sdfsafasdfas").getVersion(), is("unknown"));
  }
}