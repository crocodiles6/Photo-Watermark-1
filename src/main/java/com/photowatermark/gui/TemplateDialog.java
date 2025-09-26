package com.photowatermark.gui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 水印模板对话框 - 用于保存模板和显示模板列表
 */
public class TemplateDialog extends Dialog<Void> {
    private final WatermarkParameterManager parameterManager;
    private final WatermarkTemplateManager templateManager;
    private final OnTemplateSelectedListener listener;
    
    public interface OnTemplateSelectedListener {
        void onTemplateSelected(WatermarkTemplate template);
    }
    
    public TemplateDialog(WatermarkParameterManager parameterManager, 
                         WatermarkTemplateManager templateManager, 
                         OnTemplateSelectedListener listener) {
        this.parameterManager = parameterManager;
        this.templateManager = templateManager;
        this.listener = listener;
        
        initDialog();
        createContent();
    }
    
    private void initDialog() {
        setTitle("水印模板管理");
        setResizable(false);
        
        // 设置对话框按钮
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        
        // 自定义关闭按钮行为
        Button closeButton = (Button) getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setText("关闭");
    }
    
    private void createContent() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);
        
        // 创建保存模板部分
        HBox saveTemplateBox = new HBox(10);
        saveTemplateBox.setAlignment(Pos.CENTER);
        
        Label templateNameLabel = new Label("模板名称:");
        TextField templateNameField = new TextField();
        templateNameField.setPromptText("请输入模板名称");
        templateNameField.setPrefWidth(200);
        
        Button saveTemplateButton = new Button("保存当前设置为模板");
        saveTemplateButton.setOnAction(e -> {
            String templateName = templateNameField.getText().trim();
            if (!templateName.isEmpty()) {
                saveTemplate(templateName);
                templateNameField.clear();
            } else {
                showAlert(Alert.AlertType.WARNING, "提示", "请输入模板名称");
            }
        });
        
        saveTemplateBox.getChildren().addAll(templateNameLabel, templateNameField, saveTemplateButton);
        
        // 创建模板列表部分
        VBox templateListBox = new VBox(10);
        templateListBox.setAlignment(Pos.TOP_LEFT);
        
        Label templateListLabel = new Label("已保存的模板:");
        ObservableList<String> templateNames = FXCollections.observableArrayList(
                templateManager.getTemplateNames());
        
        ListView<String> templateListView = new ListView<>(templateNames);
        templateListView.setPrefHeight(200);
        templateListView.setPrefWidth(350);
        
        HBox templateActionsBox = new HBox(10);
        templateActionsBox.setAlignment(Pos.CENTER);
        
        Button applyTemplateButton = new Button("应用选中模板");
        applyTemplateButton.setOnAction(e -> {
            String selectedTemplateName = templateListView.getSelectionModel().getSelectedItem();
            if (selectedTemplateName != null) {
                applyTemplate(selectedTemplateName);
            } else {
                showAlert(Alert.AlertType.WARNING, "提示", "请先选择一个模板");
            }
        });
        
        Button deleteTemplateButton = new Button("删除选中模板");
        deleteTemplateButton.setOnAction(e -> {
            String selectedTemplateName = templateListView.getSelectionModel().getSelectedItem();
            if (selectedTemplateName != null) {
                deleteTemplate(selectedTemplateName, templateNames);
            } else {
                showAlert(Alert.AlertType.WARNING, "提示", "请先选择一个模板");
            }
        });
        
        templateActionsBox.getChildren().addAll(applyTemplateButton, deleteTemplateButton);
        templateListBox.getChildren().addAll(templateListLabel, templateListView, templateActionsBox);
        
        // 添加所有部分到根容器
        root.getChildren().addAll(saveTemplateBox, new Separator(), templateListBox);
        
        // 设置对话框内容
        getDialogPane().setContent(root);
        
        // 设置场景样式
        Scene scene = getDialogPane().getScene();
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
    }
    
    private void saveTemplate(String templateName) {
        if (templateManager.isTemplateNameExists(templateName)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认覆盖");
            alert.setHeaderText("模板已存在");
            alert.setContentText("是否覆盖已存在的模板？");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    WatermarkTemplate template = new WatermarkTemplate(templateName, parameterManager);
                    templateManager.addTemplate(template);
                    showAlert(Alert.AlertType.INFORMATION, "成功", "模板已更新");
                    refreshTemplateList();
                }
            });
        } else {
            WatermarkTemplate template = new WatermarkTemplate(templateName, parameterManager);
            templateManager.addTemplate(template);
            showAlert(Alert.AlertType.INFORMATION, "成功", "模板已保存");
            refreshTemplateList();
        }
    }
    
    private void applyTemplate(String templateName) {
        WatermarkTemplate template = templateManager.findTemplate(templateName);
        if (template != null) {
            template.applyTo(parameterManager);
            if (listener != null) {
                listener.onTemplateSelected(template);
            }
        }
    }
    
    private void deleteTemplate(String templateName, ObservableList<String> templateNames) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("删除模板");
        alert.setContentText("确定要删除模板 \"" + templateName + "\" 吗？");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                templateManager.removeTemplate(templateName);
                templateNames.remove(templateName);
                showAlert(Alert.AlertType.INFORMATION, "成功", "模板已删除");
            }
        });
    }
    
    private void refreshTemplateList() {
        ListView<String> templateListView = (ListView<String>) getDialogPane().lookup("ListView");
        if (templateListView != null) {
            ObservableList<String> templateNames = FXCollections.observableArrayList(
                    templateManager.getTemplateNames());
            templateListView.setItems(templateNames);
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}