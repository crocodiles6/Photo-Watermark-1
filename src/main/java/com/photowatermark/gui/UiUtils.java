package com.photowatermark.gui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * UI工具类，提供状态更新和对话框显示等功能
 */
public class UiUtils {
    private final Label statusLabel;

    public UiUtils(Label statusLabel) {
        this.statusLabel = statusLabel;
    }

    /**
     * 更新状态栏消息
     */
    public void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    /**
     * 显示信息对话框
     */
    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示警告对话框
     */
    public void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示错误对话框
     */
    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}