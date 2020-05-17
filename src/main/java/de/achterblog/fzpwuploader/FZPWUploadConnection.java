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
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.achterblog.util.ApplicationProperties;
import de.achterblog.util.MultiPartBodyPublisher;
import de.achterblog.util.RuntimeIOException;
import de.achterblog.util.log.Level;
import de.achterblog.util.log.Logger;

/**
 * Implementation of the upload for Freizeitparkweb.de
 *
 * @author boris
 */
//@NotThreadSafe
public class FZPWUploadConnection implements UploadConnection {
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

  @Override
  public LoginStatus login(String user, String password) throws IOException, UploadException {
    if (loginStatus == LoginStatus.LOGGED_IN) {
      throw new IllegalStateException("Cannot login twice");
    }

    client = HttpClient.newBuilder()
      .cookieHandler(new CookieManager(new SingleOriginCookieStore(), CookiePolicy.ACCEPT_ORIGINAL_SERVER))
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
    Logger.log(Level.DEBUG, () ->"LoginStatus for user " + user + ": " + loginStatus);
    return loginStatus;
  }

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
        Logger.log(Level.INFO, () -> "The server's response was " + response.statusCode() + ":\n" + response.body());
        throw new UploadException("Could not find URL in the response");
      }
      final String uploadedUrl = matcher.group(0);
      Logger.log(Level.INFO, () -> "Successfully uploaded file " + file.getFileName() + " to " + uploadedUrl);
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
      Logger.log(Level.WARN, "Exception while logging out", e);
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

  @Override
  public LoginStatus getLoginStatus() {
    return loginStatus;
  }

  private HttpResponse<String> sendRequest(HttpRequest.Builder requestBuilder) throws IOException, UploadException {
    try {
      final HttpRequest request = requestBuilder
        .header("User-Agent", "fzpwuploader/" + ApplicationProperties.INSTANCE.getVersion())
        .timeout(Duration.ofSeconds(30))
        .build();
      final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(FZPW_CHARSET));
      final int status = response.statusCode();
      Logger.log(Level.DEBUG, () -> "URL " + request.uri() + " returned " + status);
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


  private static final class SingleOriginCookieStore implements CookieStore {
    private final ConcurrentMap<String, HttpCookie> cookies = new ConcurrentHashMap<>();

    @Override
    public void add(URI uri, HttpCookie cookie) {
      cookies.put(cookie.getName(), cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
      return getCookies();
    }

    @Override
    public List<HttpCookie> getCookies() {
      return List.copyOf(cookies.values());
    }

    @Override
    public List<URI> getURIs() {
      return null;
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
      return cookies.remove(cookie.getName()) != null;
    }

    @Override
    public boolean removeAll() {
      cookies.clear();
      return true;
    }
  }

}
