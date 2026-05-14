package com.ops.zen.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签通用接口
 * @author xyn
 * @date 2026/05/06
 * @description 提供预设标签列表，供各模块使用
 */
@RestController
@RequestMapping("/tags")
@Slf4j
public class TagsController {

    @Value("${zen.tags:}")
    private String customTags;

    /**
     * 获取预设标签列表
     * 返回格式: [{label: '生产', type: 'primary'}, {label: '测试', type: 'warning'}]
     */
    @GetMapping("list")
    public List<Map<String, Object>> getPredefinedTags() {
        List<Map<String, Object>> tags = new ArrayList<>();

        // 如果配置文件 zen.tags 配置了自定义标签，使用配置文件
        if (StringUtils.hasText(customTags)) {
            try {
                String[] tagArray = customTags.split(",");
                for (String tagStr : tagArray) {
                    String trimmed = tagStr.trim();
                    if (StringUtils.hasText(trimmed)) {
                        String[] parts = trimmed.split(":");
                        if (parts.length == 2) {
                            tags.add(createTag(parts[0].trim(), parts[1].trim()));
                        } else if (parts.length == 1) {
                            // 如果没有指定类型，默认为 info
                            tags.add(createTag(parts[0].trim(), "info"));
                        }
                    }
                }
                log.info("从配置文件加载标签: {}", customTags);
            } catch (Exception e) {
                log.error("解析自定义标签配置失败: {}", customTags, e);
                // 解析失败时使用默认标签
                tags = getDefaultTags();
            }
        } else {
            // 如果没配置，使用默认标签
            tags = getDefaultTags();
            log.info("使用默认标签配置");
        }

        return tags;
    }

    /**
     * 获取默认标签
     */
    private List<Map<String, Object>> getDefaultTags() {
        List<Map<String, Object>> tags = new ArrayList<>();
        tags.add(createTag("生产", "primary"));
        tags.add(createTag("测试", "warning"));
        return tags;
    }

    /**
     * 创建标签对象
     * @param label 标签显示文字
     * @param type 标签类型（用于颜色）
     */
    private Map<String, Object> createTag(String label, String type) {
        Map<String, Object> tag = new LinkedHashMap<>();
        tag.put("label", label);
        tag.put("type", type);
        return tag;
    }
}
