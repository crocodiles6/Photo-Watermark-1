package com.photowatermark;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 专门负责处理图片和添加水印的类
 */
public class WatermarkProcessor {
    
    /**
     * 处理图片并添加水印
     */
    public static void processImage(
            File imageFile, 
            String watermarkText, 
            int fontSize, 
            Color color, 
            Position position
    ) throws IOException {
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(imageFile);
        
        // 创建可绘制的图像副本
        BufferedImage watermarkedImage = new BufferedImage(
                originalImage.getWidth(), 
                originalImage.getHeight(), 
                BufferedImage.TYPE_INT_RGB);
        
        // 获取图形上下文
        Graphics2D g2d = watermarkedImage.createGraphics();
        
        // 绘制原始图像
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        
        // 设置字体
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);
        
        // 设置颜色
        g2d.setColor(color);
        
        // 计算水印位置
        int[] positionXY = calculateWatermarkPosition(
                originalImage, 
                g2d, 
                watermarkText, 
                position
        );
        int x = positionXY[0];
        int y = positionXY[1];
        
        // 绘制水印
        g2d.drawString(watermarkText, x, y);
        
        // 释放资源
        g2d.dispose();
        
        // 创建保存目录
        File outputDir = createOutputDirectory(imageFile);
        
        // 保存水印图片
        saveWatermarkedImage(
                watermarkedImage, 
                outputDir, 
                imageFile.getName()
        );
    }
    
    /**
     * 计算水印在图片上的位置
     */
    private static int[] calculateWatermarkPosition(
            BufferedImage image, 
            Graphics2D g2d, 
            String text, 
            Position position
    ) {
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int textHeight = g2d.getFontMetrics().getHeight();
        int x = 0, y = 0;
        int margin = 10;
        
        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = textHeight + margin;
                break;
            case TOP_CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = textHeight + margin;
                break;
            case TOP_RIGHT:
                x = image.getWidth() - textWidth - margin;
                y = textHeight + margin;
                break;
            case CENTER_LEFT:
                x = margin;
                y = (image.getHeight() + textHeight) / 2;
                break;
            case CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = (image.getHeight() + textHeight) / 2;
                break;
            case CENTER_RIGHT:
                x = image.getWidth() - textWidth - margin;
                y = (image.getHeight() + textHeight) / 2;
                break;
            case BOTTOM_LEFT:
                x = margin;
                y = image.getHeight() - margin;
                break;
            case BOTTOM_CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = image.getHeight() - margin;
                break;
            case BOTTOM_RIGHT:
            default:
                x = image.getWidth() - textWidth - margin;
                y = image.getHeight() - margin;
                break;
        }
        
        return new int[] {x, y};
    }
    
    /**
     * 创建输出目录
     */
    private static File createOutputDirectory(File imageFile) {
        File parentDir = imageFile.getParentFile();
        String dirName = parentDir.getName() + "_watermark";
        File outputDir = new File(parentDir, dirName);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        return outputDir;
    }
    
    /**
     * 保存水印图片
     */
    private static void saveWatermarkedImage(
            BufferedImage watermarkedImage, 
            File outputDir, 
            String fileName
    ) throws IOException {
        String formatName = getFormatName(fileName);
        File outputFile = new File(outputDir, fileName);
        ImageIO.write(watermarkedImage, formatName, outputFile);
        
        System.out.println("水印图片已保存至：" + outputFile.getAbsolutePath());
    }
    
    /**
     * 获取图片格式名称
     */
    private static String getFormatName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "jpg";
    }
    
    /**
     * 水印位置枚举
     */
    public enum Position {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        CENTER_LEFT,
        CENTER,
        CENTER_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }
}