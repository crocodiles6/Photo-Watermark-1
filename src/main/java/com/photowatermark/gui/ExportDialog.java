package com.photowatermark.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;

/**
 * 导出对话框 - 负责处理导出选项的用户交互
 */
public class ExportDialog {
    private final ExportDialogResult result = new ExportDialogResult();
    private final ExportManager exportManager;
    private final ImageFile selectedImageFile;
    private String defaultFileName;
    private String originalFileNameWithoutExtension;
    
    /**
     * 导出对话框结果类，封装用户的选择
     */
    public static class ExportDialogResult {
        private File exportDirectory;
        private String fileName;
        private String format;
        private int scalePercentage = 100; // 默认100%
        private int jpegQuality = 90; // 默认90%质量
        private boolean confirmed = false;
        
        public File getExportDirectory() {
            return exportDirectory;
        }
        
        public void setExportDirectory(File exportDirectory) {
            this.exportDirectory = exportDirectory;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFormat() {
            return format;
        }
        
        public void setFormat(String format) {
            this.format = format;
        }
        
        public int getScalePercentage() {
            return scalePercentage;
        }
        
        public void setScalePercentage(int scalePercentage) {
            this.scalePercentage = scalePercentage;
        }
        
        public int getJpegQuality() {
            return jpegQuality;
        }
        
        public void setJpegQuality(int jpegQuality) {
            this.jpegQuality = jpegQuality;
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public void setConfirmed(boolean confirmed) {
            this.confirmed = confirmed;
        }
    }
    
    public ExportDialog(ExportManager exportManager, ImageFile selectedImageFile) {
        this.exportManager = exportManager;
        this.selectedImageFile = selectedImageFile;
        // 生成默认文件名（不包含扩展名）
        String defaultFullName = exportManager.generateDefaultExportFileName(selectedImageFile);
        this.defaultFileName = defaultFullName.substring(0, defaultFullName.lastIndexOf('.'));
        
        // 提取原始文件名（不包含扩展名）
        String originalFileName = selectedImageFile.getFileName();
        int dotIndex = originalFileName.lastIndexOf('.');
        this.originalFileNameWithoutExtension = dotIndex > 0 ? originalFileName.substring(0, dotIndex) : originalFileName;
    }
    
    /**
     * 显示导出对话框并返回用户选择的结果
     */
    public ExportDialogResult showDialog(Window owner) {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("导出图片");
        
        // 创建布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // 导出目录选择
        Label directoryLabel = new Label("导出目录：");
        TextField directoryField = new TextField();
        directoryField.setEditable(false);
        directoryField.setPrefWidth(300);
        Button browseButton = new Button("浏览...");
        
        browseButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择导出目录");
            // 如果有默认的桌面路径，可以设置
            File desktop = new File(System.getProperty("user.home") + "/Desktop");
            if (desktop.exists() && desktop.isDirectory()) {
                directoryChooser.setInitialDirectory(desktop);
            }
            
            File selectedDir = directoryChooser.showDialog(dialogStage);
            if (selectedDir != null) {
                result.setExportDirectory(selectedDir);
                directoryField.setText(selectedDir.getAbsolutePath());
            }
        });
        
        HBox directoryBox = new HBox(5);
        directoryBox.getChildren().addAll(directoryField, browseButton);
        directoryBox.setHgrow(directoryField, Priority.ALWAYS);
        
        // 文件名输入
        Label fileNameLabel = new Label("文件名：");
        TextField fileNameField = new TextField(defaultFileName);
        fileNameField.setPromptText("输入文件名，不包含扩展名");
        
        // 添加文件名设置选项
        Label fileNameOptionLabel = new Label("文件名选项：");
        Button originalNameButton = new Button("原文件名");
        Button prefixButton = new Button("默认前缀");
        Button suffixButton = new Button("默认后缀");
        
        // 设置按钮事件处理
        originalNameButton.setOnAction(e -> {
            fileNameField.setText(originalFileNameWithoutExtension);
        });
        
        prefixButton.setOnAction(e -> {
            fileNameField.setText("wm_" + originalFileNameWithoutExtension);
        });
        
        suffixButton.setOnAction(e -> {
            fileNameField.setText(originalFileNameWithoutExtension + "_watermarked");
        });
        
        // 创建按钮容器
        HBox fileNameOptionsBox = new HBox(10);
        fileNameOptionsBox.getChildren().addAll(originalNameButton, prefixButton, suffixButton);
        
        // 导出格式选择
        Label formatLabel = new Label("导出格式：");
        ComboBox<String> formatComboBox = new ComboBox<>();
        formatComboBox.getItems().addAll("PNG", "JPEG");
        formatComboBox.getSelectionModel().select(0); // 默认选择PNG
        
        // 图片缩放设置
        Label scaleLabel = new Label("缩放比例：");
        Slider scaleSlider = new Slider(10, 200, 100); // 10% - 200%，默认100%
        scaleSlider.setShowTickMarks(true);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setMajorTickUnit(50);
        scaleSlider.setMinorTickCount(5);
        scaleSlider.setSnapToTicks(true);
        
        Label scaleValueLabel = new Label("100%");
        scaleSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int scaleValue = (int) newValue.intValue();
            scaleValueLabel.setText(scaleValue + "%");
        });
        
        HBox scaleBox = new HBox(10);
        scaleBox.getChildren().addAll(scaleSlider, scaleValueLabel);
        scaleBox.setHgrow(scaleSlider, Priority.ALWAYS);
        
        // JPEG质量设置
        Label qualityLabel = new Label("JPEG质量：");
        Slider qualitySlider = new Slider(0, 100, 90); // 0-100，默认90%
        qualitySlider.setShowTickMarks(true);
        qualitySlider.setShowTickLabels(true);
        qualitySlider.setMajorTickUnit(20);
        qualitySlider.setMinorTickCount(4);
        qualitySlider.setSnapToTicks(true);
        qualitySlider.setDisable(true); // 初始禁用
        
        Label qualityValueLabel = new Label("90%");
        qualitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int qualityValue = (int) newValue.intValue();
            qualityValueLabel.setText(qualityValue + "%");
        });
        
        HBox qualityBox = new HBox(10);
        qualityBox.getChildren().addAll(qualitySlider, qualityValueLabel);
        qualityBox.setHgrow(qualitySlider, Priority.ALWAYS);
        
        // 根据导出格式启用/禁用质量滑块
        formatComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isJpeg = "JPEG".equals(newValue);
            qualitySlider.setDisable(!isJpeg);
            qualityLabel.setDisable(!isJpeg);
        });
        
        // 按钮
        Button confirmButton = new Button("确定");
        Button cancelButton = new Button("取消");
        
        confirmButton.setOnAction(e -> {
            if (result.getExportDirectory() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText(null);
                alert.setContentText("请选择导出目录");
                alert.showAndWait();
                return;
            }
            
            // 设置结果
            result.setFileName(fileNameField.getText());
            result.setFormat(formatComboBox.getValue());
            result.setScalePercentage((int) scaleSlider.getValue());
            result.setJpegQuality((int) qualitySlider.getValue());
            result.setConfirmed(true);
            dialogStage.close();
        });
        
        cancelButton.setOnAction(e -> {
            dialogStage.close();
        });
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(confirmButton, cancelButton);
        
        // 添加组件到网格
        grid.add(directoryLabel, 0, 0);
        grid.add(directoryBox, 1, 0);
        grid.add(fileNameLabel, 0, 1);
        grid.add(fileNameField, 1, 1);
        grid.add(fileNameOptionLabel, 0, 2);
        grid.add(fileNameOptionsBox, 1, 2);
        grid.add(formatLabel, 0, 3);
        grid.add(formatComboBox, 1, 3);
        grid.add(scaleLabel, 0, 4);
        grid.add(scaleBox, 1, 4);
        grid.add(qualityLabel, 0, 5);
        grid.add(qualityBox, 1, 5);
        grid.add(buttonBox, 1, 6);
        
        // 设置列约束，使第二列可以水平扩展
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(column1, column2);
        
        // 创建场景并显示
        Scene scene = new Scene(grid);
        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
        
        return result;
    }
}