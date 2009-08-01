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
package de.achterblog.fzpwuploader.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPanel that paints previews of images that are submitted via this classes
 * PropertyChangeListener-methods.
 *
 * @author boris
 */
public class ImagePreviewAccessory extends JPanel implements PropertyChangeListener {
  private static final int MAX_SIZE = 160;
  private static final Pattern imageFilename = Pattern.compile("\\.(png|jpe?g|gif)$", Pattern.CASE_INSENSITIVE);
  private static final Logger logger = LoggerFactory.getLogger(ImagePreviewAccessory.class);
  private transient volatile Image previewImage;
  /** A flag that show if the image changed from the last-rendering */
  private final AtomicBoolean paintNeedsClear = new AtomicBoolean(true);

  public ImagePreviewAccessory() {
    Dimension max = new Dimension(MAX_SIZE, MAX_SIZE);
    setPreferredSize(max);
    setMaximumSize(max);
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(event.getPropertyName())) {
      File file = (File) event.getNewValue();

      if (file == null) {
        logger.info("Multiple properties changed, cannot determine the file to preview");
        return;
      }

      if (imageFilename.matcher(file.getName()).find()) {
        Image image = null;
        try {
          image = ImageIO.read(file);
        } catch (IOException e) {
          logger.info("IOException reading " + file, e);
        }

        previewImage = scale(image);
      } else {
        previewImage = null;
      }

      paintNeedsClear.set(true);
      repaint();
    }
  }

  @Override
  public void paintComponent(Graphics graphic) {
    // clear our rect if the image changed (avoids artifacts of the old image)
    if (paintNeedsClear.compareAndSet(true, false)) {
      graphic.setColor(getBackground());
      graphic.fillRect(0, 0, MAX_SIZE, MAX_SIZE);
    }

    Image currentPreviewImage = previewImage; // make sure we use the same image in the next steps
    if (currentPreviewImage != null) {
      int width = getWidth() / 2 - currentPreviewImage.getWidth(this) / 2;
      int height = getHeight() / 2 - currentPreviewImage.getHeight(this) / 2;
      graphic.drawImage(currentPreviewImage, width, height, this);
    }
  }

  private Image scale(Image image) {
    if (image == null) {
      return null;
    }

    int width = image.getWidth(this);
    int height = image.getHeight(this);
    double ratio = (double) Math.max(width, height) / Math.min(width, height);

    // scale the sizes
    int smallerSide = (int) (MAX_SIZE / ratio);
    if (width < height) {
      // upright
      height = MAX_SIZE;
      width = smallerSide;
    } else if (width > height) {
      width = MAX_SIZE;
      height = smallerSide;
    } else {
      width = height = MAX_SIZE;
    }


    return image.getScaledInstance(width, height, Image.SCALE_FAST);
  }
}
