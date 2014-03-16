/*
 * This file is part of the FZPWUploader
 *
 * Copyright (C) 2009-2014 achterblog.de
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
 * along with FZPWUploader.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.achterblog.fzpwuploader;

import de.achterblog.util.Streams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common code that is shared between all UploadConnections that use HTTP.
 *
 * @author boris
 */
@ThreadSafe
public abstract class BaseHttpUploadConnection implements UploadConnection {
  private final Logger logger = LoggerFactory.getLogger(BaseHttpUploadConnection.class);
  private final Charset charset;

  /**
   * @param charset the charset to be used for this connection
   * @throws java.lang.NullPointerException If {@code charset == null}
   */
  public BaseHttpUploadConnection(Charset charset) throws NullPointerException {
    if (charset == null) {
      throw new NullPointerException("charset");
    }
    this.charset = charset;
  }

  /**
   * Read the response of any HttpMethod.
   *
   * Has a proper handling of connection-releasing and closing.
   *
   * @param method The method that should be read from (must be fully configured)
   * @return The response of the method
   * @throws IOException Passed through from InputStream of the Method
   * @throws NullPointerException If {@code method == null}
   * @throws UploadException If the return was not "200 OK"
   */
  protected String readFromHttpMethod(HttpClient client, HttpMethod method) throws IOException, UploadException {
    try {
      int status = client.executeMethod(method);
      logger.debug("URL {} returned {}", method.getURI(), status);
      if (status != HttpStatus.SC_OK) {
        throw new UploadException("Unexpected http-return code: " + status);
      }

      try (InputStream responseInputStream = method.getResponseBodyAsStream()) {
        return Streams.toString(responseInputStream, charset);
      }
    } catch (IOException e) {
      method.abort();
      throw e;
    } finally {
      method.releaseConnection();
    }
  }
}
