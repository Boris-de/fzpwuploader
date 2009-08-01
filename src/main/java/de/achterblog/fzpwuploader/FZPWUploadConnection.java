/*
 * This file is part of the FZPWUploader
 *
 * Copyright (C) 2009 achterblog.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.achterblog.fzpwuploader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the upload for Freizeitparkweb.de
 *
 * @author boris
 */
@NotThreadSafe
public class FZPWUploadConnection extends BaseHttpUploadConnection {
  private final Logger logger = LoggerFactory.getLogger(FZPWUploadConnection.class);
  private HttpClient client;
  private LoginStatus loginStatus = LoginStatus.DISCONNECTED;
  private final HttpState state = new HttpState();
  private final String baseUrl;

  public FZPWUploadConnection() {
    this("http://freizeitparkweb.de/cgi-bin/dcf/dcboard.cgi");
  }

  /** Package private constructor for the test-cases */
  FZPWUploadConnection(String baseUrl) {
    super(Charset.forName("iso-8859-1"));
    this.baseUrl = baseUrl;
  }

  @Override
  public LoginStatus login(String user, String password) throws IOException, UploadException {
    if (loginStatus == LoginStatus.LOGGED_IN) {
      throw new IllegalStateException("Cannot login twice");
    }

    client = new HttpClient();
    client.setState(state);
    PostMethod post = new PostMethod(baseUrl + "?az=login");

    NameValuePair[] data = {
      new NameValuePair("cmd", "login"),
      new NameValuePair("az", "login"),
      new NameValuePair("Username", user),
      new NameValuePair("Password", password)
    };
    post.setRequestBody(data);
    post.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    String response = readFromHttpMethod(client, post);
    loginStatus = LoginStatus.UNKNOWN;
    if (response.contains("Seite wird geladen, einen Moment bitte...")) {
      loginStatus = LoginStatus.LOGGED_IN;
    }
    if (response.contains("Login Problem: Falscher Username")) {
      loginStatus = LoginStatus.REFUSED;
    }
    logger.debug("LoginStatus for user {}: {}", user, loginStatus);
    return loginStatus;
  }

  @Override
  public String upload(File file) throws FileNotFoundException, IOException, UploadException {
    String url = baseUrl + "?az=upload_file&forum=";
    PostMethod post = new PostMethod(url);
    post.addRequestHeader("Referer", url);
    Part[] parts = {
      new StringPart("az", "upload_file"),
      new StringPart("command", "save"),
      new FilePart("file_upload", new FilePartSource(file), "image/jpeg", null),
      new StringPart("file_type", "jpg"),};
    post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

    try {
      String response = readFromHttpMethod(client, post);

      String patStr = "http://Freizeitparkweb.de/dcf/User_files/[\\da-f]+.jpg";
      Matcher m = Pattern.compile(patStr, Pattern.CASE_INSENSITIVE).matcher(response);
      if (!m.find()) {
        logger.info("The servers-response was:\n" + response);
        throw new UploadException("Could not find URL in the response");
      }
      String uploadedUrl = m.group(0);
      logger.info("Sucessfully uploaded file " + file.getName() + " to " + uploadedUrl);
      return uploadedUrl;
    } catch (IOException e) {
      throw new UploadException("Failed to upload file " + file.getName(), e);
    }
  }

  @Override
  public boolean logout() {
    try {
      GetMethod get = new GetMethod(baseUrl + "?az=logout");
      String response = readFromHttpMethod(client, get);
      return response.contains("Der User wurde auf diesem Rechner ausgeloggt...");
    } catch (UploadException e) {
      logger.info("Exception while logging out", e);
      return false;
    } catch (IOException e) {
      logger.info("Exception while logging out", e);
      return false;
    } finally {
      client = null;
      loginStatus = LoginStatus.LOGGED_OUT;
    }
  }

  @Override
  public void disconnect() {
    state.clear();
    loginStatus = LoginStatus.DISCONNECTED;
  }

  @Override
  public LoginStatus getLoginStatus() {
    return loginStatus;
  }
}
