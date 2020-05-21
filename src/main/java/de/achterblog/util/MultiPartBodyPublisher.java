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
package de.achterblog.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class MultiPartBodyPublisher implements Closeable {
  private static final String CRLF = "\r\n";

  private final List<Part> partsList = new ArrayList<>();
  private final String boundary;
  private final Charset charset;

  private final List<PartsAsBytesIterator> partsAsBytesIterators = new ArrayList<>();

  public MultiPartBodyPublisher(Charset charset) {
    this(charset, () -> UUID.randomUUID().toString());
  }

  MultiPartBodyPublisher(Charset charset, Supplier<String> boundaryGenerator) {
    this.charset = Objects.requireNonNull(charset, "charset");
    this.boundary = boundaryGenerator.get();
  }

  public HttpRequest.BodyPublisher build() {
    validState(!partsList.isEmpty(), "No parts defined yet");
    addEndOfMultiPartPart();
    return HttpRequest.BodyPublishers.ofByteArrays(() -> {
      PartsAsBytesIterator partsAsBytesIterator = new PartsAsBytesIterator();
      partsAsBytesIterators.add(partsAsBytesIterator); // store for cleanup
      return partsAsBytesIterator;
    });
  }

  public String getBoundary() {
    return boundary;
  }

  public MultiPartBodyPublisher addPart(String name, String value) {
    return addPart(new StringPart(name, value));
  }

  public MultiPartBodyPublisher addPart(String name, Path path, String filename, String contentType) {
    validState(Files.exists(path), "File does not exist");
    return this.addPart(new FilePart(name, path, filename, contentType))
               .addPart(new FileContentPart(path))
      .addPart(new LineBreakPart());
  }

  private MultiPartBodyPublisher addPart(Part e) {
    partsList.add(e);
    return this;
  }

  private void addEndOfMultiPartPart() {
    addPart(new FinalBoundaryPart());
  }

  @Override
  public void close() throws IOException {
    // iterate over a copy to avoid ConcurrentModificationException
    for (PartsAsBytesIterator partsAsBytesIterator : List.copyOf(partsAsBytesIterators)) {
      partsAsBytesIterator.close();
    }
  }

  private static void validState(boolean state, String message) {
    if (!state) {
      throw new IllegalStateException(message);
    }
  }

  private abstract class Part {
    abstract InputStream asStream() throws IOException;

    InputStream fromString(String value) {
      return new ByteArrayInputStream(value.getBytes(charset));
    }
  }

  private final class StringPart extends Part {
    private final String name;
    private final String value;

    public StringPart(String name, String value) {
      this.name = name;
      this.value = value;
    }

    @Override
    InputStream asStream() {
      String part =
        "--" + boundary + CRLF +
          "Content-Disposition: form-data; name=\"" + name + "\"" + CRLF +
          "Content-Type: text/plain; charset=" + charset.displayName() + CRLF + CRLF +
          value + CRLF;
      return fromString(part);
    }
  }

  private final class FilePart extends Part {
    private final String name;
    private final Path path;
    private final String filename;
    private final String contentType;

    public FilePart(String name, Path path, String filename, String contentType) {
      this.name = name;
      this.path = path;
      this.filename = filename;
      this.contentType = contentType;
    }

    @Override
    InputStream asStream() throws IOException {
      final String realContentType = contentType != null ? contentType : Files.probeContentType(path);
      final String realFilename = filename != null ? filename : Objects.toString(path.getFileName());
      String partHeader =
        "--" + boundary + CRLF +
          "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + realFilename + "\"" + CRLF +
          "Content-Type: " + realContentType + CRLF + CRLF;
      return fromString(partHeader);
    }
  }

  private final class FileContentPart extends Part {
    private final Path path;

    public FileContentPart(final Path path) {
      this.path = path;
    }

    @Override
    InputStream asStream() throws IOException {
      return new BufferedInputStream(Files.newInputStream(path));
    }
  }

  private final class FinalBoundaryPart extends Part {
    @Override
    InputStream asStream() {
      return fromString("--" + boundary + "--");
    }
  }

  private class LineBreakPart extends Part {
    @Override
    InputStream asStream() {
      return fromString(CRLF);
    }
  }

  private class PartsAsBytesIterator implements Iterator<byte[]>, Closeable {
    private final Iterator<Part> partIterator = partsList.iterator();
    private final byte[] buffer = new byte[4096];
    private PushbackInputStream currentStream = null;

    @Override
    public boolean hasNext() {
      if (currentStream != null) {
        try {
          final int read = currentStream.read();
          if (read >= 0) {
            currentStream.unread(read);
            return true;
          }
        } catch (IOException e) {
          throw new RuntimeIOException("IOException while generating multi parts: " + e.getMessage(), e);
        }
      }
      return partIterator.hasNext();
    }

    @Override
    public byte[] next() {
      try {
        if (currentStream != null) {
          final int read = currentStream.read(buffer);
          if (read >= 0) {
            return Arrays.copyOf(buffer, read);
          }
          closeCurrentStream();
        }
        if (partIterator.hasNext()) {
          final Part nextPart = partIterator.next();
          currentStream = new PushbackInputStream(nextPart.asStream());
          return next();
        }
        throw new NoSuchElementException();
      } catch (IOException e) {
        throw new RuntimeIOException("IOException while generating multi parts: " + e.getMessage(), e);
      }
    }

    private void closeCurrentStream() throws IOException {
      if (currentStream != null) {
        currentStream.close();
        currentStream = null;
      }
    }

    @Override
    public void close() throws IOException {
      partsAsBytesIterators.remove(this);
      closeCurrentStream();
    }
  }
}