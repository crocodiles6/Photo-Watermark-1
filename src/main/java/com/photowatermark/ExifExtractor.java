package com.photowatermark;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;

/**
 * 专门负责提取图片的EXIF信息，特别是拍摄日期
 */
public class ExifExtractor {
    
    /**
     * 提取图片的拍摄时间
     */
    public static String extractDateTime(File imageFile) throws IOException, ImageReadException {
        try {
            ImageMetadata metadata = Imaging.getMetadata(imageFile);
            
            if (metadata instanceof JpegImageMetadata) {
                JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                
                // 尝试获取EXIF信息
                TiffImageMetadata exif = jpegMetadata.getExif();
                if (exif != null) {
                    // 获取拍摄日期
                    TiffField dateTimeOriginal = exif.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                    if (dateTimeOriginal != null) {
                        String dateTimeString = dateTimeOriginal.getStringValue();
                        return formatDateTime(dateTimeString);
                    }
                    
                    // 如果没有原始日期，尝试获取修改日期
                    TiffField dateTime = exif.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                    if (dateTime != null) {
                        String dateTimeString = dateTime.getStringValue();
                        return formatDateTime(dateTimeString);
                    }
                }
            }
            
            // 如果没有EXIF信息，使用文件的修改时间
            return getFileLastModifiedDate(imageFile);
        } catch (Exception e) {
            // 如果提取EXIF信息失败，使用文件的修改时间
            return getFileLastModifiedDate(imageFile);
        }
    }
    
    /**
     * 格式化日期时间字符串，提取年月日
     */
    public static String formatDateTime(String dateTimeString) {
        try {
            // 常见的EXIF日期格式是：2023:05:15 14:30:25
            if (dateTimeString.contains(":")) {
                String[] parts = dateTimeString.split("\\s+");
                String datePart = parts[0];
                String[] dateComponents = datePart.split(":");
                if (dateComponents.length >= 3) {
                    return dateComponents[0] + "-" + dateComponents[1] + "-" + dateComponents[2];
                }
            }
            
            // 尝试其他格式
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = inputFormat.parse(dateTimeString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            return outputFormat.format(date);
        } catch (Exception e) {
            // 如果格式化失败，返回原始字符串的前10个字符
            return dateTimeString.length() > 10 ? dateTimeString.substring(0, 10) : dateTimeString;
        }
    }
    
    /**
     * 获取文件的最后修改时间
     */
    private static String getFileLastModifiedDate(File file) {
        Date lastModified = new Date(file.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(lastModified);
    }
}