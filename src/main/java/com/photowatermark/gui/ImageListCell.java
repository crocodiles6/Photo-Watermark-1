package com.photowatermark.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 * 图片列表单元格渲染器
 */
public class ImageListCell extends ListCell<ImageFile> {
    @Override
    protected void updateItem(ImageFile item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            // 创建水平布局容器
            HBox cellLayout = new HBox(10);
            cellLayout.setPadding(new Insets(5));
            
            // 创建缩略图ImageView
            ImageView thumbnail = new ImageView();
            thumbnail.setFitWidth(60);
            thumbnail.setFitHeight(60);
            thumbnail.setPreserveRatio(true);
            thumbnail.setSmooth(true);
            
            // 从文件加载缩略图（使用JavaFX内置的Image类，支持懒加载）
            Image image = new Image(item.getFile().toURI().toString(), 60, 60, true, true);
            thumbnail.setImage(image);
            
            // 创建文件名标签，优化长文件名的换行显示
            Label fileNameLabel = new Label(item.getFileName());
            fileNameLabel.setFont(Font.font(12));
            fileNameLabel.setPrefWidth(200); // 设置合适的宽度以适应列表
            fileNameLabel.setMaxWidth(Double.MAX_VALUE); // 允许标签填充可用空间
            fileNameLabel.setWrapText(true); // 启用自动换行
            fileNameLabel.setAlignment(javafx.geometry.Pos.CENTER_LEFT); // 左对齐文本
            fileNameLabel.setMinHeight(60); // 设置最小高度以适应缩略图
            
            // 设置CSS样式以优化换行效果
            fileNameLabel.setStyle("-fx-text-alignment: left; -fx-line-spacing: 2px;");
            
            // 将缩略图和文件名添加到布局中
            cellLayout.getChildren().addAll(thumbnail, fileNameLabel);
            
            // 设置单元格的图形为布局
            setGraphic(cellLayout);
            // 清空文本，因为我们使用图形显示所有内容
            setText(null);
            
            // 设置单元格的最小高度以确保内容完整显示
            setMinHeight(70);
        }
    }
}