package de.achterblog.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Flow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiPartBodyPublisherTest {
  @Test
  public void test(@TempDir final Path tempDir) throws IOException {
    final var path = tempDir.resolve("path");
    Files.writeString(path, "content");
    try (final var publisher = new MultiPartBodyPublisher(StandardCharsets.UTF_8, () -> "###boundary###")) {
      publisher.addPart("name", "test");
      publisher.addPart("name", path, null, "image/jpeg");

      final var build = publisher.build();

      final var subscriber = new ByteBuffersToStringSubscriber();
      build.subscribe(subscriber);

      assertThat(subscriber.content, is("""
                                          --###boundary###\r
                                          Content-Disposition: form-data; name="name"\r
                                          Content-Type: text/plain; charset=UTF-8\r
                                          \r
                                          test\r
                                          --###boundary###\r
                                          Content-Disposition: form-data; name="name"; filename="path"\r
                                          Content-Type: image/jpeg\r
                                          \r
                                          content\r
                                          --###boundary###--"""));
    }
  }

  @Test
  public void testMissingParts() throws IOException {
    try (final var publisher = new MultiPartBodyPublisher(StandardCharsets.UTF_8)) {
      final var e = assertThrows(IllegalStateException.class, publisher::build);
      assertThat(e.getMessage(), is("No parts defined yet"));
    }
  }

  @Test
  public void testCannotReadFromFile(@TempDir final Path tempDir) throws IOException {
    final var path = tempDir.resolve("path");
    Files.createDirectories(path);
    try (final var publisher = new MultiPartBodyPublisher(StandardCharsets.UTF_8)) {
      publisher.addPart("name", path, null, "image/jpeg");

      final var build = publisher.build();

      final var subscriber = new ByteBuffersToStringSubscriber();
      final var e = assertThrows(RuntimeIOException.class, () -> build.subscribe(subscriber));
      assertThat(e, instanceOf(RuntimeIOException.class));
      assertThat(e.getMessage(), startsWith("IOException while generating multi parts:"));
    }
  }

  private static class ByteBuffersToStringSubscriber implements Flow.Subscriber<ByteBuffer> {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private String content;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ByteBuffer b) {
      buffer.write(b.array(), 0, b.remaining());
    }

    @Override
    public void onError(Throwable t) {
      throw t instanceof RuntimeException rte ? rte : new IllegalStateException("Subscriber.onError", t);
    }

    @Override
    public void onComplete() {
      content = buffer.toString(StandardCharsets.UTF_8);
    }
  }
}