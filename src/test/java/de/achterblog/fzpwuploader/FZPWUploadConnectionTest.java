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

import de.achterblog.fzpwuploader.UploadConnection.LoginStatus;
import de.achterblog.util.Streams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class FZPWUploadConnectionTest {
  private static final String URLPART = "/dc-test";
  private static String baseTestUrl;
  private static ServletTester tester;
  private FZPWUploadConnection connection;
  private static volatile String nextResponse;
  private static volatile Map<String, String> lastRequestParameters;
  private static volatile List<Cookie> lastCookies;
  private static volatile List<FileItem> lastFileItems;

  @BeforeClass
  public static void setUpClass() throws Exception {
    tester = new ServletTester();
    tester.setContextPath("/");
    tester.addServlet(TestServlet.class, URLPART);
    baseTestUrl = tester.createSocketConnector(true) + URLPART;
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

    File tempFile = File.createTempFile("test", "tmp");
    OutputStream out = new FileOutputStream(tempFile);
    out.write(fileContents);
    out.close();

    try {
      nextResponse = "Seite wird geladen, einen Moment bitte...";
      connection.login("", "");
      nextResponse = "http://Freizeitparkweb.de/dcf/User_files/1234567890abcdef.jpg";
      String uploadedUrl = connection.upload(tempFile);
      assertEquals(uploadedUrl, nextResponse);
      assertThat(lastFileItems.size(), is(4));
      for (FileItem cur : lastFileItems) {
        String fieldName = cur.getFieldName();
        if ("az".equals(fieldName)) {
          assertEquals("upload_file", cur.getString());
        } else if ("command".equals(fieldName)) {
          assertEquals("save", cur.getString());
        } else if ("file_type".equals(fieldName)) {
          assertEquals("jpg", cur.getString());
        } else if ("file_upload".equals(fieldName)) {
          assertArrayEquals(fileContents, Streams.toBytes(cur.getInputStream()));
        } else {
          fail("Uexpected item: " + fieldName);
        }
      }
    } finally {
      tempFile.delete();
    }
  }

  @Test(expected = UploadException.class)
  public void testUploadFindsNoURL() throws Exception {
    File tempFile = File.createTempFile("test", "tmp");
    try {
      nextResponse = "Seite wird geladen, einen Moment bitte...";
      connection.login("", "");
      connection.upload(tempFile);
    } finally {
      tempFile.delete();
    }
  }

  public static final class TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

      lastCookies = Arrays.asList(req.getCookies() != null ? req.getCookies() : new Cookie[0]);

      lastRequestParameters = new HashMap<String, String>();
      for (Map.Entry<String, String[]> cur : ((Map<String, String[]>) req.getParameterMap()).entrySet()) {
        lastRequestParameters.put(cur.getKey(), cur.getValue()[0]);
      }

      resp.getWriter().append(nextResponse);
      resp.getWriter().close();
    }
  }
}