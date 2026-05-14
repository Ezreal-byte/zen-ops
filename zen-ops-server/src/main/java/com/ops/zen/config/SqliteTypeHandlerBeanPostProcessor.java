package com.ops.zen.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * SQLite TypeHandler Bean 后置处理器
 * 确保在 Mapper 初始化之前注册自定义 TypeHandler
 */
@Component
@ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.sqlite.JDBC")
public class SqliteTypeHandlerBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SqlSessionFactory) {
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
            TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
            
            // 注册 LocalDateTime 的兼容处理器
            typeHandlerRegistry.register(LocalDateTime.class, SqliteLocalDateTimeTypeHandler.class);
            
            // 注册 BLOB 的兼容处理器
            SqliteBlobTypeHandler blobHandler = new SqliteBlobTypeHandler();
            typeHandlerRegistry.register(byte[].class, blobHandler);
            typeHandlerRegistry.register(byte[].class, JdbcType.BLOB, blobHandler);
        }
        return bean;
    }
}
