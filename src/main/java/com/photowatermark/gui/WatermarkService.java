package com.photowatermark.gui;

import com.photowatermark.WatermarkProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;

/**
 * 水印服务类，处理水印相关的核心业务逻辑
 */
public class WatermarkService {
    private final WatermarkProcessor processor;

    public WatermarkService() {
        this.processor = new WatermarkProcessor();
    }

    /**
     * 应用文本水印
     */
    public BufferedImage applyTextWatermark(BufferedImage baseImage, String text, 
                                          java.awt.Color color, String fontFamily, 
                                          int fontSize, String position, double rotation, 
                                          boolean shadow, boolean stroke, boolean tiling, 
                                          boolean useExifDate, ImageFile imageFile) throws IOException {
        // 处理日期水印
        String watermarkText = text;
        if (useExifDate && imageFile != null) {
            try {
                String dateStr = com.photowatermark.ExifExtractor.extractDateTime(imageFile.getFile());
                if (dateStr != null) {
                    watermarkText = dateStr;
                }
            } catch (Exception e) {
                // 如果日期提取失败，使用默认文本
            }
        }
        
        // 调用处理器添加文本水印
        return processor.addTextWatermark(
                baseImage,
                watermarkText,
                color,
                fontFamily,
                fontSize,
                position,
                rotation,
                shadow,
                stroke,
                tiling
        );
    }

    /**
     * 应用图片水印
     */
    public BufferedImage applyImageWatermark(BufferedImage baseImage, File watermarkImageFile, 
                                          float scale, float opacity, String position, 
                                          double rotation, boolean tiling) throws IOException {
        if (watermarkImageFile == null || !watermarkImageFile.exists()) {
            throw new IOException("水印图片不存在");
        }
        
        BufferedImage watermarkImg = ImageIO.read(watermarkImageFile);
        
        // 调用处理器添加图片水印
        return processor.addImageWatermark(
                baseImage,
                watermarkImg,
                scale,
                opacity,
                position,
                rotation,
                tiling
        );
    }

    /**
     * 应用水印并返回处理后的图片（旧方法，保留兼容性）
     */
    public BufferedImage applyWatermark(BufferedImage originalImage, String text, 
                                       java.awt.Color color, int fontSize, String position, 
                                       double rotation, boolean shadow, boolean tiling, 
                                       File watermarkImageFile, float opacity, 
                                       boolean isTextWatermark) throws IOException {
        // 获取文本水印参数
        boolean hasTextWatermark = text != null && !text.trim().isEmpty();
        
        // 获取图片水印参数
        boolean hasImageWatermark = !isTextWatermark && watermarkImageFile != null && watermarkImageFile.exists();
        
        BufferedImage watermarkImg = null;
        if (hasImageWatermark) {
            watermarkImg = ImageIO.read(watermarkImageFile);
        }
        
        // 根据情况应用不同类型的水印
        if (isTextWatermark && hasTextWatermark) {
            // 仅应用文本水印
            return processor.addTextWatermark(
                    originalImage,
                    text,
                    color,
                    "Arial", // 默认字体
                    fontSize,
                    position,
                    rotation,
                    shadow,
                    false,   // 默认为false，不使用描边
                    tiling
            );
        } else if (hasImageWatermark) {
            // 仅应用图片水印
            return processor.addImageWatermark(
                    originalImage,
                    watermarkImg,
                    1.0f,    // 默认缩放为1.0
                    opacity,
                    position,
                    rotation,
                    tiling
            );
        }
        
        return null;
    }

    /**
     * 对单个图片应用水印（用于批量处理）（旧方法，保留兼容性）
     */
    public BufferedImage applyWatermarkToImage(BufferedImage original, ImageFile imageFile, 
                                             String text, boolean useExifDate, 
                                             java.awt.Color color, int fontSize, 
                                             String position, double rotation, 
                                             boolean shadow, boolean tiling, 
                                             File watermarkImageFile, float opacity, 
                                             boolean isTextWatermark) throws Exception {
        if (isTextWatermark) {
            // 处理日期水印
            String watermarkText = text;
            if (useExifDate) {
                String dateStr = com.photowatermark.ExifExtractor.extractDateTime(imageFile.getFile());
                if (dateStr != null) {
                    watermarkText = dateStr;
                }
            }
            
            return processor.addTextWatermark(
                    original,
                    watermarkText,
                    color,
                    "Arial", // 默认字体
                    fontSize,
                    position,
                    rotation,
                    shadow,
                    false,   // 默认为false，不使用描边
                    tiling
            );
        } else {
            // 图片水印
            if (watermarkImageFile == null || !watermarkImageFile.exists()) {
                throw new IOException("水印图片不存在");
            }
            
            BufferedImage watermarkImg = ImageIO.read(watermarkImageFile);
            
            return processor.addImageWatermark(
                    original,
                    watermarkImg,
                    1.0f,    // 默认缩放为1.0
                    opacity,
                    position,
                    rotation,
                    tiling
            );
        }
    }
}