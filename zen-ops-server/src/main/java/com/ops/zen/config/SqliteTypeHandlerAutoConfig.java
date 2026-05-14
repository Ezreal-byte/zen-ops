package com.ops.zen.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * SQLite 类型处理器自动注册配置
 * 当使用 SQLite 驱动时，自动注册兼容的 TypeHandler
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.sqlite.JDBC")
public class SqliteTypeHandlerAutoConfig {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void registerTypeHandlers() {
        TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        
        // 注册 LocalDateTime 的兼容处理器
        typeHandlerRegistry.register(LocalDateTime.class, SqliteLocalDateTimeTypeHandler.class);
        
        // 注册 BLOB 的兼容处理器（用于头像等二进制数据）
        // 同时注册到 byte[].class 和 Types.BLOB
        SqliteBlobTypeHandler blobHandler = new SqliteBlobTypeHandler();
        typeHandlerRegistry.register(byte[].class, blobHandler);
        typeHandlerRegistry.register(byte[].class, JdbcType.BLOB, blobHandler);
    }
}
