package com.photowatermark.gui;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 图片文件管理器 - 负责处理图片的导入、选择和预览
 */
public class ImageFileManager {
    // GUI组件引用
    private final ImageView previewImageView;
    private final UiUtils uiUtils;
    
    // 图片相关数据
    private final ObservableList<ImageFile> imageFiles = FXCollections.observableArrayList();
    private final ObjectProperty<ImageFile> selectedImageFileProperty = new SimpleObjectProperty<>();
    private BufferedImage originalImage;
    private BufferedImage watermarkedImage;
    
    // 服务类
    private final ImageConverter imageConverter;
    
    // 支持的图片文件扩展名
    private static final List<String> SUPPORTED_IMAGE_EXTENSIONS = Arrays.asList(
            "png", "jpg", "jpeg", "bmp", "tiff");
    
    public ImageFileManager(ImageView previewImageView, UiUtils uiUtils, ImageConverter imageConverter) {
        this.previewImageView = previewImageView;
        this.uiUtils = uiUtils;
        this.imageConverter = imageConverter;
        
        // 监听选择变化
        selectedImageFileProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleImageSelection(newValue);
            }
        });
    }
    
    /**
     * 处理图片选择
     */
    private void handleImageSelection(ImageFile imageFile) {
        try {
            originalImage = ImageIO.read(imageFile.getFile());
            watermarkedImage = null;
            
            // 显示原始图片预览
            Image fxImage = imageConverter.convertToFxImage(originalImage);
            Platform.runLater(() -> previewImageView.setImage(fxImage));
            
            uiUtils.updateStatus("已加载: " + imageFile.getFileName());
        } catch (IOException e) {
            uiUtils.showError("加载图片失败", "无法加载图片: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 导入图片文件
     */
    public void importImageFiles(List<File> selectedFiles) {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }
        
        List<ImageFile> newImageFiles = selectedFiles.stream()
                .map(ImageFile::new)
                .collect(Collectors.toList());
        
        imageFiles.addAll(newImageFiles);
        uiUtils.updateStatus("已导入 " + newImageFiles.size() + " 张图片");
        
        // 如果是第一次导入，自动选择第一张
        if (imageFiles.size() == newImageFiles.size() && !imageFiles.isEmpty()) {
            selectedImageFileProperty.set(imageFiles.get(0));
        }
    }
    
    /**
     * 从文件夹导入图片文件（不包括子目录）
     */
    public void importImagesFromFolder(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return;
        }
        
        // 获取文件夹中的所有文件
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            uiUtils.updateStatus("选择的文件夹中没有文件");
            return;
        }
        
        // 过滤出支持的图片文件
        List<File> imageFiles = Arrays.stream(files)
                .filter(file -> file.isFile() && isSupportedImageFile(file))
                .collect(Collectors.toList());
        
        if (imageFiles.isEmpty()) {
            uiUtils.updateStatus("选择的文件夹中没有支持的图片文件");
            return;
        }
        
        // 导入过滤后的图片文件
        importImageFiles(imageFiles);
    }
    
    /**
     * 检查文件是否为支持的图片格式
     */
    private boolean isSupportedImageFile(File file) {
        String extension = getFileExtension(file).toLowerCase(Locale.ROOT);
        return SUPPORTED_IMAGE_EXTENSIONS.contains(extension);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < name.length() - 1) {
            return name.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 重置图片预览
     */
    public void resetPreview() {
        // 清除预览图片
        previewImageView.setImage(null);
        
        // 清除已导入的图片列表
        imageFiles.clear();
        
        // 重置相关变量
        selectedImageFileProperty.set(null);
        originalImage = null;
        watermarkedImage = null;
        
        // 更新状态提示
        uiUtils.updateStatus("导入图片已重置");
    }
    
    /**
     * 更新预览图片
     */
    public void updatePreviewImage(BufferedImage image) {
        this.watermarkedImage = image;
        if (watermarkedImage != null) {
            Image fxImage = imageConverter.convertToFxImage(watermarkedImage);
            Platform.runLater(() -> previewImageView.setImage(fxImage));
        }
    }
    
    /**
     * 清除水印
     */
    public void clearWatermark() {
        watermarkedImage = null;
        if (originalImage != null) {
            Image fxImage = imageConverter.convertToFxImage(originalImage);
            previewImageView.setImage(fxImage);
        } else {
            previewImageView.setImage(null);
        }
    }
    
    // Getter 方法
    public ObservableList<ImageFile> getImageFiles() {
        return imageFiles;
    }
    
    public ImageFile getSelectedImageFile() {
        return selectedImageFileProperty.get();
    }
    
    public void setSelectedImageFile(ImageFile imageFile) {
        selectedImageFileProperty.set(imageFile);
    }
    
    public ObjectProperty<ImageFile> selectedImageFileProperty() {
        return selectedImageFileProperty;
    }
    
    public BufferedImage getOriginalImage() {
        return originalImage;
    }
    
    public BufferedImage getWatermarkedImage() {
        return watermarkedImage;
    }
    
    public boolean hasSelectedImage() {
        return selectedImageFileProperty.get() != null && originalImage != null;
    }
}