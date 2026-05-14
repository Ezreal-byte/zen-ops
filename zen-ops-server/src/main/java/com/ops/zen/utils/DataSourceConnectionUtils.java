package com.ops.zen.utils;

import com.ops.zen.utils.map.KvMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xyn
 * @date 2025/4/9 21:31
 * @description
 **/
@Slf4j
public class DataSourceConnectionUtils {

    public static KvMap testConnection(String url, String userName, String password) {
        KvMap kvMap = new KvMap();
        kvMap.put("success", true);

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(userName);
            config.setPassword(password);
            config.setMaximumPoolSize(1);
            config.setConnectionTimeout(2000);
            config.setInitializationFailTimeout(1000);
            config.setValidationTimeout(1000);
            HikariDataSource dataSource = new HikariDataSource(config);
            IOUtils.close(dataSource);
        } catch (Exception e) {
            log.warn("test connection error", e);
            kvMap.put("success", false);
            kvMap.put("msg", e.getMessage());
        }
        return kvMap;
    }


}
