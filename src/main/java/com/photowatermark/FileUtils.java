package com.photowatermark;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 文件工具类 - 专门负责文件相关的操作
 */
public class FileUtils {
    
    /**
     * 创建输出目录
     */
    public static File createOutputDirectory(File imageFile) {
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
    public static void saveWatermarkedImage(
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
    public static String getFormatName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "jpg";
    }
    
    /**
     * 读取图片文件
     */
    public static BufferedImage readImage(File file) throws IOException {
        return ImageIO.read(file);
    }
}