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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.achterblog.fzpwuploader.UploadConnection.LoginStatus;
import de.achterblog.util.log.Level;
import de.achterblog.util.log.Logger;

/**
 * Logs in, uploads multiple files in a single batch and logs out.
 *
 * @author boris
 */
public record UploadBatch(String username, String password, UploadBatchCallback callback) {

  public String upload(Iterable<Path> fileList) {
    final UploadConnection con = new FZPWUploadConnection();
    final StringBuilder buffer = new StringBuilder(512);

    try (final ExecutorService exe = Executors.newSingleThreadExecutor()) {
      final LoginStatus loginStatus = con.login(username, password);
      if (loginStatus != UploadConnection.LoginStatus.LOGGED_IN) {
        return "Failed to login user " + username + ": " + loginStatus;
      }
      final List<UploadFuture> futures = new ArrayList<>();
      for (final Path cur : fileList) {
        Future<String> f = exe.submit(() -> {
          try {
            Logger.log(Level.DEBUG, () -> "Starting upload for file " + cur);
            String result = con.upload(cur);
            callback.uploaded(cur);
            return result;
          } catch (UploadException | IOException | IllegalStateException e) {
            callback.failed(cur);
            throw new UploadException(e.getMessage(), e);
          }
        });
        futures.add(new UploadFuture(cur, f));
      }
      for (UploadFuture cur : futures) {
        try {
          String url = cur.future.get(2, TimeUnit.MINUTES);
          buffer.append(url).append('\n');
          buffer.append(cur.path.getFileName()).append("\n\n");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
          Logger.log(Level.ERROR, "Exception while executing upload: ", e);
          buffer.append("Failed to upload file").append(cur.path.getFileName())
            .append(" (").append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append(")\n\n");
        }
      }
    } catch (UploadException | IOException e) {
      Logger.log(Level.ERROR, "Exception in GUI", e);
    } finally {
      if (!con.logout()) {
        Logger.log(Level.ERROR, "The logout failed, the user may still be logged in");
      }
      con.disconnect();
    }
    return buffer.toString();
  }

  public interface UploadBatchCallback {
    /**
     * Called if a file was uploaded
     *
     * @param uploaded The file that was uploaded
     */
    void uploaded(Path uploaded);

    /**
     * Called if a file-upload failed
     *
     * @param uploaded The file that was <b>not</b> uploaded
     */
    void failed(Path uploaded);
  }

  private record UploadFuture(Path path, Future<String> future) {
  }
}
