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
package de.achterblog.fzpwuploader;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.UploadContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Wraps a HttpServletRequest into an UploadContext.
 *
 * (commons-fileupload does not support the jakarta-rename of the servlet classes)
 */
final class JakartaRequestContext implements UploadContext {
  private final HttpServletRequest req;

  JakartaRequestContext(HttpServletRequest req) {
    this.req = req;
  }

  @Override
  public String getCharacterEncoding() {
    return req.getCharacterEncoding();
  }

  @Override
  public String getContentType() {
    return req.getContentType();
  }

  @Override
  @Deprecated
  public int getContentLength() {
    return req.getContentLength();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return req.getInputStream();
  }

  @Override
  public long contentLength() {
    return req.getContentLengthLong();
  }
}
