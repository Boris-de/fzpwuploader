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
 * along with FZPWUploader.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.achterblog.fzpwuploader;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.achterblog.util.MultiPartBodyPublisher;
import de.achterblog.util.RuntimeIOException;
/**
 * Implementation of the upload for Freizeitparkweb.de
 *
 * @author boris
 */
//@NotThreadSafe
@ParametersAreNonnullByDefault
public class FZPWUploadConnection implements UploadConnection {
  private final Logger logger = LoggerFactory.getLogger(FZPWUploadConnection.class);

  private static final Charset FZPW_CHARSET = StandardCharsets.ISO_8859_1;
  public static final Pattern UPLOAD_FILE_NAME_PATTERN = Pattern.compile("https?://Freizeitparkweb.de/dcf/User_files/[\\da-f]+.jpg", Pattern.CASE_INSENSITIVE);

  private final String baseUrl;
  private HttpClient client;
  private LoginStatus loginStatus = LoginStatus.DISCONNECTED;

  public FZPWUploadConnection() {
    this("https://freizeitparkweb.de/cgi-bin/dcf/dcboard.cgi");
  }

  /** Package private constructor for the test-cases */
  FZPWUploadConnection(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @Nonnull
  @Override
  public LoginStatus login(String user, String password) throws IOException, UploadException {
    if (loginStatus == LoginStatus.LOGGED_IN) {
      throw new IllegalStateException("Cannot login twice");
    }

    client = HttpClient.newBuilder()
      .cookieHandler(new CookieManager())
      .build();
    final Map<String, String> loginParameters = Map.of("cmd", "login",
                                                       "az", "login",
                                                       "Username", user,
                                                       "Password", password);
    final HttpRequest.BodyPublisher loginRequest = makeFormEncodedRequest(loginParameters);

    final URI loginUri = makeUrl("?az=login");
    final HttpResponse<String> response = sendRequest(HttpRequest.newBuilder(loginUri)
                                                        .header("Content-Type", "application/x-www-form-urlencoded")
                                                        .POST(loginRequest));
    final String body = response.body();
    loginStatus = LoginStatus.UNKNOWN;
    if (body.contains("Seite wird geladen, einen Moment bitte...")) {
      loginStatus = LoginStatus.LOGGED_IN;
    }
    if (body.contains("Login Problem: Falscher Username")) {
      loginStatus = LoginStatus.REFUSED;
    }
    logger.debug("LoginStatus for user {}: {}", user, loginStatus);
    return loginStatus;
  }

  @Nonnull
  @Override
  public String upload(final Path file) throws IOException, UploadException {
    final URI url = makeUrl("?az=upload_file&forum=");

    try (final MultiPartBodyPublisher bodyPublisher = new MultiPartBodyPublisher(FZPW_CHARSET)) {
      bodyPublisher.addPart("az", "upload_file")
                   .addPart("command", "save")
                   .addPart("file_upload", file, null, "image/jpeg")
                   .addPart("file_type", "jpg");
      final HttpResponse<String> response = sendRequest(HttpRequest.newBuilder(url)
                                                          .header("Referer", url.toString())
                                                          .header("Content-Type", "multipart/form-data; boundary=" + bodyPublisher.getBoundary())
                                                          .POST(bodyPublisher.build()));

      final Matcher matcher = UPLOAD_FILE_NAME_PATTERN.matcher(response.body());
      if (!matcher.find()) {
        logger.info("The servers-response was:\n{}", response);
        throw new UploadException("Could not find URL in the response");
      }
      final String uploadedUrl = matcher.group(0);
      logger.info("Successfully uploaded file {} to {}", file.getFileName(), uploadedUrl);
      return uploadedUrl;
    }
  }

  @Override
  public boolean logout() {
    try {
      URI get = makeUrl("?az=logout");
      final HttpResponse<String> response = sendRequest(HttpRequest.newBuilder(get));
      return response.body().contains("Der User wurde auf diesem Rechner ausgeloggt...");
    } catch (UploadException | IOException e) {
      logger.info("Exception while logging out", e);
      return false;
    } finally {
      client = null;
      loginStatus = LoginStatus.LOGGED_OUT;
    }
  }

  @Override
  public void disconnect() {
    loginStatus = LoginStatus.DISCONNECTED;
  }

  @Nonnull
  @Override
  public LoginStatus getLoginStatus() {
    return loginStatus;
  }

  private HttpResponse<String> sendRequest(HttpRequest.Builder requestBuilder) throws IOException, UploadException {
    try {
      final HttpRequest request = requestBuilder
        .header("User-Agent", "fzpwuploader")
        .timeout(Duration.ofSeconds(30))
        .build();
      final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(FZPW_CHARSET));
      final int status = response.statusCode();
      logger.debug("URL {} returned {}", request.uri(), status);
      if (status != HttpURLConnection.HTTP_OK) {
        throw new UploadException("Unexpected http-return code: " + status);
      }
      return response;
    } catch (RuntimeIOException e) {
      throw e.getCause();
    } catch (InterruptedException e) {
      throw new InterruptedIOException(e.getMessage());
    }
  }

  private URI makeUrl(String s) {
    return URI.create(baseUrl + s);
  }

  private HttpRequest.BodyPublisher makeFormEncodedRequest(Map<String, String> data) {
    final String bodyString = data.entrySet().stream()
      .map(it -> it.getKey() + '='+ URLEncoder.encode(it.getValue(), FZPW_CHARSET))
      .collect(Collectors.joining("&"));
    return HttpRequest.BodyPublishers.ofString(bodyString);
  }
}
