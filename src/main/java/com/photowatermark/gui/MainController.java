package com.photowatermark.gui;

import com.photowatermark.ExifExtractor;
import com.photowatermark.WatermarkProcessor;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 主窗口控制器类，处理所有GUI交互和水印功能
 */
public class MainController {
    // GUI组件引用
    @FXML private ListView<ImageFile> imageListView;
    @FXML private ImageView previewImageView;
    @FXML private TextField watermarkText;
    @FXML private Slider fontSizeSlider;
    @FXML private Label fontSizeValue;
    @FXML private ColorPicker textColorPicker;
    @FXML private Slider opacitySlider;
    @FXML private Label opacityValue;
    @FXML private Slider rotationSlider;
    @FXML private Label rotationValue;
    @FXML private CheckBox enableShadow;
    @FXML private CheckBox enableTiling;
    @FXML private CheckBox useExifDate;
    @FXML private ComboBox<String> dateFormatComboBox;
    @FXML private RadioButton textWatermarkRadio;
    @FXML private RadioButton imageWatermarkRadio;
    @FXML private Button selectWatermarkImageBtn;
    @FXML private HBox statusBar;
    @FXML private Label statusLabel;

    // 应用数据
    private final ObservableList<ImageFile> imageFiles = FXCollections.observableArrayList();
    private ImageFile selectedImageFile;
    private File watermarkImageFile;
    private BufferedImage originalImage;
    private BufferedImage watermarkedImage;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final BooleanProperty isProcessing = new SimpleBooleanProperty(false);

    // 水印位置枚举
    public enum WatermarkPosition {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    private WatermarkPosition currentPosition = WatermarkPosition.BOTTOM_RIGHT;

    /**
     * 初始化控制器
     */
    @FXML
    public void initialize() {
        // 设置图片列表
        imageListView.setItems(imageFiles);
        imageListView.setCellFactory(param -> new ImageListCell());
        imageListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleImageSelection(newValue));

        // 绑定滑块与标签
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            fontSizeValue.setText(String.format("%.0f", newVal));
            updatePreviewIfPossible();
        });
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            opacityValue.setText(String.format("%.1f", newVal));
            updatePreviewIfPossible();
        });
        rotationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rotationValue.setText(String.format("%.0f°", newVal));
            updatePreviewIfPossible();
        });

        // 水印类型切换
        textWatermarkRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            selectWatermarkImageBtn.setDisable(newVal);
            updatePreviewIfPossible();
        });

        // 日期水印选项变更
        useExifDate.selectedProperty().addListener((obs, oldVal, newVal) -> {
            watermarkText.setDisable(newVal);
            
            // 当复选框从无到有时
            if (newVal && !oldVal) {
                watermarkText.clear(); // 清除输入框内容
                if (selectedImageFile != null) {
                    try {
                        String dateStr = ExifExtractor.extractDateTime(selectedImageFile.getFile());
                        if (dateStr != null) {
                            watermarkText.setText(dateStr);
                        } else {
                            watermarkText.setText("无法提取日期");
                        }
                    } catch (Exception e) {
                        watermarkText.setText("日期提取错误");
                        e.printStackTrace();
                    }
                }
            } 
            // 当复选框从有到无时
            else if (!newVal && oldVal) {
                watermarkText.clear(); // 仅清除输入框内容
                // 清除预览图片上的水印
                handleClearWatermark(null);
            }
            
            updatePreviewIfPossible();
        });

        // 其他属性变更监听器
        textColorPicker.valueProperty().addListener(obs -> updatePreviewIfPossible());
        enableShadow.selectedProperty().addListener(obs -> updatePreviewIfPossible());
        enableTiling.selectedProperty().addListener(obs -> updatePreviewIfPossible());
        watermarkText.textProperty().addListener((obs, oldVal, newVal) -> {
            // 当文本框内容被清除干净时，清除预览图片上的水印
            if (newVal == null || newVal.trim().isEmpty()) {
                handleClearWatermark(null);
            } else {
                updatePreviewIfPossible();
            }
        });

        // 初始状态更新
        watermarkText.setDisable(useExifDate.isSelected());
        selectWatermarkImageBtn.setDisable(textWatermarkRadio.isSelected());

        // 设置处理中状态
        isProcessing.addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                statusLabel.setText(newVal ? "处理中..." : "就绪");
            });
        });
    }

    /**
     * 处理图片选择
     */
    private void handleImageSelection(ImageFile imageFile) {
        if (imageFile == null) return;

        selectedImageFile = imageFile;
        try {
            originalImage = ImageIO.read(imageFile.getFile());
            watermarkedImage = null;
            
            // 显示原始图片预览
            Image fxImage = convertToFxImage(originalImage);
            previewImageView.setImage(fxImage);
            
            // 如果启用了EXIF日期，尝试提取日期
            if (useExifDate.isSelected()) {
                try {
                    String dateStr = ExifExtractor.extractDateTime(imageFile.getFile());
                    if (dateStr != null) {
                        watermarkText.setText(dateStr);
                    } else {
                        watermarkText.setText("无法提取日期");
                    }
                } catch (Exception e) {
                    watermarkText.setText("日期提取错误");
                    e.printStackTrace();
                }
            }
            
            updateStatus("已加载: " + imageFile.getFileName());
        } catch (IOException e) {
            showError("加载图片失败", "无法加载图片: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理图片导入
     */
    @FXML
    private void handleImportImages(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tiff"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            List<ImageFile> newImageFiles = selectedFiles.stream()
                    .map(ImageFile::new)
                    .collect(Collectors.toList());
            
            imageFiles.addAll(newImageFiles);
            updateStatus("已导入 " + newImageFiles.size() + " 张图片");
            
            // 如果是第一次导入，自动选择第一张
            if (imageFiles.size() == newImageFiles.size()) {
                imageListView.getSelectionModel().select(0);
            }
        }
    }

    /**
     * 处理导出图片
     */
    @FXML
    private void handleExportImages(ActionEvent event) {
        if (watermarkedImage == null) {
            showWarning("无水印图片", "请先应用水印");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存水印图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );
        
        if (selectedImageFile != null) {
            String originalName = selectedImageFile.getFileName();
            String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
            String extension = originalName.substring(originalName.lastIndexOf('.'));
            fileChooser.setInitialFileName(baseName + "_watermark" + extension);
        }
        
        File saveFile = fileChooser.showSaveDialog(null);
        if (saveFile != null) {
            try {
                String format = saveFile.getName().substring(saveFile.getName().lastIndexOf('.') + 1).toUpperCase();
                if ("JPG".equals(format)) format = "JPEG";
                
                ImageIO.write(watermarkedImage, format, saveFile);
                updateStatus("已保存: " + saveFile.getName());
                showInfo("保存成功", "图片已成功保存到: " + saveFile.getAbsolutePath());
            } catch (IOException e) {
                showError("保存失败", "无法保存图片: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理应用水印
     */
    @FXML
    private void handleApplyWatermark(ActionEvent event) {
        if (originalImage == null) {
            showWarning("无图片", "请先导入图片");
            return;
        }

        applyWatermark();
    }

    /**
     * 应用水印并更新预览
     */
    private void applyWatermark() {
        if (originalImage == null) return;

        try {
            WatermarkProcessor processor = new WatermarkProcessor();
            
            if (textWatermarkRadio.isSelected()) {
                // 文本水印
                String text = watermarkText.getText();
                // 只有在未启用EXIF日期且文本为空时才显示警告
                if (!useExifDate.isSelected() && (text == null || text.trim().isEmpty())) {
                    // showWarning("无水印文本", "请输入水印文本或启用EXIF日期");
                    return;
                }
                
                int fontSize = (int) fontSizeSlider.getValue();
                javafx.scene.paint.Color color = textColorPicker.getValue();
                float opacity = (float) opacitySlider.getValue();
                double rotation = rotationSlider.getValue();
                boolean shadow = enableShadow.isSelected();
                boolean tiling = enableTiling.isSelected();
                
                // 转换JavaFX颜色为AWT颜色
                java.awt.Color awtColor = new java.awt.Color(
                        (float) color.getRed(),
                        (float) color.getGreen(),
                        (float) color.getBlue(),
                        opacity
                );
                
                watermarkedImage = processor.addTextWatermark(
                        originalImage,
                        text,
                        awtColor,
                        fontSize,
                        currentPosition.name(),
                        rotation,
                        shadow,
                        tiling
                );
            } else {
                // 图片水印
                if (watermarkImageFile == null || !watermarkImageFile.exists()) {
                    showWarning("无水印图片", "请先选择水印图片");
                    return;
                }
                
                BufferedImage watermarkImg = ImageIO.read(watermarkImageFile);
                float opacity = (float) opacitySlider.getValue();
                double rotation = rotationSlider.getValue();
                boolean tiling = enableTiling.isSelected();
                
                watermarkedImage = processor.addImageWatermark(
                        originalImage,
                        watermarkImg,
                        opacity,
                        currentPosition.name(),
                        rotation,
                        tiling
                );
            }
            
            // 更新预览
            Image fxImage = convertToFxImage(watermarkedImage);
            Platform.runLater(() -> previewImageView.setImage(fxImage));
            updateStatus("水印应用成功");
            
        } catch (Exception e) {
            showError("应用水印失败", "无法应用水印: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理批量添加水印
     */
    @FXML
    private void handleBatchWatermark(ActionEvent event) {
        if (imageFiles.isEmpty()) {
            showWarning("无图片", "请先导入图片");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择导出目录");
        File exportDir = fileChooser.showSaveDialog(null);
        if (exportDir != null) {
            // 确保是目录
            if (!exportDir.isDirectory()) {
                exportDir = exportDir.getParentFile();
            }
            
            if (exportDir == null) return;
            
            final File finalExportDir = exportDir;
            
            // 在后台线程处理批量任务
            isProcessing.set(true);
            executorService.submit(() -> {
                int successCount = 0;
                List<String> failedFiles = new ArrayList<>();
                
                for (ImageFile imgFile : imageFiles) {
                    try {
                        BufferedImage original = ImageIO.read(imgFile.getFile());
                        BufferedImage watermarked = applyWatermarkToImage(original, imgFile);
                        
                        if (watermarked != null) {
                            String fileName = imgFile.getFileName();
                            String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();
                            if ("JPG".equals(extension)) extension = "JPEG";
                            
                            File outputFile = new File(finalExportDir, 
                                    fileName.substring(0, fileName.lastIndexOf('.')) + "_watermark." + 
                                    extension.toLowerCase());
                            ImageIO.write(watermarked, extension, outputFile);
                            successCount++;
                        }
                    } catch (Exception e) {
                        failedFiles.add(imgFile.getFileName());
                        e.printStackTrace();
                    }
                }
                
                final int finalSuccessCount = successCount;
                final List<String> finalFailedFiles = failedFiles;
                
                Platform.runLater(() -> {
                    isProcessing.set(false);
                    StringBuilder message = new StringBuilder();
                    message.append("批量处理完成:\n");
                    message.append("成功: " + finalSuccessCount + " 张\n");
                    
                    if (!finalFailedFiles.isEmpty()) {
                        message.append("失败: " + finalFailedFiles.size() + " 张\n");
                        message.append("失败文件: " + String.join(", ", finalFailedFiles));
                    }
                    
                    showInfo("批量处理结果", message.toString());
                    updateStatus("批量水印处理完成");
                });
            });
        }
    }

    /**
     * 对单个图片应用水印（用于批量处理）
     */
    private BufferedImage applyWatermarkToImage(BufferedImage original, ImageFile imageFile) throws Exception {
        WatermarkProcessor processor = new WatermarkProcessor();
        
        if (textWatermarkRadio.isSelected()) {
            // 处理日期水印
            String text = watermarkText.getText();
            if (useExifDate.isSelected()) {
                String dateStr = ExifExtractor.extractDateTime(imageFile.getFile());
                if (dateStr != null) {
                    text = dateStr;
                }
            }
            
            int fontSize = (int) fontSizeSlider.getValue();
            javafx.scene.paint.Color color = textColorPicker.getValue();
            float opacity = (float) opacitySlider.getValue();
            double rotation = rotationSlider.getValue();
            boolean shadow = enableShadow.isSelected();
            boolean tiling = enableTiling.isSelected();
            
            java.awt.Color awtColor = new java.awt.Color(
                    (float) color.getRed(),
                    (float) color.getGreen(),
                    (float) color.getBlue(),
                    opacity
            );
            
            return processor.addTextWatermark(
                    original,
                    text,
                    awtColor,
                    fontSize,
                    currentPosition.name(),
                    rotation,
                    shadow,
                    tiling
            );
        } else {
            // 图片水印
            if (watermarkImageFile == null || !watermarkImageFile.exists()) {
                throw new IOException("水印图片不存在");
            }
            
            BufferedImage watermarkImg = ImageIO.read(watermarkImageFile);
            float opacity = (float) opacitySlider.getValue();
            double rotation = rotationSlider.getValue();
            boolean tiling = enableTiling.isSelected();
            
            return processor.addImageWatermark(
                    original,
                    watermarkImg,
                    opacity,
                    currentPosition.name(),
                    rotation,
                    tiling
            );
        }
    }

    /**
     * 设置水印位置
     */
    @FXML
    private void setWatermarkPosition(ActionEvent event) {
        javafx.scene.control.Button source = (javafx.scene.control.Button) event.getSource();
        String positionStr = (String) source.getUserData();
        currentPosition = WatermarkPosition.valueOf(positionStr);
        updatePreviewIfPossible();
    }

    /**
     * 处理选择水印图片
     */
    @FXML
    private void handleSelectWatermarkImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择水印图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            watermarkImageFile = selectedFile;
            updateStatus("已选择水印图片: " + selectedFile.getName());
            updatePreviewIfPossible();
        }
    }

    /**
     * 处理保存水印模板
     */
    @FXML
    private void handleSaveTemplate(ActionEvent event) {
        // TODO: 实现水印模板保存功能
        showInfo("功能开发中", "水印模板保存功能正在开发中");
    }

    /**
     * 处理加载水印模板
     */
    @FXML
    private void handleLoadTemplate(ActionEvent event) {
        // TODO: 实现水印模板加载功能
        showInfo("功能开发中", "水印模板加载功能正在开发中");
    }

    /**
     * 处理清除水印
     */
    @FXML
    private void handleClearWatermark(ActionEvent event) {
        if (originalImage != null) {
            Image fxImage = convertToFxImage(originalImage);
            previewImageView.setImage(fxImage);
            watermarkedImage = null;
            updateStatus("水印已清除");
        }
    }
    
    /**
     * 处理重置图片
     */
    @FXML
    private void handleResetPreview(ActionEvent event) {
        // 清除预览图片
        previewImageView.setImage(null);
        
        // 清除已导入的图片列表
        imageFiles.clear();
        
        // 重置相关变量
        selectedImageFile = null;
        originalImage = null;
        watermarkedImage = null;
        
        // 更新状态提示
        updateStatus("导入图片已重置");
    }

    /**
     * 处理帮助
     */
    @FXML
    private void handleHelp(ActionEvent event) {
        showInfo("使用帮助", "Photo Watermark 使用帮助：\n\n" +
                "1. 点击'导入图片'按钮导入需要添加水印的图片\n" +
                "2. 在右侧面板设置水印参数\n" +
                "3. 点击'应用水印'按钮预览效果\n" +
                "4. 点击'导出图片'保存水印后的图片\n\n" +
                "支持文本水印和图片水印，可调整位置、大小、透明度等参数。");
    }

    /**
     * 处理关于
     */
    @FXML
    private void handleAbout(ActionEvent event) {
        showInfo("关于", "Photo Watermark 1.0\n\n" +
                "一个简单易用的图片水印工具\n" +
                "NJUSE 2025 Fall LLM4SE hw1");
    }

    /**
     * 处理退出
     */
    @FXML
    private void handleExit(ActionEvent event) {
        executorService.shutdown();
        Platform.exit();
    }

    /**
     * 更新预览（如果可能）
     */
    private void updatePreviewIfPossible() {
        if (selectedImageFile != null && originalImage != null) {
            applyWatermark();
        }
    }

    /**
     * 更新状态栏消息
     */
    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    /**
     * 显示信息对话框
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示警告对话框
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示错误对话框
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 将AWT BufferedImage转换为JavaFX Image
     */
    private Image convertToFxImage(BufferedImage bufferedImage) {
        WritableImage writableImage = new WritableImage(
                bufferedImage.getWidth(), bufferedImage.getHeight());
        return javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, writableImage);
    }

    /**
     * 图片文件类，用于ListView显示
     */
    public static class ImageFile {
        private final File file;
        private final String fileName;

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
    }

    /**
     * 图片列表单元格渲染器
     */
    private static class ImageListCell extends ListCell<ImageFile> {
        @Override
        protected void updateItem(ImageFile item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getFileName());
            }
        }
    }
}