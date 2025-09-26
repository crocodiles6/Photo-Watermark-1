package com.photowatermark;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * 组合水印处理器 - 专门负责同时处理文本和图片水印
 */
public class CombinedWatermarkProcessor {
    
    /**
     * 同时添加文本水印和图片水印
     */
    public BufferedImage addCombinedWatermark(
            BufferedImage originalImage, 
            String text, 
            Color textColor, 
            int fontSize, 
            String textPositionStr, 
            double textRotation, 
            boolean textShadow, 
            boolean textTiling, 
            BufferedImage watermarkImage, 
            float imageOpacity, 
            String imagePositionStr, 
            double imageRotation, 
            boolean imageTiling
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
        
        // 先添加图片水印
        if (watermarkImage != null) {
            // 设置透明度
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, imageOpacity));
            
            // 获取Position枚举值
            Position imagePosition = Position.valueOf(imagePositionStr);
            
            if (imageTiling) {
                // 平铺图片水印
                drawTiledImageWatermark(g2d, originalImage, watermarkImage, imageRotation);
            } else {
                // 单一图片水印
                drawSingleImageWatermark(g2d, originalImage, watermarkImage, imagePosition, imageRotation);
            }
        }
        
        // 再添加文本水印
        if (text != null && !text.trim().isEmpty()) {
            // 设置字体
            Font font = new Font("Arial", Font.BOLD, fontSize);
            g2d.setFont(font);
            
            // 设置颜色（文本颜色已包含透明度信息）
            g2d.setColor(textColor);
            
            // 获取Position枚举值
            Position textPosition = Position.valueOf(textPositionStr);
            
            if (textTiling) {
                // 平铺文本水印
                drawTiledTextWatermark(g2d, originalImage, text, font, textColor, textRotation, textShadow);
            } else {
                // 单一文本水印
                drawSingleTextWatermark(g2d, originalImage, text, font, textColor, textPosition, textRotation, textShadow);
            }
        }
        
        // 释放资源
        g2d.dispose();
        
        return watermarkedImage;
    }
    
    /**
     * 绘制单一图片水印
     */
    private void drawSingleImageWatermark(
            Graphics2D g2d, 
            BufferedImage image, 
            BufferedImage watermarkImage, 
            Position position, 
            double rotation
    ) {
        int wmWidth = watermarkImage.getWidth();
        int wmHeight = watermarkImage.getHeight();
        int margin = 20;
        int x = 0;
        int y = 0;
        
        // 根据位置计算坐标
        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = margin;
                break;
            case TOP_CENTER:
                x = (image.getWidth() - wmWidth) / 2;
                y = margin;
                break;
            case TOP_RIGHT:
                x = image.getWidth() - wmWidth - margin;
                y = margin;
                break;
            case CENTER_LEFT:
                x = margin;
                y = (image.getHeight() - wmHeight) / 2;
                break;
            case CENTER:
                x = (image.getWidth() - wmWidth) / 2;
                y = (image.getHeight() - wmHeight) / 2;
                break;
            case CENTER_RIGHT:
                x = image.getWidth() - wmWidth - margin;
                y = (image.getHeight() - wmHeight) / 2;
                break;
            case BOTTOM_LEFT:
                x = margin;
                y = image.getHeight() - wmHeight - margin;
                break;
            case BOTTOM_CENTER:
                x = (image.getWidth() - wmWidth) / 2;
                y = image.getHeight() - wmHeight - margin;
                break;
            case BOTTOM_RIGHT:
            default:
                x = image.getWidth() - wmWidth - margin;
                y = image.getHeight() - wmHeight - margin;
                break;
        }
        
        // 保存当前变换状态
        AffineTransform originalTransform = g2d.getTransform();
        
        if (rotation != 0) {
            // 旋转水印
            g2d.translate(x + wmWidth / 2, y + wmHeight / 2);
            g2d.rotate(Math.toRadians(rotation));
            g2d.drawImage(watermarkImage, -wmWidth / 2, -wmHeight / 2, null);
        } else {
            // 不旋转
            g2d.drawImage(watermarkImage, x, y, null);
        }
        
        // 恢复原始变换
        g2d.setTransform(originalTransform);
    }
    
    /**
     * 绘制平铺图片水印
     */
    private void drawTiledImageWatermark(
            Graphics2D g2d, 
            BufferedImage image, 
            BufferedImage watermarkImage, 
            double rotation
    ) {
        int wmWidth = watermarkImage.getWidth();
        int wmHeight = watermarkImage.getHeight();
        
        // 保存当前变换状态
        AffineTransform originalTransform = g2d.getTransform();
        
        // 平铺绘制水印，每个水印绕自己的中心旋转
        for (int x = -wmWidth; x < image.getWidth() + wmWidth; x += wmWidth * 2) {
            for (int y = -wmHeight; y < image.getHeight() + wmHeight; y += wmHeight * 2) {
                // 保存当前变换状态
                AffineTransform tileTransform = new AffineTransform(originalTransform);
                g2d.setTransform(tileTransform);
                
                if (rotation != 0) {
                    // 移动到水印位置，然后绕水印中心旋转
                    g2d.translate(x + wmWidth / 2, y + wmHeight / 2);
                    g2d.rotate(Math.toRadians(rotation));
                    g2d.drawImage(watermarkImage, -wmWidth / 2, -wmHeight / 2, null);
                } else {
                    // 不旋转，直接绘制
                    g2d.drawImage(watermarkImage, x, y, null);
                }
            }
        }
        
        // 恢复原始变换
        g2d.setTransform(originalTransform);
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
            boolean shadow
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
                g2d.drawString(text, 2, 2);
                g2d.setColor(color);
            }
            
            g2d.drawString(text, -textWidth / 2, textHeight / 2 - metrics.getDescent());
        } else {
            // 不旋转
            if (shadow) {
                // 添加阴影
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(text, x + 2, y + 2);
                g2d.setColor(color);
            }
            
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
            boolean shadow
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
                    g2d.drawString(text, x + 2, y + 2 + metrics.getAscent());
                    g2d.setColor(color);
                }
                g2d.drawString(text, x, y + metrics.getAscent());
            }
        }
        
        // 恢复原始变换
        g2d.setTransform(originalTransform);
    }
}