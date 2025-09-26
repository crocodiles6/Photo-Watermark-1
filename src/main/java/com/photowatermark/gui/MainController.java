package com.photowatermark.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.SplitPane;
import javafx.geometry.Bounds;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
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
    @FXML private CheckBox enableTextCustomPosition;
    @FXML private CheckBox enableImageCustomPosition;
    @FXML private HBox statusBar;
    @FXML private Label statusLabel;
    @FXML private SplitPane mainSplitPane;
    @FXML private Pane previewContainer; // 添加对预览容器的引用

    // 管理器和服务类
    private UiUtils uiUtils;
    private final ImageConverter imageConverter;
    private final WatermarkService watermarkService;
    private WatermarkParameterManager parameterManager;
    private ImageFileManager imageFileManager;
    private WatermarkProcessor watermarkProcessor;
    private ExportManager exportManager;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    
    // 水印选中和拖拽相关变量
    private boolean isDragging = false;
    private boolean isWatermarkSelected = false;
    private boolean isTextWatermarkActive = false;
    private boolean isTextWatermarkDraggable = false;
    private boolean isImageWatermarkDraggable = false;
    private double dragStartX, dragStartY;
    private double watermarkX, watermarkY, watermarkWidth, watermarkHeight;
    private Rectangle watermarkBorder;
    private final Color SELECTED_BORDER_COLOR = Color.GREEN;
    private final double BORDER_THICKNESS = 2.0;
    private final int WATERMARK_HIT_TOLERANCE = 20; // 增加点击容错范围到20像素
    private final int MIN_WATERMARK_SIZE = 50; // 最小水印大小，确保可点击
    
    /**
     * 绑定文本自定义位置变化事件
     */
    private void setupCustomPositionBinding() {
        // 确保UI元素和parameterManager状态同步
        if (enableTextCustomPosition != null) {
            // 首先将ParameterManager的状态同步到UI
            boolean currentState = parameterManager.isUseCustomTextPosition();
            enableTextCustomPosition.setSelected(currentState);
            
            enableTextCustomPosition.selectedProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("文本自定义位置选项变化: " + newValue);
                // 直接调用setter方法确保状态更新
                parameterManager.setUseCustomTextPosition(newValue);
                
                // 再次验证状态是否正确同步
                boolean pmState = parameterManager.isUseCustomTextPosition();
                System.out.println("设置后ParameterManager状态: " + pmState);
                
                if (newValue) {
                    parameterManager.setTextWatermarkPosition(WatermarkPosition.CUSTOM);
                    // 立即计算水印位置
                    calculateWatermarkPosition();
                } else {
                    // 恢复到默认位置
                    parameterManager.setTextWatermarkPosition(WatermarkPosition.BOTTOM_RIGHT);
                    // 隐藏边框
                    hideWatermarkBorder();
                    isWatermarkSelected = false;
                }
                updatePreviewIfPossible();
            });
        }
        
        // 绑定图片自定义位置变化事件
        if (enableImageCustomPosition != null) {
            // 首先将ParameterManager的状态同步到UI
            boolean currentState = parameterManager.isUseCustomImagePosition();
            enableImageCustomPosition.setSelected(currentState);
            
            enableImageCustomPosition.selectedProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("图片自定义位置选项变化: " + newValue);
                // 直接调用setter方法确保状态更新
                parameterManager.setUseCustomImagePosition(newValue);
                
                // 再次验证状态是否正确同步
                boolean pmState = parameterManager.isUseCustomImagePosition();
                System.out.println("设置后ParameterManager状态: " + pmState);
                
                if (newValue) {
                    parameterManager.setImageWatermarkPosition(WatermarkPosition.CUSTOM);
                    // 立即计算水印位置
                    calculateWatermarkPosition();
                } else {
                    // 恢复到默认位置
                    parameterManager.setImageWatermarkPosition(WatermarkPosition.TOP_LEFT);
                    // 隐藏边框
                    hideWatermarkBorder();
                    isWatermarkSelected = false;
                }
                updatePreviewIfPossible();
            });
        }
    }
    
    /**
     * 处理鼠标按下事件，开始拖拽
     */
    @FXML
    private void handleMousePressed(MouseEvent event) {
        // 首先检查是否启用了自定义位置模式
        boolean useCustomPosition = enableTextCustomPosition.isSelected() || enableImageCustomPosition.isSelected();
        
        // 同时也检查parameterManager中的设置
        boolean pmCustomPosition = parameterManager.isUseCustomTextPosition() || parameterManager.isUseCustomImagePosition();
        
        System.out.println("自定义位置模式状态 - UI复选框: " + useCustomPosition + ", ParameterManager: " + pmCustomPosition);
        
        // 强制同步状态
        if (useCustomPosition != pmCustomPosition) {
            System.out.println("状态不同步，进行强制同步");
            if (enableTextCustomPosition.isSelected()) {
                parameterManager.setUseCustomTextPosition(true);
                parameterManager.setTextWatermarkPosition(WatermarkPosition.CUSTOM);
            } else {
                parameterManager.setUseCustomTextPosition(false);
            }
            if (enableImageCustomPosition.isSelected()) {
                parameterManager.setUseCustomImagePosition(true);
                parameterManager.setImageWatermarkPosition(WatermarkPosition.CUSTOM);
            } else {
                parameterManager.setUseCustomImagePosition(false);
            }
            // 重新获取状态
            pmCustomPosition = parameterManager.isUseCustomTextPosition() || parameterManager.isUseCustomImagePosition();
            System.out.println("强制同步后ParameterManager状态: " + pmCustomPosition);
        }
        
        ImageFile currentImageFile = imageFileManager.getSelectedImageFile();
        if (currentImageFile == null || previewImageView.getImage() == null) {
            System.out.println("没有选中图片或预览为空");
            return;
        }
        
        // 无论是否有水印内容，都先计算水印位置
        calculateWatermarkPosition();
        
        // 检查是否有水印内容
        boolean hasTextWatermark = parameterManager.hasTextWatermark();
        boolean hasImageWatermark = parameterManager.hasImageWatermark();
        System.out.println("水印内容状态 - 文本: " + hasTextWatermark + ", 图片: " + hasImageWatermark);
        
        // 确保水印区域大小不为0
        if (watermarkWidth < MIN_WATERMARK_SIZE || watermarkHeight < MIN_WATERMARK_SIZE) {
            System.out.println("水印区域过小，使用最小大小");
            watermarkWidth = Math.max(watermarkWidth, MIN_WATERMARK_SIZE);
            watermarkHeight = Math.max(watermarkHeight, MIN_WATERMARK_SIZE);
        }
        
        // 获取鼠标在场景中的坐标
        double mouseX = event.getSceneX();
        double mouseY = event.getSceneY();
        
        // 获取预览图在场景中的坐标边界
        Bounds imageBounds = previewImageView.getBoundsInLocal();
        Bounds sceneBounds = previewImageView.localToScene(imageBounds);
        
        // 检查鼠标是否在预览图范围内
        boolean isInPreviewArea = mouseX >= sceneBounds.getMinX() && 
                                 mouseX <= sceneBounds.getMaxX() && 
                                 mouseY >= sceneBounds.getMinY() && 
                                 mouseY <= sceneBounds.getMaxY();
        
        System.out.println("鼠标是否在预览区域内: " + isInPreviewArea);
        System.out.println("鼠标按下位置: " + mouseX + ", " + mouseY);
        System.out.println("水印区域: X=" + watermarkX + ", Y=" + watermarkY + ", W=" + watermarkWidth + ", H=" + watermarkHeight);
        
        // 使用改进的点击检测算法
        boolean clickedOnWatermark = false;
        if (isInPreviewArea) {
            clickedOnWatermark = isPointInWatermark(mouseX, mouseY);
        }
        
        // 重置拖拽状态
        isDragging = false;
        
        if (clickedOnWatermark) {
            // 选中水印
            isWatermarkSelected = true;
            showWatermarkBorder();
            
            // 开始拖拽并记录起始位置，确保handleMouseDragged可以实现连续拖动
            isDragging = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            
            System.out.println("水印被选中并准备拖拽");
        } else {
            // 如果点击在水印外但在预览区内，且启用了自定义位置，可以创建新的水印位置
            if (isInPreviewArea && (useCustomPosition || pmCustomPosition)) {
                System.out.println("点击在预览区域但水印外，设置新的水印位置");
                
                // 计算相对位置
                double relativeX = (mouseX - sceneBounds.getMinX()) / sceneBounds.getWidth();
                double relativeY = (mouseY - sceneBounds.getMinY()) / sceneBounds.getHeight();
                
                // 限制在有效范围内
                relativeX = Math.max(0.0, Math.min(1.0, relativeX));
                relativeY = Math.max(0.0, Math.min(1.0, relativeY));
                
                // 更新位置
                if (enableTextCustomPosition.isSelected() || parameterManager.isUseCustomTextPosition()) {
                    currentImageFile.setCustomTextWatermarkX(relativeX);
                    currentImageFile.setCustomTextWatermarkY(relativeY);
                    System.out.println("设置新文本水印位置: " + relativeX + ", " + relativeY);
                }
                if (enableImageCustomPosition.isSelected() || parameterManager.isUseCustomImagePosition()) {
                    currentImageFile.setCustomImageWatermarkX(relativeX);
                    currentImageFile.setCustomImageWatermarkY(relativeY);
                    System.out.println("设置新图片水印位置: " + relativeX + ", " + relativeY);
                }
                
                // 选中并显示水印
                isWatermarkSelected = true;
                showWatermarkBorder();
                
                // 开始拖拽以支持连续拖动
                isDragging = true;
                dragStartX = mouseX;
                dragStartY = mouseY;
                
                // 重新计算位置
                calculateWatermarkPosition();
                updateBorderPosition();
                
                // 应用水印变更
                updatePreviewIfPossible();
            } else {
                // 点击在预览区域外，取消选中
                isWatermarkSelected = false;
                hideWatermarkBorder();
                System.out.println("点击在水印区域外");
            }
        }
        
        // 阻止事件冒泡，防止ScrollPane捕获事件
        event.consume();
    }
    
    /**
     * 处理鼠标拖拽事件，移动水印位置
     */
    @FXML
    private void handleMouseDragged(MouseEvent event) {
        if (!isDragging || !isWatermarkSelected) {
            return;
        }
        
        ImageFile currentImageFile = imageFileManager.getSelectedImageFile();
        if (currentImageFile == null || previewImageView.getImage() == null) {
            return;
        }
        
        // 获取预览图片尺寸
        Image image = previewImageView.getImage();
        Bounds bounds = previewImageView.getBoundsInLocal();
        Bounds sceneBounds = previewImageView.localToScene(bounds);
        
        // 计算缩放比例
        double scaleX = sceneBounds.getWidth() / image.getWidth();
        double scaleY = sceneBounds.getHeight() / image.getHeight();
        double scale = Math.min(scaleX, scaleY);
        
        // 计算图片在预览区域中的实际显示尺寸
        double displayWidth = image.getWidth() * scale;
        double displayHeight = image.getHeight() * scale;
        double offsetX = sceneBounds.getMinX() + (sceneBounds.getWidth() - displayWidth) / 2;
        double offsetY = sceneBounds.getMinY() + (sceneBounds.getHeight() - displayHeight) / 2;
        
        // 获取鼠标当前位置
        double currentX = event.getSceneX();
        double currentY = event.getSceneY();
        
        // 直接将水印位置设置为鼠标位置（考虑水印中心）
        double newWatermarkX = currentX - watermarkWidth / 2;
        double newWatermarkY = currentY - watermarkHeight / 2;
        
        // 限制水印在预览区域内
        newWatermarkX = Math.max(offsetX, Math.min(offsetX + displayWidth - watermarkWidth, newWatermarkX));
        newWatermarkY = Math.max(offsetY, Math.min(offsetY + displayHeight - watermarkHeight, newWatermarkY));
        
        // 更新水印绝对位置
        watermarkX = newWatermarkX;
        watermarkY = newWatermarkY;
        
        // 将新的水印位置转换为相对位置并更新
        double relativeX = (watermarkX - offsetX) / displayWidth;
        double relativeY = (watermarkY - offsetY) / displayHeight;
        
        // 限制在有效范围内
        relativeX = Math.max(0.0, Math.min(1.0, relativeX));
        relativeY = Math.max(0.0, Math.min(1.0, relativeY));
        
        // 更新水印位置信息
        if (enableTextCustomPosition.isSelected() || parameterManager.isUseCustomTextPosition()) {
            currentImageFile.setCustomTextWatermarkX(relativeX);
            currentImageFile.setCustomTextWatermarkY(relativeY);
        }
        
        if (enableImageCustomPosition.isSelected() || parameterManager.isUseCustomImagePosition()) {
            currentImageFile.setCustomImageWatermarkX(relativeX);
            currentImageFile.setCustomImageWatermarkY(relativeY);
        }
        
        // 立即更新水印预览，实现实时拖动效果
        updatePreviewIfPossible();
        
        // 更新拖拽起始位置
        dragStartX = currentX;
        dragStartY = currentY;
        
        // 更新边框位置，确保边框实时跟随水印移动
        updateBorderPosition();
        
        System.out.println("水印连续拖动中，当前位置: X=" + watermarkX + ", Y=" + watermarkY + ", 水印预览已更新");
        
        // 阻止事件冒泡
        event.consume();
    }
    
    /**
     * 处理鼠标释放事件，结束水印拖拽
     */
    @FXML
    private void handleMouseReleased(MouseEvent event) {
        if (isDragging) {
            isDragging = false;
            
            System.out.println("水印拖拽结束，当前位置: X=" + watermarkX + ", Y=" + watermarkY);
        }
        
        // 阻止事件冒泡
        event.consume();
    }
    
    /**
     * 计算水印在场景中的位置和大小
     */
    private void calculateWatermarkPosition() {
        ImageFile currentImageFile = imageFileManager.getSelectedImageFile();
        if (currentImageFile == null || previewImageView.getImage() == null) {
            System.out.println("没有选中图片或预览为空，无法计算水印位置");
            return;
        }
        
        // 获取预览图片信息
        Image image = previewImageView.getImage();
        Bounds bounds = previewImageView.getBoundsInLocal();
        Bounds sceneBounds = previewImageView.localToScene(bounds);
        
        // 计算缩放比例
        double scaleX = sceneBounds.getWidth() / image.getWidth();
        double scaleY = sceneBounds.getHeight() / image.getHeight();
        double scale = Math.min(scaleX, scaleY);
        
        // 计算图片在预览区域中的实际显示位置和尺寸
        double displayWidth = image.getWidth() * scale;
        double displayHeight = image.getHeight() * scale;
        double offsetX = sceneBounds.getMinX() + (sceneBounds.getWidth() - displayWidth) / 2;
        double offsetY = sceneBounds.getMinY() + (sceneBounds.getHeight() - displayHeight) / 2;
        
        System.out.println("预览图片信息 - 显示宽度: " + displayWidth + ", 显示高度: " + displayHeight + ", 偏移X: " + offsetX + ", 偏移Y: " + offsetY);
        
        // 优先使用自定义位置模式
        boolean useTextCustomPosition = parameterManager.isUseCustomTextPosition();
        boolean useImageCustomPosition = parameterManager.isUseCustomImagePosition();
        
        // 获取水印类型和内容信息
        boolean hasTextWatermark = parameterManager.hasTextWatermark();
        boolean hasImageWatermark = parameterManager.hasImageWatermark();
        
        System.out.println("自定义位置模式 - 文本: " + useTextCustomPosition + ", 图片: " + useImageCustomPosition);
        System.out.println("水印内容状态 - 文本: " + hasTextWatermark + ", 图片: " + hasImageWatermark);
        
        // 获取水印的相对位置（0.0-1.0）
        double relativeX, relativeY;
        
        // 确定使用哪种水印
        if (useTextCustomPosition || (hasTextWatermark && (!useImageCustomPosition || !hasImageWatermark))) {
            // 使用文本水印位置
            if (useTextCustomPosition) {
                relativeX = currentImageFile.getCustomTextWatermarkX();
                relativeY = currentImageFile.getCustomTextWatermarkY();
                System.out.println("使用自定义文本水印位置: " + relativeX + ", " + relativeY);
            } else {
                // 使用默认位置
                relativeX = 0.5; // 居中
                relativeY = 0.5; // 居中
                System.out.println("使用默认文本水印位置");
            }
            // 文本水印大小计算
            watermarkWidth = displayWidth * 0.15;
            watermarkHeight = displayHeight * 0.08;
            isTextWatermarkActive = true;
        } else if (useImageCustomPosition || hasImageWatermark) {
            // 使用图片水印位置
            if (useImageCustomPosition) {
                relativeX = currentImageFile.getCustomImageWatermarkX();
                relativeY = currentImageFile.getCustomImageWatermarkY();
                System.out.println("使用自定义图片水印位置: " + relativeX + ", " + relativeY);
            } else {
                // 使用默认位置
                relativeX = 0.5; // 居中
                relativeY = 0.5; // 居中
                System.out.println("使用默认图片水印位置");
            }
            // 图片水印大小计算
            watermarkWidth = displayWidth * 0.25;
            watermarkHeight = displayHeight * 0.25;
            isTextWatermarkActive = false;
        } else {
            // 默认情况，设置在图片中心
            relativeX = 0.5;
            relativeY = 0.5;
            watermarkWidth = displayWidth * 0.2; // 默认宽度
            watermarkHeight = displayHeight * 0.1; // 默认高度
            isTextWatermarkActive = true; // 默认假设是文本水印
            System.out.println("使用默认水印配置");
        }
        
        // 确保相对坐标在有效范围内
        relativeX = Math.max(0.0, Math.min(1.0, relativeX));
        relativeY = Math.max(0.0, Math.min(1.0, relativeY));
        
        // 计算水印在场景中的绝对位置
        watermarkX = offsetX + (displayWidth - watermarkWidth) * relativeX;
        watermarkY = offsetY + (displayHeight - watermarkHeight) * relativeY;
        
        // 确保水印不会超出预览区域
        watermarkX = Math.max(offsetX, Math.min(offsetX + displayWidth - watermarkWidth, watermarkX));
        watermarkY = Math.max(offsetY, Math.min(offsetY + displayHeight - watermarkHeight, watermarkY));
        
        // 确保水印区域大小不为0
        watermarkWidth = Math.max(watermarkWidth, MIN_WATERMARK_SIZE);
        watermarkHeight = Math.max(watermarkHeight, MIN_WATERMARK_SIZE);
        
        System.out.println("水印位置计算完成: X=" + watermarkX + ", Y=" + watermarkY + ", W=" + watermarkWidth + ", H=" + watermarkHeight);
    }
    
    /**
     * 更新水印位置
     */
    private void updateWatermarkPosition(ImageFile currentImageFile, double deltaX, double deltaY) {
        // 更新文本水印位置
        if (parameterManager.isUseCustomTextPosition()) {
            double newX = currentImageFile.getCustomTextWatermarkX() + deltaX;
            double newY = currentImageFile.getCustomTextWatermarkY() + deltaY;
            // 限制在有效范围内
            newX = Math.max(0.0, Math.min(1.0, newX));
            newY = Math.max(0.0, Math.min(1.0, newY));
            currentImageFile.setCustomTextWatermarkX(newX);
            currentImageFile.setCustomTextWatermarkY(newY);
        }
        
        // 更新图片水印位置
        if (parameterManager.isUseCustomImagePosition()) {
            double newX = currentImageFile.getCustomImageWatermarkX() + deltaX;
            double newY = currentImageFile.getCustomImageWatermarkY() + deltaY;
            // 限制在有效范围内
            newX = Math.max(0.0, Math.min(1.0, newX));
            newY = Math.max(0.0, Math.min(1.0, newY));
            currentImageFile.setCustomImageWatermarkX(newX);
            currentImageFile.setCustomImageWatermarkY(newY);
        }
    }
    
    /**
     * 检查点是否在水印区域内，使用改进的算法
     */
    private boolean isPointInWatermark(double x, double y) {
        // 扩大点击区域，增加容错范围
        boolean inArea = x >= watermarkX - WATERMARK_HIT_TOLERANCE && 
                         x <= watermarkX + watermarkWidth + WATERMARK_HIT_TOLERANCE &&
                         y >= watermarkY - WATERMARK_HIT_TOLERANCE && 
                         y <= watermarkY + watermarkHeight + WATERMARK_HIT_TOLERANCE;
        
        System.out.println("点是否在水印区域内(含容错): " + inArea + ", 容错范围: " + WATERMARK_HIT_TOLERANCE + "px");
        return inArea;
    }
    
    /**
     * 显示水印边框
     */
    private void showWatermarkBorder() {
        // 确保边框对象已创建
        if (watermarkBorder == null) {
            watermarkBorder = new Rectangle();
            watermarkBorder.setStroke(SELECTED_BORDER_COLOR);
            watermarkBorder.setStrokeWidth(BORDER_THICKNESS);
            watermarkBorder.setFill(Color.TRANSPARENT);
            // 添加虚线样式，使边框更明显
            watermarkBorder.getStrokeDashArray().addAll(5d, 5d);
        }
        
        // 更新边框位置和大小
        updateBorderPosition();
        
        // 将边框添加到父容器 - 优先使用previewContainer
        Pane parentPane = null;
        
        // 检查previewContainer是否存在
        if (previewContainer != null && previewContainer instanceof Pane) {
            parentPane = previewContainer;
        } else {
            // 尝试获取previewImageView的父容器
            if (previewImageView.getParent() instanceof Pane) {
                parentPane = (Pane) previewImageView.getParent();
            }
        }
        
        if (parentPane != null) {
            // 确保边框只被添加一次
            if (!parentPane.getChildren().contains(watermarkBorder)) {
                parentPane.getChildren().add(watermarkBorder);
                System.out.println("水印边框已添加到容器中");
            }
            watermarkBorder.setVisible(true);
            watermarkBorder.toFront(); // 确保边框在最上层
            System.out.println("水印边框已显示在容器中");
        } else {
            System.out.println("无法获取预览容器，边框添加失败");
        }
    }
    
    /**
     * 更新边框位置
     */
    private void updateBorderPosition() {
        if (watermarkBorder != null) {
            // 根据用户要求，不再显示绿色边框
            watermarkBorder.setVisible(false);
            System.out.println("根据用户要求，边框已设置为不可见");
        }
    }
    
    /**
     * 隐藏水印边框
     */
    private void hideWatermarkBorder() {
        if (watermarkBorder != null) {
            watermarkBorder.setVisible(false);
            System.out.println("水印边框已隐藏");
        }
    }
    
    /**
     * 重写鼠标进入事件，重新计算水印位置
     */
    @FXML
    private void handleMouseEntered(MouseEvent event) {
        // 当鼠标进入时重新计算水印位置，确保位置准确
        calculateWatermarkPosition();
        if (isWatermarkSelected && watermarkBorder != null) {
            updateBorderPosition();
        }
    }
    
    /**
     * 重写鼠标退出事件
     */
    @FXML
    private void handleMouseExited(MouseEvent event) {
        // 鼠标退出时不需要取消选中
    }

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
        
        // 设置图片选择变化回调，当切换图片时自动应用水印
        imageFileManager.setOnImageSelectedCallback(imageFile -> {
            // 检查是否有水印参数设置，如果有，自动更新预览
            updatePreviewIfPossible();
        });
        
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
        
        // 设置自定义位置绑定
        setupCustomPositionBinding();
        
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
        
        // 初始化水印边框属性
        if (watermarkBorder == null) {
            watermarkBorder = new Rectangle();
            watermarkBorder.setStroke(SELECTED_BORDER_COLOR);
            watermarkBorder.setStrokeWidth(BORDER_THICKNESS);
            watermarkBorder.setFill(Color.TRANSPARENT);
            watermarkBorder.setVisible(false);
        }
        
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
        
        System.out.println("MainController初始化完成，水印选中功能已启用");
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
        // 文本水印内容实时预览
        watermarkText.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePreviewIfPossible();
        });
        
        // 字体样式实时预览
        fontFamilyComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePreviewIfPossible();
        });
        
        // 文本颜色实时预览
        textColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePreviewIfPossible();
        });
        
        // 文本水印阴影效果实时预览
        enableShadow.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updatePreviewIfPossible();
        });
        
        // 文本水印平铺效果实时预览
        enableTextTiling.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updatePreviewIfPossible();
        });
        
        // 图片水印平铺效果实时预览
        enableImageTiling.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updatePreviewIfPossible();
        });
        
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
     * 处理从文件夹导入图片
     */
    @FXML
    private void handleImportFromFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择图片文件夹");
        
        // 显示文件夹选择对话框
        File selectedFolder = directoryChooser.showDialog(null);
        if (selectedFolder != null) {
            // 调用ImageFileManager从文件夹导入图片
            imageFileManager.importImagesFromFolder(selectedFolder);
        }
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
     * 设置文本水印位置
     */
    @FXML
    private void setTextWatermarkPosition(ActionEvent event) {
        Button source = (Button) event.getSource();
        String positionStr = (String) source.getUserData();
        try {
            WatermarkPosition position = WatermarkPosition.valueOf(positionStr);
            parameterManager.setTextWatermarkPosition(position);
            // 关闭自定义位置选项
            if (enableTextCustomPosition != null) {
                enableTextCustomPosition.setSelected(false);
                parameterManager.setUseCustomTextPosition(false);
            }
            updatePreviewIfPossible();
        } catch (IllegalArgumentException e) {
            uiUtils.showError("位置设置错误", "无效的水印位置");
        }
    }

    /**
     * 设置图片水印位置
     */
    @FXML
    private void setImageWatermarkPosition(ActionEvent event) {
        Button source = (Button) event.getSource();
        String positionStr = (String) source.getUserData();
        try {
            WatermarkPosition position = WatermarkPosition.valueOf(positionStr);
            parameterManager.setImageWatermarkPosition(position);
            // 关闭自定义位置选项
            if (enableImageCustomPosition != null) {
                enableImageCustomPosition.setSelected(false);
                parameterManager.setUseCustomImagePosition(false);
            }
            updatePreviewIfPossible();
        } catch (IllegalArgumentException e) {
            uiUtils.showError("位置设置错误", "无效的水印位置");
        }
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
        if (!imageFileManager.hasSelectedImage() || imageFileManager.getWatermarkedImage() == null) {
            uiUtils.showWarning("无图片", "请先应用水印后再导出");
            return;
        }
        
        // 创建并显示导出对话框
        ExportDialog exportDialog = new ExportDialog(exportManager, imageFileManager.getSelectedImageFile());
        ExportDialog.ExportDialogResult result = exportDialog.showDialog(previewImageView.getScene().getWindow());
        
        if (result.isConfirmed()) {
            try {
                // 生成最终的文件名（包含扩展名）
                String finalFileName = exportManager.generateFileNameWithExtension(
                        result.getFileName(), result.getFormat());
                
                // 创建完整的输出文件路径
                File outputFile = new File(result.getExportDirectory(), finalFileName);
                
                // 检查文件是否已存在
                if (outputFile.exists()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("文件已存在");
                    alert.setHeaderText(null);
                    alert.setContentText("文件已存在，是否覆盖？");
                    
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            // 执行导出
                            exportManager.exportWatermarkedImage(
                                    imageFileManager.getWatermarkedImage(),
                                    outputFile,
                                    imageFileManager.getSelectedImageFile(),
                                    result.getScalePercentage(),
                                    result.getJpegQuality()
                            );
                        }
                    });
                } else {
                    // 文件不存在，直接导出
                    exportManager.exportWatermarkedImage(
                            imageFileManager.getWatermarkedImage(),
                            outputFile,
                            imageFileManager.getSelectedImageFile(),
                            result.getScalePercentage(),
                            result.getJpegQuality()
                    );
                }
            } catch (Exception e) {
                uiUtils.showError("导出失败", "导出过程中发生错误：" + e.getMessage());
                e.printStackTrace();
            }
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
     * 处理拖拽经过事件
     */
    @FXML
    private void handleDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        // 检查是否包含文件，且至少有一个是图片文件
        if (db.hasFiles() && !db.getFiles().isEmpty()) {
            File file = db.getFiles().get(0);
            if (isImageFile(file)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        event.consume();
    }

    /**
     * 处理拖拽释放事件
     */
    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        if (db.hasFiles() && !db.getFiles().isEmpty()) {
            File file = db.getFiles().get(0);
            if (isImageFile(file)) {
                // 使用现有的imageFileManager导入单个图片文件
                imageFileManager.importImageFiles(Arrays.asList(file));
                success = true;
            } else {
                uiUtils.showWarning("不支持的文件格式", "请导入有效的图片文件");
            }
        }
        
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * 检查文件是否为图片文件
     */
    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || fileName.endsWith(".bmp") || 
               fileName.endsWith(".tiff");
    }
    
    /**
     * 更新预览（如果可能）
     */
    private void updatePreviewIfPossible() {
        if (imageFileManager.hasSelectedImage()) {
            // 检查是否有水印内容
            boolean hasTextWatermark = parameterManager.hasTextWatermark();
            boolean hasImageWatermark = parameterManager.hasImageWatermark();
            
            // 如果文本水印为空且没有图片水印，清空预览
            if (!hasTextWatermark && !hasImageWatermark) {
                imageFileManager.clearWatermark();
            } else {
                watermarkProcessor.applyWatermark();
            }
        }
    }
}