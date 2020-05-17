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
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;

import de.achterblog.fzpwuploader.UploadConnection.LoginStatus;
import de.achterblog.util.log.Level;
import de.achterblog.util.log.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class FZPWUploadConnectionTest {
  private static final String URLPART = "/dc-test";
  private static String baseTestUrl;
  private static ServletTester tester;
  private FZPWUploadConnection connection;
  private static volatile String nextResponse;
  private static volatile Map<String, String> lastRequestParameters;
  private static volatile List<FileItem> lastFileItems;
  private static volatile List<Cookie> lastCookies;
  private static FileSystem inMemoryfileSystem;

  @BeforeAll
  public static void setUpClass() throws Exception {
    inMemoryfileSystem = Jimfs.newFileSystem();

    tester = new ServletTester();
    tester.setContextPath("/");
    tester.addServlet(TestServlet.class, URLPART);
    baseTestUrl = tester.createConnector(true) + URLPART;
    tester.start();
  }

  @AfterAll
  public static void tearDownClass() throws Exception {
    tester.stop();
  }

  @BeforeEach
  public void setUp() {
    connection = new FZPWUploadConnection(baseTestUrl);
    nextResponse = "";
    lastRequestParameters = Collections.emptyMap();
  }

  @AfterEach
  public void tearDown() {
    connection.disconnect();
  }

  @Test
  public void testLoginLogout() throws Exception {
    String user = "Test";
    String password = "password123";
    nextResponse = "Seite wird geladen, einen Moment bitte...";
    LoginStatus result = connection.login(user, password);
    assertThat(result, is(LoginStatus.LOGGED_IN));
    assertThat(connection.getLoginStatus(), is(LoginStatus.LOGGED_IN));
    assertThat(lastRequestParameters.get("Username"), is(user));
    assertThat(lastRequestParameters.get("Password"), is(password));
    assertThat(lastCookies, emptyCollectionOf(Cookie.class));

    connection.logout();
    assertThat(connection.getLoginStatus(), is(LoginStatus.LOGGED_OUT));
    assertThat(lastCookies, hasSize(1));
    assertThat(lastCookies.get(0).getName(), is("DCForumSessionID"));
    assertThat(lastCookies.get(0).getValue(), is("%%abcdefgh"));

    nextResponse = "Login Problem: Falscher Username";
    assertThat(connection.login(user, password), is(LoginStatus.REFUSED));

    connection.logout();
    nextResponse = "xxx";
    assertThat(connection.login(user, password), is(LoginStatus.UNKNOWN));

    connection.disconnect();
    assertThat(connection.getLoginStatus(), is(LoginStatus.DISCONNECTED));
  }

  @Test
  public void loginTwice() throws Exception {
    nextResponse = "Seite wird geladen, einen Moment bitte...";
    connection.login("", "");
    var e = assertThrows(IllegalStateException.class, () -> connection.login("", ""));
    assertThat(e.getMessage(), is("Cannot login twice"));
  }

  @Test
  public void testUpload() throws Exception {
    final byte[] fileContents = new byte[]{(byte) 255, (byte) 216, (byte) 97, (byte) 255, (byte) 217};

    final Path testFile = inMemoryfileSystem.getPath("testUpload.test");
    try (OutputStream out = Files.newOutputStream(testFile)) {
      out.write(fileContents);
    }

    nextResponse = "Seite wird geladen, einen Moment bitte...";
    connection.login("", "");
    nextResponse = "https://Freizeitparkweb.de/dcf/User_files/1234567890abcdef.jpg";
    String uploadedUrl = connection.upload(testFile);
    assertEquals(uploadedUrl, nextResponse);
    assertThat(lastFileItems.size(), is(4));
    for (FileItem cur : lastFileItems) {
      String fieldName = cur.getFieldName();
      if (null != fieldName) switch (fieldName) {
        case "az" -> assertEquals("upload_file", cur.getString());
        case "command" -> assertEquals("save", cur.getString());
        case "file_type" -> assertEquals("jpg", cur.getString());
        case "file_upload" -> assertArrayEquals(fileContents, IOUtils.toByteArray(cur.getInputStream()));
        default -> fail("Unexpected item: " + fieldName);
      }
    }
  }

  @Test
  public void testUploadFindsNoURL() throws Exception {
    final Path testFile = inMemoryfileSystem.getPath("testUploadFindsNoURL.test");
    try (OutputStream out = Files.newOutputStream(testFile)) {
      out.flush();
    }
    nextResponse = "Seite wird geladen, einen Moment bitte...";
    connection.login("", "");
    final var e = assertThrows(UploadException.class, () -> connection.upload(testFile));
    assertThat(e.getMessage(), is("Could not find URL in the response"));
  }

  public static final class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      lastCookies = Optional.ofNullable(req.getCookies()).map(ImmutableList::copyOf).orElse(ImmutableList.of());
      if (req.getContentType() != null && req.getContentType().startsWith("multipart/form-data")) {
        try {
          FileItemFactory factory = new DiskFileItemFactory();
          ServletFileUpload upload = new ServletFileUpload(factory);
          lastFileItems = upload.parseRequest(req);
        } catch (FileUploadException e) {
          Logger.log(Level.ERROR, "FileUploadException", e);
          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "FileUploadException");
        }
      } else {
        lastFileItems = Collections.emptyList();
      }
      if (req.getParameter("Username") != null) {
        resp.addHeader("Set-Cookie", "DCForumSessionID=; expires=Thur, 31-Dec-98 12:00:00 GMT; path=/;");
        resp.addHeader("Set-Cookie", "DCForumSessionID=%%abcdefgh; expires=Mon, 31-Dec-2025 12:00:00 GMT; path=/;");
      }

      lastRequestParameters = new HashMap<>();
      for (Map.Entry<String, String[]> cur : req.getParameterMap().entrySet()) {
        lastRequestParameters.put(cur.getKey(), cur.getValue()[0]);
      }

      resp.getWriter().append(nextResponse);
      resp.getWriter().close();
    }
  }
}