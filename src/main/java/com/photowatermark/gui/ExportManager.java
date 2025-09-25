package com.photowatermark.gui;

import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 导出管理器 - 负责处理图片的导出功能
 */
public class ExportManager {
    private final UiUtils uiUtils;
    private final ImageConverter imageConverter;
    
    public ExportManager(UiUtils uiUtils, ImageConverter imageConverter) {
        this.uiUtils = uiUtils;
        this.imageConverter = imageConverter;
    }
    
    /**
     * 导出带有水印的图片
     */
    public void exportWatermarkedImage(BufferedImage watermarkedImage, File outputFile, ImageFile selectedImageFile) {
        if (watermarkedImage == null) {
            uiUtils.showWarning("无水印图片", "请先应用水印");
            return;
        }
        
        try {
            // 获取文件扩展名
            String extension = outputFile.getName().substring(
                    outputFile.getName().lastIndexOf('.') + 1).toUpperCase();
            if ("JPG".equals(extension)) {
                extension = "JPEG";
            }
            
            // 写入文件
            ImageIO.write(watermarkedImage, extension, outputFile);
            uiUtils.showInfo("导出成功", "图片已成功导出到：" + outputFile.getAbsolutePath());
            uiUtils.updateStatus("已导出图片：" + outputFile.getName());
        } catch (IOException e) {
            uiUtils.showError("导出失败", "无法导出图片：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 根据原始图片生成默认的导出文件名
     */
    public String generateDefaultExportFileName(ImageFile selectedImageFile) {
        if (selectedImageFile != null) {
            String originalName = selectedImageFile.getFileName();
            String nameWithoutExt = originalName.substring(0, originalName.lastIndexOf('.'));
            return nameWithoutExt + "_watermark.png";
        } else {
            return "watermarked_image.png";
        }
    }
}