package com.photowatermark.gui;

import com.photowatermark.CustomPositionImageFile;
import java.io.File;

/**
 * 图片文件类，用于ListView显示
 */
public class ImageFile implements CustomPositionImageFile {
    private final File file;
    private final String fileName;
    
    // 自定义水印位置属性
    private double customTextWatermarkX = 0.5; // 默认中心位置（0.0-1.0范围）
    private double customTextWatermarkY = 0.5;
    private double customImageWatermarkX = 0.5;
    private double customImageWatermarkY = 0.5;

    public ImageFile(File file) {
        this.file = file;
        this.fileName = file.getName();
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return fileName;
    }
    
    @Override
    public double getCustomTextWatermarkX() {
        return customTextWatermarkX;
    }
    
    @Override
    public void setCustomTextWatermarkX(double x) {
        this.customTextWatermarkX = clamp(x, 0.0, 1.0);
    }
    
    @Override
    public double getCustomTextWatermarkY() {
        return customTextWatermarkY;
    }
    
    @Override
    public void setCustomTextWatermarkY(double y) {
        this.customTextWatermarkY = clamp(y, 0.0, 1.0);
    }
    
    @Override
    public double getCustomImageWatermarkX() {
        return customImageWatermarkX;
    }
    
    @Override
    public void setCustomImageWatermarkX(double x) {
        this.customImageWatermarkX = clamp(x, 0.0, 1.0);
    }
    
    @Override
    public double getCustomImageWatermarkY() {
        return customImageWatermarkY;
    }
    
    @Override
    public void setCustomImageWatermarkY(double y) {
        this.customImageWatermarkY = clamp(y, 0.0, 1.0);
    }
    
    /**
     * 限制值在指定范围内
     */
    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
}