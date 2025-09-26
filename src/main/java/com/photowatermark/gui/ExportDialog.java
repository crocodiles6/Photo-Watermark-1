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
    
    /**
     * 导出对话框结果类，封装用户的选择
     */
    public static class ExportDialogResult {
        private File exportDirectory;
        private String fileName;
        private String format;
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
        
        // 导出格式选择
        Label formatLabel = new Label("导出格式：");
        ComboBox<String> formatComboBox = new ComboBox<>();
        formatComboBox.getItems().addAll("PNG", "JPEG");
        formatComboBox.getSelectionModel().select(0); // 默认选择PNG
        
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
        grid.add(formatLabel, 0, 2);
        grid.add(formatComboBox, 1, 2);
        grid.add(buttonBox, 1, 3);
        
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