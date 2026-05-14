package com.ops.zen.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * @author xyn
 * @date 2025/4/23 14:42
 * @description
 **/
@Configuration
public class ApplicationContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.applicationContext = applicationContext;
    }

    public static <T> T get(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> T get(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public static Object get(String name) {
        return applicationContext.getBean(name);
    }
}
