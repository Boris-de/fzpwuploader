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

import de.achterblog.fzpwuploader.UploadConnection.LoginStatus;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs in, uploads multiple files in a single batch and logs out.
 *
 * @author boris
 */
public class UploadBatch {
  private final static Logger logger = LoggerFactory.getLogger(UploadBatch.class);
  private final String username;
  private final String password;
  private final UploadBatchCallback callback;

  public UploadBatch(String username, String password, UploadBatchCallback callback) {
    this.username = username;
    this.password = password;
    this.callback = callback;
  }

  public String upload(Iterable<File> fileList) {
    final UploadConnection con = new FZPWUploadConnection();
    final StringBuilder buffer = new StringBuilder(512);

    try {
      LoginStatus loginStatus = con.login(this.username, this.password);
      if (loginStatus != UploadConnection.LoginStatus.LOGGED_IN) {
        return "Failed to login user " + this.username + ": " + loginStatus;
      }
      ExecutorService exe = Executors.newSingleThreadExecutor();
      Map<File, Future<String>> futures = new HashMap<>();
      for (final File cur : fileList) {
        Future<String> f = exe.submit(new Callable<String>() {
          @Override
          public String call() throws Exception {
            try {
              logger.debug("Starting upload for file {}", cur.getName());
              String result = con.upload(cur);
              callback.uploaded(cur);
              return result;
            } catch (UploadException | IOException | IllegalStateException e) {
              callback.failed(cur);
              throw new UploadException(e.getMessage(), e);
            }
          }
        });
        futures.put(cur, f);
      }
      for (Map.Entry<File, Future<String>> cur : futures.entrySet()) {
        try {
          String url = cur.getValue().get();
          buffer.append(url).append('\n');
          buffer.append(cur.getKey().getName()).append("\n\n");
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
          logger.error("Exception while executing upload: ", ex);
        }
      }
    } catch (UploadException e) {
      logger.error("UploadException in GUI", e);
    } catch (IOException e) {
      logger.error("IOException in GUI", e);
    } finally {
      if (!con.logout()) {
        logger.error("The logout failed, the user may still be logged in");
      }
      con.disconnect();
    }
    return buffer.toString();
  }

  public static interface UploadBatchCallback {
    /**
     * Called if a file was uploaded
     *
     * @param uploaded The file that was uploaded
     */
    public void uploaded(File uploaded);

    /**
     * Called if a file-upload failed
     *
     * @param uploaded The file that was <b>not</b> uploaded
     */
    public void failed(File uploaded);
  }
}
