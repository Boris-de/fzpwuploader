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
package de.achterblog.util;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import de.achterblog.util.log.Level;
import de.achterblog.util.log.Logger;

public class ApplicationProperties {
  private final String version;

  public static final ApplicationProperties INSTANCE = new ApplicationProperties();

  public ApplicationProperties() {
    this("/application.properties");
  }

  ApplicationProperties(final String resourceName) {
    final Properties tmp = new Properties();
    try (InputStream in = ApplicationProperties.class.getResourceAsStream(resourceName)) {
      tmp.load(Objects.requireNonNull(in, "Resources is missing"));
    } catch (Exception e) {
      Logger.log(Level.ERROR, () -> "Failed to read " + resourceName, e);
    }
    version = tmp.getProperty("version", "unknown");
  }

  public String getVersion() {
    return version;
  }
}
