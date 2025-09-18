# 图片水印程序（Photo-Watermark）

这是一个Java命令行程序，用于为图片添加基于拍摄日期的水印。

NJUSE 2025 Fall LLM4SE hw1

## 功能特性

- 读取图片文件的EXIF信息，提取拍摄日期（年月日）作为水印文本
- 支持自定义水印字体大小、颜色和位置
- 将添加水印后的图片保存到原目录的子目录中

## 技术栈

- Java 11
- Maven（项目管理和构建）
- Apache Commons Imaging（EXIF信息提取）

## 构建项目

确保已安装Java 11和Maven，然后执行以下命令：

```bash
mvn clean package
```

构建成功后，将在`target`目录下生成可执行的JAR文件。

## 运行程序

使用以下命令运行程序：

```bash
java -jar target/photo-watermark-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 使用说明

1. 程序运行后，按照提示输入图片文件路径
2. 输入水印字体大小（默认为24）
3. 输入水印字体颜色（支持RED, GREEN等预定义颜色或RGB值如255,0,0）
4. 输入水印位置（支持TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT）
5. 程序将处理图片并保存到原目录下的`原目录名_watermark`子目录中

## 注意事项

- 如果图片没有EXIF信息或无法提取拍摄日期，程序将使用文件的修改时间作为水印
- 支持常见的图片格式如JPG、PNG等

## 许可证

MIT License