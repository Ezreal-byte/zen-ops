package com.ops.zen;

import com.github.pagehelper.PageHelper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@SpringBootApplication
@MapperScan({"com.ops.zen.mapper", "com.ops.zen.mapper.custom"})
@EnableScheduling
public class ZenOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZenOpsApplication.class, args);
    }

    //配置mybatis的分页插件pageHelper - 根据数据源类型自动识别方言
    @Bean
    public PageHelper pageHelper(DataSourceProperties dataSourceProperties) {
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        
        // 根据 JDBC URL 自动识别数据库类型
        String jdbcUrl = dataSourceProperties.getUrl();
        String dialect = detectDialect(jdbcUrl);
        properties.setProperty("dialect", dialect);
        
        pageHelper.setProperties(properties);
        return pageHelper;
    }
    
    /**
     * 根据 JDBC URL 自动检测数据库方言
     */
    private String detectDialect(String jdbcUrl) {
        if (jdbcUrl == null) {
            return "mysql"; // 默认使用 mysql
        }
        
        String url = jdbcUrl.toLowerCase();
        if (url.contains(":sqlite:")) {
            return "sqlite";
        } else if (url.contains(":mysql:")) {
            return "mysql";
        } else if (url.contains(":oracle:")) {
            return "oracle";
        } else if (url.contains(":postgresql:") || url.contains(":postgres:")) {
            return "postgresql";
        } else if (url.contains(":sqlserver:") || url.contains(":mssql:")) {
            return "sqlserver";
        } else {
            // 默认使用 mysql
            return "mysql";
        }
    }

}
