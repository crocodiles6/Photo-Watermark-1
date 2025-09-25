package com.photowatermark.gui;

import javafx.scene.control.ListCell;

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
            setText(item.getFileName());
        }
    }
}