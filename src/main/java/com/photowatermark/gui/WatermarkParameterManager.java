package com.photowatermark.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.File;
import java.awt.image.BufferedImage;

/**
 * 水印参数管理器 - 负责管理和提供水印相关的所有参数
 */
public class WatermarkParameterManager {
    // GUI组件引用
    private final TextField watermarkText;
    private final ComboBox<String> fontFamilyComboBox;
    private final Slider textFontSizeSlider;
    private final ColorPicker textColorPicker;
    private final Slider textOpacitySlider;
    private final Slider textRotationSlider;
    private final CheckBox enableShadow;
    private final CheckBox enableStroke;
    private final CheckBox useExifDate;
    private final ComboBox<String> dateFormatComboBox;
    private final Slider imageScaleSlider;
    private final Slider imageOpacitySlider;
    private final Slider imageRotationSlider;
    private final CheckBox enableTextTiling;
    private final CheckBox enableImageTiling;
    
    // 水印参数
    private File watermarkImageFile;
    private WatermarkPosition textWatermarkPosition = WatermarkPosition.BOTTOM_RIGHT;
    private WatermarkPosition imageWatermarkPosition = WatermarkPosition.TOP_LEFT;
    private final BooleanProperty parametersChanged = new SimpleBooleanProperty(false);
    
    // 自定义位置坐标（百分比表示，相对于图片尺寸）
    private double customTextWatermarkX = 1.0; // 默认右下角
    private double customTextWatermarkY = 1.0;
    private double customImageWatermarkX = 0.0; // 默认左上角
    private double customImageWatermarkY = 0.0;
    
    // 是否使用自定义位置
    private boolean useCustomTextPosition = false;
    private boolean useCustomImagePosition = false;
    
    public WatermarkParameterManager(
            TextField watermarkText,
            ComboBox<String> fontFamilyComboBox,
            Slider textFontSizeSlider,
            ColorPicker textColorPicker,
            Slider textOpacitySlider,
            Slider textRotationSlider,
            CheckBox enableShadow,
            CheckBox enableStroke,
            CheckBox useExifDate,
            ComboBox<String> dateFormatComboBox,
            Slider imageScaleSlider,
            Slider imageOpacitySlider,
            Slider imageRotationSlider,
            CheckBox enableTextTiling,
            CheckBox enableImageTiling
    ) {
        this.watermarkText = watermarkText;
        this.fontFamilyComboBox = fontFamilyComboBox;
        this.textFontSizeSlider = textFontSizeSlider;
        this.textColorPicker = textColorPicker;
        this.textOpacitySlider = textOpacitySlider;
        this.textRotationSlider = textRotationSlider;
        this.enableShadow = enableShadow;
        this.enableStroke = enableStroke;
        this.useExifDate = useExifDate;
        this.dateFormatComboBox = dateFormatComboBox;
        this.imageScaleSlider = imageScaleSlider;
        this.imageOpacitySlider = imageOpacitySlider;
        this.imageRotationSlider = imageRotationSlider;
        this.enableTextTiling = enableTextTiling;
        this.enableImageTiling = enableImageTiling;
        
        // 监听参数变化
        setupParameterChangeListeners();
    }
    
    /**
     * 设置参数变化监听器
     */
    private void setupParameterChangeListeners() {
        textFontSizeSlider.valueProperty().addListener(obs -> parametersChanged.set(true));
        textOpacitySlider.valueProperty().addListener(obs -> parametersChanged.set(true));
        textRotationSlider.valueProperty().addListener(obs -> parametersChanged.set(true));
        imageScaleSlider.valueProperty().addListener(obs -> parametersChanged.set(true));
        imageOpacitySlider.valueProperty().addListener(obs -> parametersChanged.set(true));
        imageRotationSlider.valueProperty().addListener(obs -> parametersChanged.set(true));
        
        textColorPicker.valueProperty().addListener(obs -> parametersChanged.set(true));
        enableShadow.selectedProperty().addListener(obs -> parametersChanged.set(true));
        enableStroke.selectedProperty().addListener(obs -> parametersChanged.set(true));
        useExifDate.selectedProperty().addListener(obs -> parametersChanged.set(true));
        enableTextTiling.selectedProperty().addListener(obs -> parametersChanged.set(true));
        enableImageTiling.selectedProperty().addListener(obs -> parametersChanged.set(true));
        fontFamilyComboBox.valueProperty().addListener(obs -> parametersChanged.set(true));
        watermarkText.textProperty().addListener(obs -> parametersChanged.set(true));
        
        // 重置变化标志
        parametersChanged.addListener(obs -> {
            if (parametersChanged.get()) {
                parametersChanged.set(false);
            }
        });
    }
    
    /**
     * 判断是否有文本水印
     */
    public boolean hasTextWatermark() {
        return useExifDate.isSelected() || (watermarkText.getText() != null && !watermarkText.getText().trim().isEmpty());
    }
    
    /**
     * 判断是否有图片水印
     */
    public boolean hasImageWatermark() {
        return watermarkImageFile != null && watermarkImageFile.exists();
    }
    
    // Getter 方法
    public String getWatermarkText() {
        return watermarkText.getText();
    }
    
    public String getFontFamily() {
        return fontFamilyComboBox.getValue() != null ? fontFamilyComboBox.getValue() : "Arial";
    }
    
    public int getFontSize() {
        return (int) textFontSizeSlider.getValue();
    }
    
    public Color getTextColor() {
        return textColorPicker.getValue();
    }
    
    public float getTextOpacity() {
        return (float) textOpacitySlider.getValue();
    }
    
    public double getTextRotation() {
        return textRotationSlider.getValue();
    }
    
    public boolean isShadowEnabled() {
        return enableShadow.isSelected();
    }
    
    public boolean isStrokeEnabled() {
        return enableStroke.isSelected();
    }
    
    public boolean isUseExifDate() {
        return useExifDate.isSelected();
    }
    
    public float getImageScale() {
        return (float) imageScaleSlider.getValue();
    }
    
    public float getImageOpacity() {
        return (float) imageOpacitySlider.getValue();
    }
    
    public double getImageRotation() {
        return imageRotationSlider.getValue();
    }
    
    public boolean isTextTilingEnabled() {
        return enableTextTiling.isSelected();
    }
    
    public boolean isImageTilingEnabled() {
        return enableImageTiling.isSelected();
    }
    
    public File getWatermarkImageFile() {
        return watermarkImageFile;
    }
    
    public void setWatermarkImageFile(File file) {
        this.watermarkImageFile = file;
        parametersChanged.set(true);
    }
    
    public WatermarkPosition getTextWatermarkPosition() {
        return textWatermarkPosition;
    }
    
    public void setTextWatermarkPosition(WatermarkPosition position) {
        this.textWatermarkPosition = position;
        // 当设置预设位置时，关闭自定义位置
        this.useCustomTextPosition = false;
        parametersChanged.set(true);
    }
    
    public WatermarkPosition getImageWatermarkPosition() {
        return imageWatermarkPosition;
    }
    
    public void setImageWatermarkPosition(WatermarkPosition position) {
        this.imageWatermarkPosition = position;
        // 当设置预设位置时，关闭自定义位置
        this.useCustomImagePosition = false;
        parametersChanged.set(true);
    }
    
    // 自定义位置相关方法
    public boolean isUseCustomTextPosition() {
        return useCustomTextPosition;
    }
    
    public void setUseCustomTextPosition(boolean useCustomTextPosition) {
        this.useCustomTextPosition = useCustomTextPosition;
        parametersChanged.set(true);
    }
    
    public double getCustomTextWatermarkX() {
        return customTextWatermarkX;
    }
    
    public void setCustomTextWatermarkX(double customTextWatermarkX) {
        this.customTextWatermarkX = customTextWatermarkX;
        this.useCustomTextPosition = true;
        parametersChanged.set(true);
    }
    
    public double getCustomTextWatermarkY() {
        return customTextWatermarkY;
    }
    
    public void setCustomTextWatermarkY(double customTextWatermarkY) {
        this.customTextWatermarkY = customTextWatermarkY;
        this.useCustomTextPosition = true;
        parametersChanged.set(true);
    }
    
    public void setCustomTextWatermarkPosition(double x, double y) {
        this.customTextWatermarkX = x;
        this.customTextWatermarkY = y;
        this.useCustomTextPosition = true;
        parametersChanged.set(true);
    }
    
    public boolean isUseCustomImagePosition() {
        return useCustomImagePosition;
    }
    
    public void setUseCustomImagePosition(boolean useCustomImagePosition) {
        this.useCustomImagePosition = useCustomImagePosition;
        parametersChanged.set(true);
    }
    
    public double getCustomImageWatermarkX() {
        return customImageWatermarkX;
    }
    
    public void setCustomImageWatermarkX(double customImageWatermarkX) {
        this.customImageWatermarkX = customImageWatermarkX;
        this.useCustomImagePosition = true;
        parametersChanged.set(true);
    }
    
    public double getCustomImageWatermarkY() {
        return customImageWatermarkY;
    }
    
    public void setCustomImageWatermarkY(double customImageWatermarkY) {
        this.customImageWatermarkY = customImageWatermarkY;
        this.useCustomImagePosition = true;
        parametersChanged.set(true);
    }
    
    public void setCustomImageWatermarkPosition(double x, double y) {
        this.customImageWatermarkX = x;
        this.customImageWatermarkY = y;
        this.useCustomImagePosition = true;
        parametersChanged.set(true);
    }
    
    /**
     * 清除水印设置
     */
    public void clearWatermarkSettings() {
        // 清除文本水印内容
        watermarkText.clear();
        useExifDate.setSelected(false);
        useCustomTextPosition = false;
        
        // 清除图片水印内容
        watermarkImageFile = null;
        useCustomImagePosition = false;
    }
}