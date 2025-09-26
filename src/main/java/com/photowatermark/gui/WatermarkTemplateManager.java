package com.photowatermark.gui;

import java.io.*;
import java.util.*;

/**
 * 水印模板管理器 - 负责模板的存储、加载和管理
 */
public class WatermarkTemplateManager {
    private static final String TEMPLATES_FILE = System.getProperty("user.home") + "/.photowatermark/templates.ser";
    private List<WatermarkTemplate> templates;
    
    public WatermarkTemplateManager() {
        this.templates = new ArrayList<>();
        loadTemplates();
    }
    
    /**
     * 从文件加载模板
     */
    @SuppressWarnings("unchecked")
    private void loadTemplates() {
        File file = new File(TEMPLATES_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                this.templates = (List<WatermarkTemplate>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("加载模板失败: " + e.getMessage());
                this.templates = new ArrayList<>();
            }
        }
    }
    
    /**
     * 保存模板到文件
     */
    private void saveTemplates() {
        // 确保目录存在
        File file = new File(TEMPLATES_FILE);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this.templates);
        } catch (IOException e) {
            System.err.println("保存模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加新模板
     */
    public void addTemplate(WatermarkTemplate template) {
        // 检查是否已存在同名模板
        for (int i = 0; i < templates.size(); i++) {
            if (templates.get(i).getTemplateName().equals(template.getTemplateName())) {
                // 如果存在，替换它
                templates.set(i, template);
                saveTemplates();
                return;
            }
        }
        
        // 不存在则添加
        templates.add(template);
        saveTemplates();
    }
    
    /**
     * 删除模板
     */
    public void removeTemplate(String templateName) {
        templates.removeIf(template -> template.getTemplateName().equals(templateName));
        saveTemplates();
    }
    
    /**
     * 查找模板
     */
    public WatermarkTemplate findTemplate(String templateName) {
        for (WatermarkTemplate template : templates) {
            if (template.getTemplateName().equals(templateName)) {
                return template;
            }
        }
        return null;
    }
    
    /**
     * 获取所有模板
     */
    public List<WatermarkTemplate> getAllTemplates() {
        return new ArrayList<>(templates); // 返回副本以保护内部数据
    }
    
    /**
     * 获取模板名称列表
     */
    public List<String> getTemplateNames() {
        List<String> names = new ArrayList<>();
        for (WatermarkTemplate template : templates) {
            names.add(template.getTemplateName());
        }
        return names;
    }
    
    /**
     * 检查模板名称是否已存在
     */
    public boolean isTemplateNameExists(String templateName) {
        for (WatermarkTemplate template : templates) {
            if (template.getTemplateName().equals(templateName)) {
                return true;
            }
        }
        return false;
    }
}