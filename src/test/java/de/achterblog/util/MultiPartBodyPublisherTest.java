package de.achterblog.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.concurrent.Flow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Jimfs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiPartBodyPublisherTest {
  private FileSystem inMemoryFileSystem;

  @BeforeEach
  public void setUp() {
    inMemoryFileSystem = Jimfs.newFileSystem();
  }

  @AfterEach
  public void tearDown() throws IOException {
    inMemoryFileSystem.close();
  }

  @Test
  public void test() throws IOException {
    final var path = inMemoryFileSystem.getPath("path");
    Files.writeString(path, "content");
    try (final var publisher = new MultiPartBodyPublisher(StandardCharsets.UTF_8, () -> "###boundary###")) {
      publisher.addPart("name", "test");
      publisher.addPart("name", path, null, "image/jpeg");

      final var build = publisher.build();

      final var subscriber = new ByteBuffersToStringSubscriber();
      build.subscribe(subscriber);

      assertThat(subscriber.content, is("--###boundary###\r\n" +
                                          "Content-Disposition: form-data; name=\"name\"\r\n" +
                                          "Content-Type: text/plain; charset=UTF-8\r\n" +
                                          "\r\n" +
                                          "test\r\n" +
                                          "--###boundary###\r\n" +
                                          "Content-Disposition: form-data; name=\"name\"; filename=\"path\"\r\n" +
                                          "Content-Type: image/jpeg\r\n" +
                                          "\r\n" +
                                          "content\r\n" +
                                          "--###boundary###--"));
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
  public void testCannotReadFromFile() throws IOException {
    final var path = inMemoryFileSystem.getPath("path");
    Files.createDirectories(path);
    try (final var publisher = new MultiPartBodyPublisher(StandardCharsets.UTF_8)) {
      publisher.addPart("name", path, null, "image/jpeg");

      final var build = publisher.build();

      final var subscriber = new ByteBuffersToStringSubscriber();
      final var e = assertThrows(RuntimeIOException.class, () -> build.subscribe(subscriber));
      assertThat(e, instanceOf(RuntimeIOException.class));
      assertThat(e.getMessage(), is("IOException while generating multi parts: path: not a regular file"));
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
      throw t instanceof RuntimeException ? (RuntimeException) t : new IllegalStateException("Subscriber.onError", t);
    }

    @Override
    public void onComplete() {
      content = buffer.toString(StandardCharsets.UTF_8);
    }
  }
}