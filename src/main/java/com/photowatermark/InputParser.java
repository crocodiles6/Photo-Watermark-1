package com.photowatermark;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.photowatermark.WatermarkProcessor.Position;

/**
 * 专门负责解析用户输入的工具类
 */
public class InputParser {
    
    /**
     * 从字符串解析颜色
     */
    public static Color parseColor(String colorStr, Color defaultColor) {
        if (colorStr == null || colorStr.isEmpty()) {
            return defaultColor;
        }
        
        // 尝试解析十六进制颜色值
        try {
            if (!colorStr.startsWith("#")) {
                return Color.decode("#" + colorStr);
            } else {
                return Color.decode(colorStr);
            }
        } catch (NumberFormatException e1) {
            // 尝试解析预定义颜色
            try {
                return (Color) Color.class.getField(colorStr.toUpperCase()).get(null);
            } catch (Exception e2) {
                // 尝试解析RGB格式
                Pattern rgbPattern = Pattern.compile("^(\\d{1,3}),(\\d{1,3}),(\\d{1,3})$");
                Matcher matcher = rgbPattern.matcher(colorStr);
                if (matcher.matches()) {
                    try {
                        int r = Integer.parseInt(matcher.group(1));
                        int g = Integer.parseInt(matcher.group(2));
                        int b = Integer.parseInt(matcher.group(3));
                        // 确保RGB值在有效范围内
                        r = Math.max(0, Math.min(255, r));
                        g = Math.max(0, Math.min(255, g));
                        b = Math.max(0, Math.min(255, b));
                        return new Color(r, g, b);
                    } catch (NumberFormatException e3) {
                        // 忽略，返回默认颜色
                    }
                }
            }
        }
        
        System.out.println("颜色格式错误，使用默认颜色。");
        return defaultColor;
    }
    
    /**
     * 从字符串解析位置
     */
    public static Position parsePosition(String positionStr, Position defaultPosition) {
        if (positionStr == null || positionStr.isEmpty()) {
            return defaultPosition;
        }
        
        try {
            return Position.valueOf(positionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("位置格式错误，使用默认位置。");
            return defaultPosition;
        }
    }
    
    /**
     * 解析字体大小
     */
    public static int parseFontSize(String fontSizeStr, int defaultSize) {
        if (fontSizeStr == null || fontSizeStr.isEmpty()) {
            return defaultSize;
        }
        
        try {
            int size = Integer.parseInt(fontSizeStr);
            if (size <= 0) {
                System.out.println("字体大小无效，使用默认值。");
                return defaultSize;
            }
            return size;
        } catch (NumberFormatException e) {
            System.out.println("字体大小格式错误，使用默认值。");
            return defaultSize;
        }
    }
}