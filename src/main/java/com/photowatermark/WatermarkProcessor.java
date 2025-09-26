package com.photowatermark;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 水印处理器门面类 - 整合所有水印处理功能，提供统一的接口
 */
public class WatermarkProcessor {
    private final TextWatermarkProcessor textProcessor;
    private final ImageWatermarkProcessor imageProcessor;
    private final CombinedWatermarkProcessor combinedProcessor;
    
    public WatermarkProcessor() {
        this.textProcessor = new TextWatermarkProcessor();
        this.imageProcessor = new ImageWatermarkProcessor();
        this.combinedProcessor = new CombinedWatermarkProcessor();
    }
    
    /**
     * 处理图片并添加水印（原有静态方法保持兼容）
     */
    public static void processImage(
            File imageFile, 
            String watermarkText, 
            int fontSize, 
            Color color, 
            Position position
    ) throws IOException {
        // 读取原始图片
        BufferedImage originalImage = FileUtils.readImage(imageFile);
        
        // 创建处理器实例
        WatermarkProcessor processor = new WatermarkProcessor();
        
        // 添加文本水印
        BufferedImage watermarkedImage = processor.addTextWatermark(
                originalImage, 
                watermarkText, 
                color, 
                "Arial", // 默认字体
                fontSize, 
                position.name(), 
                0, 
                false, 
                false, // 默认为false，不使用描边
                false
        );
        
        // 创建保存目录
        File outputDir = FileUtils.createOutputDirectory(imageFile);
        
        // 保存水印图片
        FileUtils.saveWatermarkedImage(
                watermarkedImage, 
                outputDir, 
                imageFile.getName()
        );
    }
    
    /**
     * 添加文本水印（新方法，支持字体和描边）
     */
    public BufferedImage addTextWatermark(
            BufferedImage originalImage, 
            String text, 
            Color color, 
            String fontFamily,
            int fontSize, 
            String positionStr, 
            double rotation, 
            boolean shadow, 
            boolean stroke,
            boolean tiling
    ) {
        return textProcessor.addTextWatermark(
                originalImage, 
                text, 
                color, 
                fontFamily,
                fontSize, 
                positionStr, 
                rotation, 
                shadow, 
                stroke,
                tiling
        );
    }
    
    /**
     * 添加文本水印（带自定义位置支持）
     */
    public BufferedImage addTextWatermark(
            BufferedImage originalImage, 
            String text, 
            Color color, 
            String fontFamily,
            int fontSize, 
            String positionStr, 
            double rotation, 
            boolean shadow, 
            boolean stroke,
            boolean tiling,
            double customX, 
            double customY
    ) {
        return textProcessor.addTextWatermark(
                originalImage, 
                text, 
                color, 
                fontFamily,
                fontSize, 
                positionStr, 
                rotation, 
                shadow, 
                stroke,
                tiling,
                customX,
                customY
        );
    }
    
    /**
     * 添加文本水印（旧方法，保持向后兼容）
     */
    public BufferedImage addTextWatermark(
            BufferedImage originalImage, 
            String text, 
            Color color, 
            int fontSize, 
            String positionStr, 
            double rotation, 
            boolean shadow, 
            boolean tiling
    ) {
        // 调用新方法，传入默认值
        return addTextWatermark(
                originalImage, 
                text, 
                color, 
                "Arial", // 默认字体
                fontSize, 
                positionStr, 
                rotation, 
                shadow, 
                false, // 默认不使用描边
                tiling
        );
    }
    
    /**
     * 添加图片水印（新方法，支持缩放）
     */
    public BufferedImage addImageWatermark(
            BufferedImage originalImage, 
            BufferedImage watermarkImage, 
            float scale,
            float opacity, 
            String positionStr, 
            double rotation, 
            boolean tiling
    ) {
        return imageProcessor.addImageWatermark(
                originalImage,
                watermarkImage,
                scale,
                opacity,
                positionStr,
                rotation,
                tiling
        );
    }
    
    /**
     * 添加图片水印（带自定义位置支持）
     */
    public BufferedImage addImageWatermark(
            BufferedImage originalImage, 
            BufferedImage watermarkImage, 
            float scale,
            float opacity, 
            String positionStr, 
            double rotation, 
            boolean tiling,
            double customX,
            double customY
    ) {
        return imageProcessor.addImageWatermark(
                originalImage,
                watermarkImage,
                scale,
                opacity,
                positionStr,
                rotation,
                tiling,
                customX,
                customY
        );
    }
    
    /**
     * 添加图片水印（旧方法，保持向后兼容）
     */
    public BufferedImage addImageWatermark(
            BufferedImage originalImage, 
            BufferedImage watermarkImage, 
            float opacity, 
            String positionStr, 
            double rotation, 
            boolean tiling
    ) {
        // 调用新方法，传入默认缩放值
        return addImageWatermark(
                originalImage, 
                watermarkImage, 
                1.0f, // 默认缩放为1.0
                opacity, 
                positionStr, 
                rotation, 
                tiling
        );
    }
    
    /**
     * 同时添加文本水印和图片水印
     */
    public BufferedImage addCombinedWatermark(
            BufferedImage originalImage,
            String text,
            Color textColor,
            int fontSize,
            String textPositionStr,
            double textRotation,
            boolean textShadow,
            boolean textTiling,
            BufferedImage watermarkImage,
            float imageOpacity,
            String imagePositionStr,
            double imageRotation,
            boolean imageTiling
    ) {
        return combinedProcessor.addCombinedWatermark(
                originalImage,
                text,
                textColor,
                fontSize,
                textPositionStr,
                textRotation,
                textShadow,
                textTiling,
                watermarkImage,
                imageOpacity,
                imagePositionStr,
                imageRotation,
                imageTiling
        );
    }
}