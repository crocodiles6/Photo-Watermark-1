package com.photowatermark;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * 文本水印处理器 - 专门负责处理文本水印的添加
 */
public class TextWatermarkProcessor {
    
    /**
     * 添加文本水印（新方法，支持字体和描边）
     */
    public BufferedImage addTextWatermark(
            BufferedImage originalImage, 
            String text, 
            Color color, 
            String fontFamily,
            int fontSize, 
            String positionStr, 
            double rotation, 
            boolean shadow, 
            boolean stroke,
            boolean tiling
    ) {
        // 创建可绘制的图像副本
        BufferedImage watermarkedImage = new BufferedImage(
                originalImage.getWidth(), 
                originalImage.getHeight(), 
                BufferedImage.TYPE_INT_ARGB);
        
        // 获取图形上下文
        Graphics2D g2d = watermarkedImage.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制原始图像
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 设置字体
        Font font = new Font(fontFamily, Font.BOLD, fontSize);
        g2d.setFont(font);
        
        // 设置颜色（包含透明度）
        g2d.setColor(color);
        
        // 获取Position枚举值
        Position position = Position.valueOf(positionStr);
        
        if (tiling) {
            // 平铺水印
            drawTiledTextWatermark(g2d, originalImage, text, font, color, rotation, shadow, stroke);
        } else {
            // 单一水印
            drawSingleTextWatermark(g2d, originalImage, text, font, color, position, rotation, shadow, stroke);
        }
        
        // 释放资源
        g2d.dispose();
        
        return watermarkedImage;
    }
    
    /**
     * 绘制单一文本水印
     */
    private void drawSingleTextWatermark(
            Graphics2D g2d, 
            BufferedImage image, 
            String text, 
            Font font, 
            Color color, 
            Position position, 
            double rotation, 
            boolean shadow,
            boolean stroke
    ) {
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        int margin = 20;
        int x = 0;
        int y = 0;
        
        // 根据位置计算坐标
        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = margin + textHeight - metrics.getDescent();
                break;
            case TOP_CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = margin + textHeight - metrics.getDescent();
                break;
            case TOP_RIGHT:
                x = image.getWidth() - textWidth - margin;
                y = margin + textHeight - metrics.getDescent();
                break;
            case CENTER_LEFT:
                x = margin;
                y = (image.getHeight() + textHeight) / 2 - metrics.getDescent();
                break;
            case CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = (image.getHeight() + textHeight) / 2 - metrics.getDescent();
                break;
            case CENTER_RIGHT:
                x = image.getWidth() - textWidth - margin;
                y = (image.getHeight() + textHeight) / 2 - metrics.getDescent();
                break;
            case BOTTOM_LEFT:
                x = margin;
                y = image.getHeight() - margin - metrics.getDescent();
                break;
            case BOTTOM_CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = image.getHeight() - margin - metrics.getDescent();
                break;
            case BOTTOM_RIGHT:
            default:
                x = image.getWidth() - textWidth - margin;
                y = image.getHeight() - margin - metrics.getDescent();
                break;
        }
        
        // 保存当前变换状态
        AffineTransform originalTransform = g2d.getTransform();
        
        if (rotation != 0) {
            // 旋转水印
            g2d.translate(x + textWidth / 2, y - textHeight / 2 + metrics.getAscent());
            g2d.rotate(Math.toRadians(rotation));
            
            if (shadow) {
                // 添加阴影
                g2d.setColor(new Color(0, 0, 0, 100));
                if (stroke) {
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawString(text, 2, 2);
                    g2d.setStroke(new BasicStroke(1));
                }
                g2d.drawString(text, 2, 2);
                g2d.setColor(color);
            }
            
            // 绘制描边
            if (stroke) {
                g2d.setStroke(new BasicStroke(2));
                g2d.drawString(text, -textWidth / 2, textHeight / 2 - metrics.getDescent());
                g2d.setStroke(new BasicStroke(1));
            }
            
            // 绘制文本
            g2d.drawString(text, -textWidth / 2, textHeight / 2 - metrics.getDescent());
        } else {
            // 不旋转
            if (shadow) {
                // 添加阴影
                g2d.setColor(new Color(0, 0, 0, 100));
                if (stroke) {
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawString(text, x + 2, y + 2);
                    g2d.setStroke(new BasicStroke(1));
                }
                g2d.drawString(text, x + 2, y + 2);
                g2d.setColor(color);
            }
            
            // 绘制描边
            if (stroke) {
                g2d.setStroke(new BasicStroke(2));
                g2d.drawString(text, x, y);
                g2d.setStroke(new BasicStroke(1));
            }
            
            // 绘制文本
            g2d.drawString(text, x, y);
        }
        
        // 恢复原始变换
        g2d.setTransform(originalTransform);
    }
    
    /**
     * 绘制平铺文本水印
     */
    private void drawTiledTextWatermark(
            Graphics2D g2d, 
            BufferedImage image, 
            String text, 
            Font font, 
            Color color, 
            double rotation, 
            boolean shadow,
            boolean stroke
    ) {
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        int tileWidth = textWidth * 2;
        int tileHeight = textHeight * 2;
        
        // 保存当前变换状态
        AffineTransform originalTransform = g2d.getTransform();
        
        if (rotation != 0) {
            g2d.rotate(Math.toRadians(rotation));
        }
        
        // 平铺绘制水印
        for (int x = -tileWidth; x < image.getWidth() + tileWidth; x += tileWidth) {
            for (int y = -tileHeight; y < image.getHeight() + tileHeight; y += tileHeight) {
                if (shadow) {
                    // 添加阴影
                    g2d.setColor(new Color(0, 0, 0, 100));
                    if (stroke) {
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawString(text, x + 2, y + 2 + metrics.getAscent());
                        g2d.setStroke(new BasicStroke(1));
                    }
                    g2d.drawString(text, x + 2, y + 2 + metrics.getAscent());
                    g2d.setColor(color);
                }
                
                // 绘制描边
                if (stroke) {
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawString(text, x, y + metrics.getAscent());
                    g2d.setStroke(new BasicStroke(1));
                }
                
                // 绘制文本
                g2d.drawString(text, x, y + metrics.getAscent());
            }
        }
        
        // 恢复原始变换
        g2d.setTransform(originalTransform);
    }
    
    /**
     * 计算水印位置（兼容原有方法）
     */
    public int[] calculateWatermarkPosition(
            BufferedImage image, 
            Graphics2D g2d, 
            String text, 
            Position position
    ) {
        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        int margin = 20;
        int x = 0;
        int y = 0;
        
        // 根据位置计算坐标
        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = margin + textHeight - metrics.getDescent();
                break;
            case TOP_CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = margin + textHeight - metrics.getDescent();
                break;
            case TOP_RIGHT:
                x = image.getWidth() - textWidth - margin;
                y = margin + textHeight - metrics.getDescent();
                break;
            case CENTER_LEFT:
                x = margin;
                y = (image.getHeight() + textHeight) / 2 - metrics.getDescent();
                break;
            case CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = (image.getHeight() + textHeight) / 2 - metrics.getDescent();
                break;
            case CENTER_RIGHT:
                x = image.getWidth() - textWidth - margin;
                y = (image.getHeight() + textHeight) / 2 - metrics.getDescent();
                break;
            case BOTTOM_LEFT:
                x = margin;
                y = image.getHeight() - margin - metrics.getDescent();
                break;
            case BOTTOM_CENTER:
                x = (image.getWidth() - textWidth) / 2;
                y = image.getHeight() - margin - metrics.getDescent();
                break;
            case BOTTOM_RIGHT:
            default:
                x = image.getWidth() - textWidth - margin;
                y = image.getHeight() - margin - metrics.getDescent();
                break;
        }
        
        return new int[] {x, y};
    }
    
    /**
     * 添加文本水印（旧方法，保持向后兼容）
     */
    public BufferedImage addTextWatermark(
            BufferedImage originalImage, 
            String text, 
            Color color, 
            int fontSize, 
            String positionStr, 
            double rotation, 
            boolean shadow, 
            boolean tiling
    ) {
        // 调用新方法，传入默认值
        return addTextWatermark(
                originalImage, 
                text, 
                color, 
                "Arial", // 默认字体
                fontSize, 
                positionStr, 
                rotation, 
                shadow, 
                false, // 默认不使用描边
                tiling
        );
    }
}