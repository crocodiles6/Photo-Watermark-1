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
    public void exportWatermarkedImage(BufferedImage watermarkedImage, File outputFile, ImageFile selectedImageFile, int scalePercentage) {
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
            
            // 先进行缩放
            BufferedImage scaledImage = scaleImage(watermarkedImage, scalePercentage);
            
            // 根据目标格式进行必要的格式转换
            BufferedImage imageToExport = convertImageFormatIfNeeded(scaledImage, extension);
            
            // 写入文件
            ImageIO.write(imageToExport, extension, outputFile);
            uiUtils.showInfo("导出成功", "图片已成功导出到：" + outputFile.getAbsolutePath());
            uiUtils.updateStatus("已导出图片：" + outputFile.getName());
        } catch (IOException e) {
            uiUtils.showError("导出失败", "无法导出图片：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 按照指定百分比缩放图片
     */
    private BufferedImage scaleImage(BufferedImage source, int scalePercentage) {
        if (scalePercentage == 100) {
            // 如果是100%，直接返回原图，避免不必要的处理
            return source;
        }
        
        // 计算新尺寸
        int newWidth = (int) (source.getWidth() * scalePercentage / 100.0);
        int newHeight = (int) (source.getHeight() * scalePercentage / 100.0);
        
        // 创建缩放后的图片
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, source.getType());
        
        // 绘制并缩放
        java.awt.Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                          java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return scaledImage;
    }
    
    /**
     * 根据目标格式转换图片格式（必要时）
     * 特别是处理透明通道的情况
     */
    private BufferedImage convertImageFormatIfNeeded(BufferedImage source, String format) {
        // 如果是JPEG格式且原图有alpha通道，需要转换为RGB模式
        if ("JPEG".equals(format) && source.getColorModel().hasAlpha()) {
            BufferedImage rgbImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            // 绘制白色背景
            rgbImage.createGraphics().drawImage(source, 0, 0, null);
            return rgbImage;
        }
        // 其他情况直接返回原图
        return source;
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
    
    /**
     * 生成带指定扩展名的文件名
     */
    public String generateFileNameWithExtension(String baseName, String extension) {
        if (baseName == null || baseName.trim().isEmpty()) {
            // 如果没有指定基本文件名，使用默认名
            return "watermarked_image." + extension.toLowerCase();
        }
        
        // 确保扩展名前有.
        if (!extension.startsWith(".")) {
            extension = "." + extension.toLowerCase();
        }
        
        // 如果文件名已经包含相同扩展名，直接返回
        if (baseName.toLowerCase().endsWith(extension)) {
            return baseName;
        }
        
        // 移除已有的扩展名（如果有）并添加新扩展名
        int lastDotIndex = baseName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            baseName = baseName.substring(0, lastDotIndex);
        }
        
        return baseName + extension;
    }
}