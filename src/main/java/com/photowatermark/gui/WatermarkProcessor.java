package com.photowatermark.gui;

import com.photowatermark.gui.WatermarkService;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 水印处理器 - 负责水印应用的核心逻辑
 */
public class WatermarkProcessor {
    private final WatermarkService watermarkService;
    private final WatermarkParameterManager parameterManager;
    private final ImageFileManager imageFileManager;
    private final UiUtils uiUtils;
    private final ExecutorService executorService;
    private final BooleanProperty isProcessing = new SimpleBooleanProperty(false);
    
    public WatermarkProcessor(
            WatermarkService watermarkService,
            WatermarkParameterManager parameterManager,
            ImageFileManager imageFileManager,
            UiUtils uiUtils,
            ExecutorService executorService
    ) {
        this.watermarkService = watermarkService;
        this.parameterManager = parameterManager;
        this.imageFileManager = imageFileManager;
        this.uiUtils = uiUtils;
        this.executorService = executorService;
        
        // 设置处理中状态
        isProcessing.addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                uiUtils.updateStatus(newVal ? "处理中..." : "就绪");
            });
        });
    }
    
    /**
     * 应用水印
     */
    public void applyWatermark() {
        if (!imageFileManager.hasSelectedImage()) {
            return;
        }
        
        try {
            // 检查是否有水印内容
            boolean hasTextWatermark = parameterManager.hasTextWatermark();
            boolean hasImageWatermark = parameterManager.hasImageWatermark();
            
            if (!hasTextWatermark && !hasImageWatermark) {
                return;
            }
            
            // 准备文本水印参数
            String fontFamily = parameterManager.getFontFamilyValue();
            int fontSize = parameterManager.getFontSizeValue();
            javafx.scene.paint.Color color = parameterManager.getTextColorValue();
            float textOpacity = parameterManager.getTextOpacityValue();
            double textRotation = parameterManager.getTextRotationValue();
            boolean shadow = parameterManager.isShadowEnabled();
            boolean stroke = parameterManager.isStrokeEnabled();
            boolean textTiling = parameterManager.isTextTilingEnabled();
            
            // 准备图片水印参数
            float imageScale = parameterManager.getImageScaleValue();
            float imageOpacity = parameterManager.getImageOpacityValue();
            double imageRotation = parameterManager.getImageRotationValue();
            boolean imageTiling = parameterManager.isImageTilingEnabled();
            
            // 转换JavaFX颜色为AWT颜色
            java.awt.Color awtColor = new java.awt.Color(
                    (float) color.getRed(),
                    (float) color.getGreen(),
                    (float) color.getBlue(),
                    textOpacity
            );
            
            // 复制原始图像作为基础
            BufferedImage baseImage = new BufferedImage(
                    imageFileManager.getOriginalImage().getWidth(), 
                    imageFileManager.getOriginalImage().getHeight(), 
                    BufferedImage.TYPE_INT_ARGB
            );
            java.awt.Graphics2D g2d = baseImage.createGraphics();
            g2d.drawImage(imageFileManager.getOriginalImage(), 0, 0, null);
            g2d.dispose();
            
            // 应用文本水印（如果有）
            if (hasTextWatermark) {
                baseImage = watermarkService.applyTextWatermark(
                        baseImage,
                        parameterManager.getWatermarkTextValue(),
                        awtColor,
                        fontFamily,
                        fontSize,
                        parameterManager.getTextWatermarkPosition().name(),
                        textRotation,
                        shadow,
                        stroke,
                        textTiling,
                        parameterManager.isUseExifDate(),
                        imageFileManager.getSelectedImageFile()
                );
            }
            
            // 应用图片水印（如果有）
            if (hasImageWatermark) {
                baseImage = watermarkService.applyImageWatermark(
                        baseImage,
                        parameterManager.getWatermarkImageFile(),
                        imageScale,
                        imageOpacity,
                        parameterManager.getImageWatermarkPosition().name(),
                        imageRotation,
                        imageTiling,
                        imageFileManager.getSelectedImageFile()
                );
            }
            
            // 更新预览
            imageFileManager.updatePreviewImage(baseImage);
            uiUtils.updateStatus("水印应用成功");
        } catch (Exception e) {
            uiUtils.showError("应用水印失败", "无法应用水印: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查水印参数是否有效
     */
    public boolean validateWatermarkParameters() {
        boolean hasTextWatermark = parameterManager.hasTextWatermark();
        boolean hasImageWatermark = parameterManager.hasImageWatermark();
        
        if (!hasTextWatermark && !hasImageWatermark) {
            uiUtils.showWarning("无水印内容", "请输入水印文本、启用EXIF日期或选择水印图片");
            return false;
        }
        
        return true;
    }
}