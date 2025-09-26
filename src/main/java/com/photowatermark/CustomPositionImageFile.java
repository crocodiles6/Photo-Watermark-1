package com.photowatermark;

/**
 * 支持自定义水印位置的图片文件接口
 */
public interface CustomPositionImageFile {
    
    /**
     * 获取文本水印的自定义X坐标（相对于图片宽度的比例，0.0-1.0）
     */
    double getCustomTextWatermarkX();
    
    /**
     * 设置文本水印的自定义X坐标
     */
    void setCustomTextWatermarkX(double x);
    
    /**
     * 获取文本水印的自定义Y坐标（相对于图片高度的比例，0.0-1.0）
     */
    double getCustomTextWatermarkY();
    
    /**
     * 设置文本水印的自定义Y坐标
     */
    void setCustomTextWatermarkY(double y);
    
    /**
     * 获取图片水印的自定义X坐标（相对于图片宽度的比例，0.0-1.0）
     */
    double getCustomImageWatermarkX();
    
    /**
     * 设置图片水印的自定义X坐标
     */
    void setCustomImageWatermarkX(double x);
    
    /**
     * 获取图片水印的自定义Y坐标（相对于图片高度的比例，0.0-1.0）
     */
    double getCustomImageWatermarkY();
    
    /**
     * 设置图片水印的自定义Y坐标
     */
    void setCustomImageWatermarkY(double y);
}