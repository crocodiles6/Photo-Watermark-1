package com.photowatermark.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.SplitPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主窗口控制器类 - 负责UI事件分发和协调各个管理器
 */
public class MainController {
    // GUI组件引用
    @FXML private ListView<ImageFile> imageListView;
    @FXML private ImageView previewImageView;
    @FXML private TextField watermarkText;
    @FXML private ComboBox<String> fontFamilyComboBox;
    @FXML private Slider textFontSizeSlider;
    @FXML private Label textFontSizeValue;
    @FXML private ColorPicker textColorPicker;
    @FXML private Slider textOpacitySlider;
    @FXML private Label textOpacityValue;
    @FXML private Slider textRotationSlider;
    @FXML private Label textRotationValue;
    @FXML private CheckBox enableShadow;
    @FXML private CheckBox enableStroke;
    @FXML private CheckBox useExifDate;
    @FXML private ComboBox<String> dateFormatComboBox;
    @FXML private RadioButton textWatermarkRadio;
    @FXML private RadioButton imageWatermarkRadio;
    @FXML private Button selectWatermarkImageBtn;
    @FXML private Slider imageScaleSlider;
    @FXML private Label imageScaleValue;
    @FXML private Slider imageOpacitySlider;
    @FXML private Label imageOpacityValue;
    @FXML private Slider imageRotationSlider;
    @FXML private Label imageRotationValue;
    @FXML private CheckBox enableTextTiling;
    @FXML private CheckBox enableImageTiling;
    @FXML private HBox statusBar;
    @FXML private Label statusLabel;
    @FXML private SplitPane mainSplitPane;

    // 管理器和服务类
    private UiUtils uiUtils;
    private final ImageConverter imageConverter;
    private final WatermarkService watermarkService;
    private WatermarkParameterManager parameterManager;
    private ImageFileManager imageFileManager;
    private WatermarkProcessor watermarkProcessor;
    private ExportManager exportManager;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public MainController() {
        // 在构造函数中初始化服务类
        this.imageConverter = new ImageConverter();
        this.watermarkService = new WatermarkService();
    }

    /**
     * 初始化控制器
     */
    @FXML
    public void initialize() {
        // 初始化UI工具类
        this.uiUtils = new UiUtils(statusLabel);
        
        // 初始化各个管理器
        this.parameterManager = new WatermarkParameterManager(
                watermarkText, fontFamilyComboBox, textFontSizeSlider, textColorPicker,
                textOpacitySlider, textRotationSlider, enableShadow, enableStroke,
                useExifDate, dateFormatComboBox, imageScaleSlider, imageOpacitySlider,
                imageRotationSlider, enableTextTiling, enableImageTiling
        );
        
        this.imageFileManager = new ImageFileManager(previewImageView, uiUtils, imageConverter);
        this.watermarkProcessor = new WatermarkProcessor(
                watermarkService, parameterManager, imageFileManager, uiUtils, executorService
        );
        this.exportManager = new ExportManager(uiUtils, imageConverter);
        
        // 设置图片列表
        imageListView.setItems(imageFileManager.getImageFiles());
        imageListView.setCellFactory(param -> new ImageListCell());
        imageListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> imageFileManager.setSelectedImageFile(newValue));

        // 绑定滑块与标签的更新
        bindSliderLabels();
        
        // 水印类型切换
        if (textWatermarkRadio != null) {
            textWatermarkRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                selectWatermarkImageBtn.setDisable(newVal);
            });
        }

        // 日期水印选项变更
        setupExifDateHandling();
        
        // 初始状态更新
        watermarkText.setDisable(useExifDate.isSelected());
        selectWatermarkImageBtn.setDisable(false); // 允许随时选择图片水印
        
        // 设置SplitPane初始分隔位置
        if (mainSplitPane != null) {
            mainSplitPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    // 当场景大小变化时更新SplitPane分隔位置
                    newScene.widthProperty().addListener((widthObs, oldWidth, newWidth) -> {
                        updateSplitPaneDividers();
                    });
                    
                    // 当窗口首次显示时更新SplitPane分隔位置
                    Platform.runLater(this::updateSplitPaneDividers);
                }
            });
        }
    }
    
    /**
     * 更新SplitPane分隔位置，确保左右面板各占窗口宽度的1/4
     */
    private void updateSplitPaneDividers() {
        if (mainSplitPane != null && mainSplitPane.getScene() != null) {
            double totalWidth = mainSplitPane.getScene().getWidth();
            if (totalWidth > 0) {
                // 计算分隔线位置
                double leftPaneWidth = totalWidth * 0.25; // 左侧面板占25%
                double rightPaneWidth = totalWidth * 0.25; // 右侧面板占25%
                
                // 设置分隔线位置
                java.util.List<SplitPane.Divider> dividers = mainSplitPane.getDividers();
                if (dividers.size() >= 2) {
                    dividers.get(0).setPosition(leftPaneWidth / totalWidth);
                    dividers.get(1).setPosition((leftPaneWidth + (totalWidth - leftPaneWidth - rightPaneWidth)) / totalWidth);
                }
            }
        }
    }
    
    /**
     * 绑定滑块与标签
     */
    private void bindSliderLabels() {
        textFontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            textFontSizeValue.setText(String.format("%.0f", newVal));
            updatePreviewIfPossible();
        });
        textOpacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            textOpacityValue.setText(String.format("%.1f", newVal));
            updatePreviewIfPossible();
        });
        textRotationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            textRotationValue.setText(String.format("%.0f°", newVal));
            updatePreviewIfPossible();
        });
        
        imageScaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            imageScaleValue.setText(String.format("%.1f", newVal));
            updatePreviewIfPossible();
        });
        imageOpacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            imageOpacityValue.setText(String.format("%.1f", newVal));
            updatePreviewIfPossible();
        });
        imageRotationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            imageRotationValue.setText(String.format("%.0f°", newVal));
            updatePreviewIfPossible();
        });
    }
    
    /**
     * 设置EXIF日期处理
     */
    private void setupExifDateHandling() {
        useExifDate.selectedProperty().addListener((obs, oldVal, newVal) -> {
            watermarkText.setDisable(newVal);
            
            if (newVal && !oldVal) {
                watermarkText.clear();
                if (imageFileManager.getSelectedImageFile() != null) {
                    try {
                        String dateStr = com.photowatermark.ExifExtractor.extractDateTime(imageFileManager.getSelectedImageFile().getFile());
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
            else if (!newVal && oldVal) {
                watermarkText.clear();
            }
            
            updatePreviewIfPossible();
        });
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
        imageFileManager.importImageFiles(selectedFiles);
    }

    /**
     * 处理应用水印
     */
    @FXML
    private void handleApplyWatermark(ActionEvent event) {
        if (!imageFileManager.hasSelectedImage()) {
            uiUtils.showWarning("无图片", "请先导入图片");
            return;
        }
        
        if (watermarkProcessor.validateWatermarkParameters()) {
            watermarkProcessor.applyWatermark();
        }
    }

    /**
     * 处理批量添加水印
     */
    @FXML
    private void handleBatchWatermark(ActionEvent event) {
        if (imageFileManager.getImageFiles().isEmpty()) {
            uiUtils.showWarning("无图片", "请先导入图片");
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
            
            if (exportDir != null && watermarkProcessor.validateWatermarkParameters()) {
                watermarkProcessor.batchApplyWatermark(exportDir);
            }
        }
    }

    /**
     * 设置文本水印位置
     */
    @FXML
    private void setTextWatermarkPosition(ActionEvent event) {
        Button source = (Button) event.getSource();
        String positionStr = (String) source.getUserData();
        parameterManager.setTextWatermarkPosition(WatermarkPosition.valueOf(positionStr));
        updatePreviewIfPossible();
    }

    /**
     * 设置图片水印位置
     */
    @FXML
    private void setImageWatermarkPosition(ActionEvent event) {
        Button source = (Button) event.getSource();
        String positionStr = (String) source.getUserData();
        parameterManager.setImageWatermarkPosition(WatermarkPosition.valueOf(positionStr));
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
            parameterManager.setWatermarkImageFile(selectedFile);
            uiUtils.updateStatus("已选择水印图片: " + selectedFile.getName());
            updatePreviewIfPossible();
        }
    }

    /**
     * 处理保存水印模板
     */
    @FXML
    private void handleSaveTemplate(ActionEvent event) {
        uiUtils.showInfo("功能开发中", "水印模板保存功能正在开发中");
    }

    /**
     * 处理加载水印模板
     */
    @FXML
    private void handleLoadTemplate(ActionEvent event) {
        uiUtils.showInfo("功能开发中", "水印模板加载功能正在开发中");
    }

    /**
     * 处理清除水印
     */
    @FXML
    private void handleClearWatermark() {
        // 清除水印设置
        parameterManager.clearWatermarkSettings();
        
        // 清除预览中的水印
        imageFileManager.clearWatermark();
        
        uiUtils.updateStatus("水印已清除");
    }
    
    /**
     * 处理重置图片
     */
    @FXML
    private void handleResetPreview(ActionEvent event) {
        imageFileManager.resetPreview();
    }
    
    /**
     * 处理导出图片
     */
    @FXML
    private void handleExportImages(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG图片", "*.png"),
                new FileChooser.ExtensionFilter("JPEG图片", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("BMP图片", "*.bmp")
        );
        
        // 设置默认文件名
        fileChooser.setInitialFileName(exportManager.generateDefaultExportFileName(imageFileManager.getSelectedImageFile()));
        
        File outputFile = fileChooser.showSaveDialog(null);
        if (outputFile != null) {
            exportManager.exportWatermarkedImage(
                    imageFileManager.getWatermarkedImage(), 
                    outputFile, 
                    imageFileManager.getSelectedImageFile()
            );
        }
    }

    /**
     * 处理帮助
     */
    @FXML
    private void handleHelp(ActionEvent event) {
        uiUtils.showInfo("使用帮助", "Photo Watermark 使用帮助：\n\n" +
                "1. 点击'导入图片'按钮导入需要添加水印的图片\n" +
                "2. 在右侧面板设置水印参数\n" +
                "   - 文本水印：设置文字内容、字体、大小、颜色、透明度等\n" +
                "   - 图片水印：选择水印图片、设置缩放、透明度、旋转等\n" +
                "3. 点击'应用水印'按钮预览效果\n" +
                "4. 点击'导出图片'保存水印后的图片\n\n" +
                "支持同时添加文本水印和图片水印，可独立设置各自的属性。");
    }

    /**
     * 处理关于
     */
    @FXML
    private void handleAbout(ActionEvent event) {
        uiUtils.showInfo("关于", "Photo Watermark 1.0\n\n" +
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
        if (imageFileManager.hasSelectedImage()) {
            watermarkProcessor.applyWatermark();
        }
    }
}