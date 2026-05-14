package com.ops.zen.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/app")
public class AppConfigController {

    @Value("${zen.app.title:ZenOps}")
    private String title;

    @Value("${zen.app.subtitle:一站式运维平台}")
    private String subtitle;

    @Value("${zen.app.browser-title:ZenOps一站式运维平台}")
    private String browserTitle;

    /**
     * 获取应用配置信息
     */
    @GetMapping("/config")
    public Map<String, Object> getAppConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("title", title);
        config.put("subtitle", subtitle);
        config.put("browserTitle", browserTitle);
        return config;
    }
}
