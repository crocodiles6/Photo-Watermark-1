package com.photowatermark;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * 图片水印处理器 - 专门负责处理图片水印的添加
 */
public class ImageWatermarkProcessor {
    
    /**
     * 添加图片水印（新方法，支持缩放）
     */
    public BufferedImage addImageWatermark(
            BufferedImage originalImage, 
            BufferedImage watermarkImage, 
            float scale,
            float opacity, 
            String positionStr, 
            double rotation, 
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
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // 绘制原始图像
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 获取Position枚举值
        Position position = Position.valueOf(positionStr);
        
        // 设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        
        // 应用缩放
        BufferedImage scaledWatermark = scaleWatermarkImage(watermarkImage, scale);
        
        if (tiling) {
            // 平铺水印
            drawTiledImageWatermark(g2d, originalImage, scaledWatermark, rotation);
        } else {
            // 单一水印，对于CUSTOM位置，默认使用(0.5, 0.5)即中心位置
            double customX = 0.5;
            double customY = 0.5;
            drawSingleImageWatermark(g2d, originalImage, scaledWatermark, position, rotation, customX, customY);
        }
        
        // 释放资源
        g2d.dispose();
        
        return watermarkedImage;
    }
    
    /**
     * 添加图片水印（带自定义位置支持）
     */
    public BufferedImage addImageWatermark(
            BufferedImage originalImage, 
            BufferedImage watermarkImage, 
            float scale,
            float opacity, 
            String positionStr, 
            double rotation, 
            boolean tiling,
            double customX, // 自定义X坐标 (0-1)
            double customY  // 自定义Y坐标 (0-1)
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
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // 绘制原始图像
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 获取Position枚举值
        Position position = Position.valueOf(positionStr);
        
        // 设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        
        // 应用缩放
        BufferedImage scaledWatermark = scaleWatermarkImage(watermarkImage, scale);
        
        if (tiling) {
            // 平铺水印
            drawTiledImageWatermark(g2d, originalImage, scaledWatermark, rotation);
        } else {
            // 单一水印，传递自定义位置
            drawSingleImageWatermark(g2d, originalImage, scaledWatermark, position, rotation, customX, customY);
        }
        
        // 释放资源
        g2d.dispose();
        
        return watermarkedImage;
    }
    
    /**
     * 缩放水印图片
     */
    private BufferedImage scaleWatermarkImage(BufferedImage watermarkImage, float scale) {
        if (scale == 1.0f) {
            return watermarkImage; // 无需缩放
        }
        
        int scaledWidth = (int) (watermarkImage.getWidth() * scale);
        int scaledHeight = (int) (watermarkImage.getHeight() * scale);
        
        // 创建缩放后的图像
        BufferedImage scaledImage = new BufferedImage(
                scaledWidth, 
                scaledHeight, 
                BufferedImage.TYPE_INT_ARGB);
        
        // 绘制缩放后的图像
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(watermarkImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        return scaledImage;
    }
    
    /**
     * 绘制单一图片水印
     */
    private void drawSingleImageWatermark(
            Graphics2D g2d, 
            BufferedImage image, 
            BufferedImage watermarkImage, 
            Position position, 
            double rotation,
            double customX, // 自定义X坐标 (0-1)
            double customY  // 自定义Y坐标 (0-1)
    ) {
        int wmWidth = watermarkImage.getWidth();
        int wmHeight = watermarkImage.getHeight();
        int margin = 20;
        int x = 0;
        int y = 0;
        
        // 根据位置计算坐标
        if (position == Position.CUSTOM) {
            // 使用自定义坐标
            x = (int) (customX * (image.getWidth() - wmWidth)) - margin / 2;
            y = (int) (customY * (image.getHeight() - wmHeight)) - margin / 2;
            
            // 确保坐标在有效范围内
            x = Math.max(margin, Math.min(x, image.getWidth() - wmWidth - margin));
            y = Math.max(margin, Math.min(y, image.getHeight() - wmHeight - margin));
        } else {
            // 使用预设位置
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
     * 添加图片水印（旧方法，保持向后兼容）
     */
    public BufferedImage addImageWatermark(
            BufferedImage originalImage, 
            BufferedImage watermarkImage, 
            float opacity, 
            String positionStr, 
            double rotation, 
            boolean tiling
    ) {
        // 调用新方法，传入默认缩放值
        return addImageWatermark(
                originalImage, 
                watermarkImage, 
                1.0f, // 默认缩放为1.0
                opacity, 
                positionStr, 
                rotation, 
                tiling
        );
    }
}