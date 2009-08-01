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

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Priority;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Basic interface for classes that upload a file to a login-secured place
 *
 * @author boris
 */
public interface UploadConnection {
  /** The return value for {@link #login(java.lang.String, java.lang.String) login} */
  public enum LoginStatus {
    /** The login was successful and the Connection can be used to upload. */
    LOGGED_IN,
    /** The user was once logged in, but is now logged out on his own request */
    LOGGED_OUT,
    /** The login was refused by the server (wrong password, invalid user, ...) */
    REFUSED,
    /** The login-status could not be recognized */
    UNKNOWN,
    /** The connection is disconnected (can be before or after a connection) */
    DISCONNECTED;
  }

  /** Connect to the server with this username and password
   *
   * @param user The user that should be logged in
   * @param password The password for this user
   * @return The status of the login, see {@link LoginStatus}
   * @throws java.io.IOException Can be forwared if it occurs within the login-process
   * @throws IllegalStateException If the connection is already logged in
   */
  @CheckReturnValue(priority = Priority.LOW, explanation = "You should always check if the login was successful or unknown. And handle the unknown-status")
  public LoginStatus login(@NonNull String user, @NonNull String password) throws UploadException, IOException, IllegalStateException;

  /**
   * Upload this file
   *
   * @param file The file to be uploaded
   * @return The URL of the uploaded file. Should never return null, instead throw an exception
   * @throws UploadException If something goes wrong in the upload
   * @throws java.io.FileNotFoundException If the file to be uploaded does not exist
   * @throws java.lang.IllegalStateException If the user was not logged in
   * @throws java.io.IOException Can be forwared if it occurs within the upload
   */
  public String upload(@NonNull File file) throws FileNotFoundException, UploadException, IOException, IllegalStateException;

  /**
   * Logout from the server.
   *
   * Calling this method more than once should be possible and it must be possible to call it with
   * any current loginStatus.
   *
   * @return {@code true} if successful, {@code false} if an error happend (aside from an
   *         Exception, e.g. the answer was not unknown to the implementation)
   * @throws java.io.IOException Can be forwarded if the logout throws this
   */
  @CheckReturnValue(priority = Priority.LOW, explanation = "If the logout fails the user should be informed")
  public boolean logout();

  /** Completely disconnect from the server. May close connections, wipe login-data etc. */
  public void disconnect();

  /** Return the current LoginStatus of this connection */
  public LoginStatus getLoginStatus();
}
