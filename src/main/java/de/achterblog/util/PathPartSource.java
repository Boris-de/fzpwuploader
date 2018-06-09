package de.achterblog.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.httpclient.methods.multipart.PartSource;

public class PathPartSource implements PartSource {
  private final Path path;

  public PathPartSource(Path path) {
    this.path = path;
  }

  @Override
  public long getLength() {
    try {
      return Files.size(path);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot get size of path \"" + path + '"');
    }
  }

  @Override
  public String getFileName() {
    final Path fileName = path.getFileName();
    return fileName != null ? fileName.toString() : "noname";
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return Files.newInputStream(path);
  }
}
