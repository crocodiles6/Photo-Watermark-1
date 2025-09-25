package com.photowatermark.gui;

import java.io.File;

/**
 * 图片文件类，用于ListView显示
 */
public class ImageFile {
    private final File file;
    private final String fileName;

    public ImageFile(File file) {
        this.file = file;
        this.fileName = file.getName();
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return fileName;
    }
}