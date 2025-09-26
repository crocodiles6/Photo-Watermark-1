package com.photowatermark.gui;

import java.io.File;
import java.io.Serializable;
import javafx.scene.paint.Color;

/**
 * 水印模板类 - 用于存储和序列化水印设置
 */
public class WatermarkTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 模板名称
    private String templateName;
    
    // 文本水印参数
    private String watermarkText;
    private String fontFamily;
    private int fontSize;
    // 使用可序列化的方式存储颜色信息，替代直接存储Color对象
    private double textColorRed;
    private double textColorGreen;
    private double textColorBlue;
    private double textColorOpacity;
    private float textOpacity;
    private double textRotation;
    private boolean isShadowEnabled;
    private boolean isStrokeEnabled;
    private boolean isUseExifDate;
    private boolean isTextTilingEnabled;
    
    // 图片水印参数
    private float imageScale;
    private float imageOpacity;
    private double imageRotation;
    private boolean isImageTilingEnabled;
    private boolean hasImageWatermark;
    
    // 水印位置参数
    private WatermarkPosition textWatermarkPosition;
    private WatermarkPosition imageWatermarkPosition;
    private boolean useCustomTextPosition;
    private boolean useCustomImagePosition;
    private double customTextWatermarkX;
    private double customTextWatermarkY;
    private double customImageWatermarkX;
    private double customImageWatermarkY;
    
    public WatermarkTemplate(String templateName, WatermarkParameterManager parameterManager) {
        this.templateName = templateName;
        saveParameters(parameterManager);
    }
    
    /**
     * 从参数管理器保存参数到模板
     */
    private void saveParameters(WatermarkParameterManager parameterManager) {
        // 保存文本水印参数
        this.watermarkText = parameterManager.getWatermarkTextValue();
        this.fontFamily = parameterManager.getFontFamilyValue();
        this.fontSize = parameterManager.getFontSizeValue();
        
        // 保存颜色（分解为RGB组件）
        Color textColor = parameterManager.getTextColorValue();
        this.textColorRed = textColor.getRed();
        this.textColorGreen = textColor.getGreen();
        this.textColorBlue = textColor.getBlue();
        this.textColorOpacity = textColor.getOpacity();
        
        this.textOpacity = parameterManager.getTextOpacityValue();
        this.textRotation = parameterManager.getTextRotationValue();
        this.isShadowEnabled = parameterManager.isShadowEnabled();
        this.isStrokeEnabled = parameterManager.isStrokeEnabled();
        this.isUseExifDate = parameterManager.isUseExifDate();
        this.isTextTilingEnabled = parameterManager.isTextTilingEnabled();
        
        // 保存图片水印参数
        this.imageScale = parameterManager.getImageScaleValue();
        this.imageOpacity = parameterManager.getImageOpacityValue();
        this.imageRotation = parameterManager.getImageRotationValue();
        this.isImageTilingEnabled = parameterManager.isImageTilingEnabled();
        this.hasImageWatermark = parameterManager.hasImageWatermark();
        
        // 保存位置参数
        this.textWatermarkPosition = parameterManager.getTextWatermarkPosition();
        this.imageWatermarkPosition = parameterManager.getImageWatermarkPosition();
        this.useCustomTextPosition = parameterManager.isUseCustomTextPosition();
        this.useCustomImagePosition = parameterManager.isUseCustomImagePosition();
        this.customTextWatermarkX = parameterManager.getCustomTextWatermarkX();
        this.customTextWatermarkY = parameterManager.getCustomTextWatermarkY();
        this.customImageWatermarkX = parameterManager.getCustomImageWatermarkX();
        this.customImageWatermarkY = parameterManager.getCustomImageWatermarkY();
    }
    
    /**
     * 将模板参数应用到参数管理器
     */
    public void applyTo(WatermarkParameterManager parameterManager) {
        // 应用文本水印参数
        if (parameterManager.getWatermarkText() != null) {
            parameterManager.getWatermarkText().setText(this.watermarkText);
        }
        if (parameterManager.getFontFamily() != null) {
            parameterManager.getFontFamily().setValue(this.fontFamily);
        }
        parameterManager.getFontSize().setValue(this.fontSize);
        
        // 重建颜色
        Color textColor = Color.color(
            this.textColorRed, 
            this.textColorGreen, 
            this.textColorBlue, 
            this.textColorOpacity
        );
        parameterManager.getTextColor().setValue(textColor);
        
        parameterManager.getTextOpacity().setValue(this.textOpacity);
        parameterManager.getTextRotation().setValue(this.textRotation);
        parameterManager.getEnableShadow().setSelected(this.isShadowEnabled);
        parameterManager.getEnableStroke().setSelected(this.isStrokeEnabled);
        parameterManager.getUseExifDate().setSelected(this.isUseExifDate);
        parameterManager.getEnableTextTiling().setSelected(this.isTextTilingEnabled);
        
        // 应用图片水印参数
        parameterManager.getImageScale().setValue(this.imageScale);
        parameterManager.getImageOpacity().setValue(this.imageOpacity);
        parameterManager.getImageRotation().setValue(this.imageRotation);
        parameterManager.getEnableImageTiling().setSelected(this.isImageTilingEnabled);
        
        // 应用位置参数
        parameterManager.setTextWatermarkPosition(this.textWatermarkPosition);
        parameterManager.setImageWatermarkPosition(this.imageWatermarkPosition);
        
        if (this.useCustomTextPosition) {
            parameterManager.setCustomTextWatermarkPosition(this.customTextWatermarkX, this.customTextWatermarkY);
        }
        
        if (this.useCustomImagePosition) {
            parameterManager.setCustomImageWatermarkPosition(this.customImageWatermarkX, this.customImageWatermarkY);
        }
    }
    
    // Getters and Setters
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    @Override
    public String toString() {
        return templateName;
    }
}