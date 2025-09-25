package com.photowatermark;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.imaging.ImageReadException;

import com.photowatermark.Position;

/**
 * 图片水印程序主类
 * 遵循单一职责原则，主要负责用户交互和协调各个组件的工作
 */
public class PhotoWatermarkApp {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // 获取用户输入的图片路径
            System.out.println("请输入图片文件路径：");
            String imagePath = scanner.nextLine().trim();
            
            // 移除可能的不可见字符（如零宽空格）
            imagePath = imagePath.replaceAll("[\\p{Cf}]", "");
            
            File imageFile = new File(imagePath);
            if (!imageFile.exists() || !imageFile.isFile()) {
                System.out.println("错误：指定的文件不存在或不是有效的文件路径。");
                System.out.println("使用的路径：" + imagePath);
                System.out.println("当前工作目录：" + System.getProperty("user.dir"));
                return;
            }
            
            // 获取字体大小
            System.out.println("请输入水印字体大小（默认为24）：");
            String fontSizeStr = scanner.nextLine().trim();
            int fontSize = InputParser.parseFontSize(fontSizeStr, 24);
            
            // 获取字体颜色
            System.out.println("请输入水印字体颜色（支持格式：RED, GREEN, BLUE, BLACK, WHITE 或 RGB值如：255,0,0）：");
            String colorStr = scanner.nextLine().trim();
            Color watermarkColor = InputParser.parseColor(colorStr, Color.RED);
            
            // 获取水印位置
            System.out.println("请输入水印位置（TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT，默认为BOTTOM_RIGHT）：");
            String positionStr = scanner.nextLine().trim();
            Position position = InputParser.parsePosition(positionStr, Position.BOTTOM_RIGHT);
            
            // 提取EXIF信息中的拍摄时间
            String dateTime = ExifExtractor.extractDateTime(imageFile);
            String watermarkText = dateTime;
            
            // 处理图片并添加水印
            WatermarkProcessor.processImage(imageFile, watermarkText, fontSize, watermarkColor, position);
            
            System.out.println("水印添加成功！");
        } catch (IOException | ImageReadException e) {
            System.out.println("处理过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}