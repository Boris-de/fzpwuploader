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
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Jimfs;

import de.achterblog.fzpwuploader.UploadConnection.LoginStatus;
import de.achterblog.util.Streams;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class FZPWUploadConnectionTest {
  private static final String URLPART = "/dc-test";
  private static String baseTestUrl;
  private static ServletTester tester;
  private FZPWUploadConnection connection;
  private static volatile String nextResponse;
  private static volatile Map<String, String> lastRequestParameters;
  private static volatile List<FileItem> lastFileItems;
  private static FileSystem inMemoryfileSystem;

  @BeforeClass
  public static void setUpClass() throws Exception {
    inMemoryfileSystem = Jimfs.newFileSystem();

    tester = new ServletTester();
    tester.setContextPath("/");
    tester.addServlet(TestServlet.class, URLPART);
    baseTestUrl = tester.createConnector(true) + URLPART;
    tester.start();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    tester.stop();
  }

  @Before
  public void setUp() {
    connection = new FZPWUploadConnection(baseTestUrl);
    nextResponse = "";
    lastRequestParameters = Collections.emptyMap();
  }

  @After
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
    assertEquals(user, lastRequestParameters.get("Username"));
    assertEquals(password, lastRequestParameters.get("Password"));

    connection.logout();
    assertThat(connection.getLoginStatus(), is(LoginStatus.LOGGED_OUT));
    nextResponse = "Login Problem: Falscher Username";
    assertThat(connection.login(user, password), is(LoginStatus.REFUSED));

    connection.logout();
    nextResponse = "xxx";
    assertThat(connection.login(user, password), is(LoginStatus.UNKNOWN));

    connection.disconnect();
    assertThat(connection.getLoginStatus(), is(LoginStatus.DISCONNECTED));
  }

  @Test(expected = IllegalStateException.class)
  public void loginTwice() throws Exception {
    nextResponse = "Seite wird geladen, einen Moment bitte...";
    connection.login("", "");
    connection.login("", "");
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
        case "az":
          assertEquals("upload_file", cur.getString());
          break;
        case "command":
          assertEquals("save", cur.getString());
          break;
        case "file_type":
          assertEquals("jpg", cur.getString());
          break;
        case "file_upload":
          assertArrayEquals(fileContents, Streams.toBytes(cur.getInputStream()));
          break;
        default:
          fail("Uexpected item: " + fieldName);
          break;
      }
    }
  }

  @Test(expected = UploadException.class)
  public void testUploadFindsNoURL() throws Exception {
    final Path testFile = inMemoryfileSystem.getPath("testUploadFindsNoURL.test");
    try (OutputStream out = Files.newOutputStream(testFile)) {
      out.flush();
    }
    nextResponse = "Seite wird geladen, einen Moment bitte...";
    connection.login("", "");
    connection.upload(testFile);
  }

  public static final class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      doPost(req, resp);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      if (req.getContentType() != null && req.getContentType().startsWith("multipart/form-data")) {
        try {
          FileItemFactory factory = new DiskFileItemFactory();
          ServletFileUpload upload = new ServletFileUpload(factory);
          lastFileItems = upload.parseRequest(req);
        } catch (FileUploadException e) {
          LoggerFactory.getLogger(FZPWUploadConnectionTest.class).warn("", e);
        }
      } else {
        lastFileItems = Collections.emptyList();
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