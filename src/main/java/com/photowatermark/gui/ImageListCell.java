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
            
            // 创建文件名标签
            Label fileNameLabel = new Label(item.getFileName());
            fileNameLabel.setFont(Font.font(12));
            fileNameLabel.setPrefWidth(200); // 设置足够的宽度显示文件名
            fileNameLabel.setWrapText(true); // 允许文件名换行
            
            // 将缩略图和文件名添加到布局中
            cellLayout.getChildren().addAll(thumbnail, fileNameLabel);
            
            // 设置单元格的图形为布局
            setGraphic(cellLayout);
            // 清空文本，因为我们使用图形显示所有内容
            setText(null);
        }
    }
}