package com.photowatermark.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX应用程序的主入口类
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 加载FXML文件
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MainWindow.fxml")));
            
            // 设置场景
            Scene scene = new Scene(root, 1000, 700);
            
            // 设置窗口标题和图标
            primaryStage.setTitle("Photo Watermark - 图片水印工具");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("无法加载FXML文件: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}