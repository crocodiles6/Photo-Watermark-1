package com.photowatermark.gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;

/**
 * 图片转换器类，负责AWT和JavaFX图片格式之间的转换
 */
public class ImageConverter {
    /**
     * 将AWT BufferedImage转换为JavaFX Image
     */
    public Image convertToFxImage(BufferedImage bufferedImage) {
        WritableImage writableImage = new WritableImage(
                bufferedImage.getWidth(), bufferedImage.getHeight());
        return SwingFXUtils.toFXImage(bufferedImage, writableImage);
    }
}