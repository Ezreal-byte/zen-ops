package com.ops.zen.ds;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.IOUtils;
import com.ops.zen.utils.JsonUtils;
import com.ops.zen.utils.StringUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * * <table class="bbcode " style="margin:10px 0px; padding:0px; border:1px solid rgb(204,204,204); outline:0px; font-size:12px; vertical-align:baseline; background-color:rgb(185,185,185); border-spacing:1px; color:rgb(0,0,0); font-family:Helvetica,Tahoma,Arial,sans-serif; line-height:18px"><tbody style="margin:0px; padding:0px; border:0px; outline:0px; vertical-align:baseline; background-color:transparent"><tr style="margin:0px; padding:0px; border:0px; outline:0px; vertical-align:baseline; background-color:transparent"><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> JDBC Driver</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> connectTimeout配置项</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> socketTimeout配置项</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> url格式</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> 示例</td></tr><tr style="margin:0px; padding:0px; border:0px; outline:0px; vertical-align:baseline; background-color:transparent"><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> MySQL Driver</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> connectTimeout（默认值：0，单位：ms）</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> socketTimeout（默认值：0，单位：ms）</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> jdbc:mysql://[host:port],[host:port]…/[database][?propertyName1][=propertyValue1][&amp;propertyName2][=propertyValue2]…</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> jdbc:mysql://xxx.xx.xxx.xxx:3306/database?connectTimeout=60000&amp;socketTimeout=60000</td></tr><tr style="margin:0px; padding:0px; border:0px; outline:0px; vertical-align:baseline; background-color:transparent"><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> MS-SQL DriverjTDS Driver</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> loginTimeout（默认值：0，单位：s）</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> socketTimeout（默认值：0，单位：s）</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> jdbc:jtds:&lt;server_type&gt;://&lt;server&gt;[:&lt;port&gt;][/&lt;database&gt;][;&lt;property&gt;=&lt;value&gt;[;...]]</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> jdbc:jtds:sqlserver://server:port/database;loginTimeout=60;socketTimeout=60</td></tr><tr style="margin:0px; padding:0px; border:0px; outline:0px; vertical-align:baseline; background-color:transparent"><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> Oracle Thin Driver</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> oracle.net.CONNECT_TIMEOUT （默认值：0，单位：ms）</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> oracle.jdbc.ReadTimeout（默认值：0，单位：ms）</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> 不支持通过url配置，只能通过OracleDatasource.setConnectionProperties() API设置，使用DBCP时可以调用BasicDatasource.setConnectionProperties()或BasicDatasource.addConnectionProperties()进行设置</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> &nbsp;</td></tr><tr style="margin:0px; padding:0px; border:0px; outline:0px; vertical-align:baseline; background-color:transparent"><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> CUBRID Thin Driver</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> 无独立配置项（默认值：5,000，单位：ms）</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> 无独立配置项（默认值：5,000，单位：ms）</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> &nbsp;</td><td style="margin:0px; padding:3px; border:1px solid rgb(204,204,204); outline:0px; font-size:1em; vertical-align:baseline; background-color:rgb(251,251,251)"> &nbsp;</td></tr></tbody></table>
 *
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public class DataSourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);


    /**
     * 数据源MAP
     */
    private static Map<DSConfig, DataSource> DS_MAP = new ConcurrentHashMap<>();




    /**
     * 创建数据源并返回，异常时返回空
     *
     * @param dataSourceName
     * @param properties
     * @return
     */
    public static DataSource createDataSourceByConfig(String dataSourceName, Map properties) {

        // 校验properties文件的数据源配置
        try {
            Assert.isTrue(properties != null && properties.size() > 0, "数据源%s未找到配置项", dataSourceName);
            Assert.isTrue(properties.containsKey("url"), "数据源%s配置项url不能为null", dataSourceName);
            Assert.isTrue(properties.containsKey("username"), "数据源%s配置项username不能为null", dataSourceName);
            Assert.isTrue(properties.containsKey("password"), "数据源%s配置项password不能为null", dataSourceName);
        } catch (Exception e) {
            logger.warn("数据源配置有问题：{}，返回空数据源", e.getMessage(), e);
            return null;
        }
        return doCreate(dataSourceName, properties);
    }

    public static DataSource doCreate(String dataSourceName, Map properties) {
        DataSource dataSource = null;
        try {
            DSConfig dsConfig = JsonUtils.toObject(DSConfig.class, JsonUtils.toJSONString(properties));
            dsConfig.setDataSourceName(dataSourceName);
            dsConfig.setDataSourceFromTp(DataSourceFromEn.PROPERTIES);
            dataSource = buildDataSource(dsConfig);
            return dataSource;
        } catch (Exception e) {
            logger.error("数据源{}创建失败，请查看数据源连接是否配置正确。如果该数据源未使用，请忽略该错误信息", dataSourceName, e);

            if (dataSource instanceof DruidDataSource || dataSource instanceof HikariDataSource) {
                IOUtils.close((Closeable) dataSource);
            }
            return null;
        }
    }

    public static DataSource buildDataSource(DSConfig dsConfig) {
        return hikari(dsConfig);
//        return druid(dsConfig);
    }

    public static DataSource druid(DSConfig cfg) {

        DruidDataSource dataSource = null;
        Properties properties = new Properties();
        try {
            //改为手动init
            properties.put("init", "false");

            // 连接池策略配置，稳定性配置等
            String driverClassName = cfg.getDriverClassName();
            if (driverClassName != null && driverClassName.toLowerCase().indexOf("oracle") >= 0) {
                properties.put("validationQuery", "select 1 from dual");
            } else {
                properties.put("validationQuery", "select 1");
            }
            properties.put("driverClassName", driverClassName);
            properties.put("testOnBorrow", "true"); // testOnBorrow为true时testWhileIdle配置无效
            properties.put("testWhileIdle", "true");
            properties.put("timeBetweenEvictionRunsMillis", "60000");

            properties.put("url", cfg.getUrl());

            // 空字符串和null对于hikari表现不同，空字符串应该设置为null
            if (StringUtils.isNotBlank(cfg.getUsername())) {
                properties.put("username", cfg.getUsername());
            }
            if (StringUtils.isNotBlank(cfg.getPassword())) {
                properties.put("password", cfg.getPassword());
            }

            // 以下校验边界和非空等
            if (cfg.getInitialSize() != null && cfg.getMaxActive() != null && cfg.getInitialSize() > cfg.getMaxActive()) {
                throw new RuntimeException(String.format("initialSize：%d不能大于maxActive：%d", cfg.getInitialSize(), cfg.getMaxActive()));
            }
            if (cfg.getInitialSize() != null && cfg.getInitialSize() > 0) {
                properties.put("initialSize", cfg.getInitialSize().toString());
            } else {
                properties.put("initialSize", DSConfig.SIZE_CONN_MIN + "");
            }
            if (cfg.getMaxActive() != null && cfg.getMaxActive() > 0) {
                properties.put("maxActive", cfg.getMaxActive().toString());
            } else {
                properties.put("maxActive", DSConfig.SIZE_CONN_MAX + "");
            }

            if (cfg.getMaxWait() != null && cfg.getMaxWait() > 0) {
                properties.put("maxWait", cfg.getMaxWait().toString());
            } else {
                properties.put("maxWait", DSConfig.MAX_WAIT_IN_MS + "");
            }


            if (cfg.getPhyConnTimeout() == null && cfg.getPhyConnTimeout() <= 0) {
                cfg.setPhyConnTimeout(DSConfig.PHY_CONNECT_TIMEOUT_MS);
            }
            if (cfg.getPhyReadTimeout() == null && cfg.getPhyReadTimeout() <= 0) {
                cfg.setPhyReadTimeout(DSConfig.PHY_READ_TIME_OUT_MS);
            }


            dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);

            // 参数BreakAfterAcquireFailure设置为true-获取连接失败以后就退出，不会连续的获取数据库连接 - https://www.cnblogs.com/xchendevelop/articles/9291044.html
            dataSource.setBreakAfterAcquireFailure(false);

            // Oralce的连接参数，连接超时时间和读写超时时间-单位毫秒（默认好像是20秒，有点长）
            dataSource.setConnectionProperties(String.format("oracle.net.CONNECT_TIMEOUT=%d;oracle.jdbc.ReadTimeout=%d", cfg.getPhyConnTimeout(), cfg.getPhyReadTimeout()));
            dataSource.init(); //手动调用init

            // 存入DS_MAP
            DS_MAP.put(cfg, dataSource);

            return dataSource;
        } catch (Exception e) {
            logger.error("数据源{}创建失败，请查看数据源连接是否配置正确。如果该数据源未使用，请忽略该错误信息", cfg.getDataSourceName(), e);
            IOUtils.close(dataSource);
            return null;
        }
    }

    public static DataSource hikari(DSConfig dsConfig) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dsConfig.getUrl());

        // 空字符串和null对于hikari表现不同，空字符串应该设置为null
        if (StringUtils.isNotBlank(dsConfig.getUsername())) {
            hikariConfig.setUsername(dsConfig.getUsername());
        }
        if (StringUtils.isNotBlank(dsConfig.getPassword())) {
            hikariConfig.setPassword(dsConfig.getPassword());
        }

        // 以下校验边界和非空等
        if (dsConfig.getInitialSize() != null && dsConfig.getMaxActive() != null && dsConfig.getInitialSize() > dsConfig.getMaxActive()) {
            throw new RuntimeException(String.format("initialSize：%d不能大于maxActive：%d", dsConfig.getInitialSize(), dsConfig.getMaxActive()));
        }
        if (dsConfig.getInitialSize() != null && dsConfig.getInitialSize() > 0) {
            hikariConfig.setMinimumIdle(dsConfig.getInitialSize().intValue());
        } else {
            hikariConfig.setMinimumIdle((int) DSConfig.SIZE_CONN_MIN);
        }
        if (dsConfig.getMaxActive() != null && dsConfig.getMaxActive() > 0) {
            hikariConfig.setMaximumPoolSize(dsConfig.getMaxActive().intValue());
        } else {
            hikariConfig.setMaximumPoolSize((int) DSConfig.SIZE_CONN_MAX);
        }
        if (dsConfig.getMaxWait() != null && dsConfig.getMaxWait() > 0) {
            hikariConfig.setConnectionTimeout(dsConfig.getMaxWait());
        } else {
            hikariConfig.setConnectionTimeout(DSConfig.MAX_WAIT_IN_MS);
        }
        if (dsConfig.getPhyConnTimeout() == null && dsConfig.getPhyConnTimeout() <= 0) {
            dsConfig.setPhyConnTimeout(DSConfig.PHY_CONNECT_TIMEOUT_MS);
        }
        if (dsConfig.getPhyReadTimeout() == null && dsConfig.getPhyReadTimeout() <= 0) {
            dsConfig.setPhyReadTimeout(DSConfig.PHY_READ_TIME_OUT_MS);
        }

//        hikariConfig.setConnectionTestQuery("select 1 from dual");
        hikariConfig.setPoolName(dsConfig.getDataSourceName());

        // oracle的连接和读写超时配置 TODO 只有ORACLE才需要配置？
        hikariConfig.addDataSourceProperty("oracle.net.CONNECT_TIMEOUT", dsConfig.getPhyConnTimeout());
        hikariConfig.addDataSourceProperty("oracle.jdbc.ReadTimeout", dsConfig.getPhyReadTimeout());
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        // https://blog.csdn.net/abu935009066/article/details/127155653
        MeterRegistry loggingMeterRegistry = new StepMeterRegistry(new StepRegistryConfig() {
            @Override
            public String prefix() {
                return null;
            }

            @Override
            public String get(String key) {
                return null;
            }
        }, Clock.SYSTEM) {
            @Override
            protected void publish() {
                System.out.println("publish()");
            }

            @Override
            protected TimeUnit getBaseTimeUnit() {
                return TimeUnit.SECONDS;
            }
        };

        dataSource.setMetricRegistry(loggingMeterRegistry);
        DS_MAP.put(dsConfig, dataSource);
        return dataSource;
    }


    public static void close(DataSource dataSource) {
        // 清理数据源
        Iterator<Map.Entry<DSConfig, DataSource>> iterator = DS_MAP.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == dataSource) {
                iterator.remove();
            }
        }
        // 关闭
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        } else if (dataSource instanceof DruidDataSource) {
            ((DruidDataSource) dataSource).close();
        }
    }
}
