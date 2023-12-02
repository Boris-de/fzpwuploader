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
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MultiPartBodyPublisher implements Closeable {
  private static final String CRLF = "\r\n";

  private final List<Part> partsList = new ArrayList<>();
  @Getter
  private final String boundary;
  private final Charset charset;

  private PartInputStreamEnumeration partInputStreamEnumeration = null;

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
    return HttpRequest.BodyPublishers.ofInputStream(() -> {
      partInputStreamEnumeration = new PartInputStreamEnumeration();
      return new SequenceInputStream(partInputStreamEnumeration);
    });
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
    //noinspection resource
    addPart(new FinalBoundaryPart());
  }

  @Override
  public void close() throws IOException {
    if (partInputStreamEnumeration != null) {
      partInputStreamEnumeration.close();
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

  @RequiredArgsConstructor
  private final class StringPart extends Part {
    private final String name;
    private final String value;

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

  @RequiredArgsConstructor
  private final class FilePart extends Part {
    private final String name;
    private final Path path;
    private final String filename;
    private final String contentType;

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

  @RequiredArgsConstructor
  private final class FileContentPart extends Part {
    private final Path path;

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

  private final class PartInputStreamEnumeration implements Enumeration<InputStream>, Closeable {
    private final Iterator<Part> partIterator = partsList.iterator();
    private InputStream currentStream = null;

    @Override
    public boolean hasMoreElements() {
      return partIterator.hasNext();
    }

    @Override
    public InputStream nextElement() {
      try {
        close();
        currentStream = partIterator.next().asStream();
        return currentStream;
      } catch (IOException e) {
        throw new UncheckedIOException("Failed to open stream for next part: " + e.getMessage(), e);
      }
    }

    @Override
    public void close() throws IOException {
      if (currentStream != null) {
        currentStream.close();
        currentStream = null;
      }
    }
  }
}