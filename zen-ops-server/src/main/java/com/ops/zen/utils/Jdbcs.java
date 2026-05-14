package com.ops.zen.utils;

import com.ops.zen.ds.DSConfig;
import com.ops.zen.ds.DataSourceFactory;
import com.ops.zen.ds.DataSourceFromEn;
import com.ops.zen.en.DsConnTypeEn;
import com.ops.zen.en.DbTypeEn;
import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.jdbc.EasyJdbc;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.mapper.ZenDbDsMapper;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xyn
 * @date 2025/4/23 14:32
 * @description JDBC工具类
 **/
public class Jdbcs {


    public static final long DEFAULT_INITIAL_SIZE = 1L;
    public static final long DEFAULT_MAX_ACTIVE = 10L;
    public static final long DEFAULT_MAX_WAIT = 1000L;
    public static final long DEFAULT_PHY_CONN_TIMEOUT = 1000L;
    public static final long DEFAULT_PHY_READ_TIMEOUT = 5000L;

    private static ConcurrentHashMap<String, Jdbc> PROPERTIES_JDBCS = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Jdbc> DATASOURCE_JDBCS = new ConcurrentHashMap<>();
    private static final String DEFAULT_PROPERTIES_JDBC_KEY = "DEFAULT_PROPERTIES_JDBC_KEY";

    /**
     * 创建配置文件对应的jdbc
     *
     * @return
     */
    @Deprecated
//    public synchronized static Jdbc propertiesJdbc() {
//        Jdbc jdbc = PROPERTIES_JDBCS.get(DEFAULT_PROPERTIES_JDBC_KEY);
//        if (jdbc == null) {
//            Environment environment = ApplicationContextUtils.get(Environment.class);
//            String driverClassName = environment.getProperty("spring.datasource.driver-class-name");
//            String url = environment.getProperty("spring.datasource.url");
//            String username = environment.getProperty("spring.datasource.username");
//            String password = environment.getProperty("spring.datasource.password");
//            DSConfig dsConfig = new DSConfig(DEFAULT_PROPERTIES_JDBC_KEY,
//                    driverClassName, url, username, password,
//                    DEFAULT_INITIAL_SIZE, DEFAULT_MAX_ACTIVE, DEFAULT_MAX_WAIT, DEFAULT_PHY_CONN_TIMEOUT, DEFAULT_PHY_READ_TIMEOUT, DEFAULT_PROPERTIES_JDBC_KEY, DataSourceFromEn.PROPERTIES);
//            jdbc = new EasyJdbc(DataSourceFactory.buildDataSource(dsConfig));
//            PROPERTIES_JDBCS.put(DEFAULT_PROPERTIES_JDBC_KEY, jdbc);
//        }
//        return jdbc;
//    }


    public synchronized static Jdbc getJdbc(String key) {
        Jdbc jdbc = DATASOURCE_JDBCS.get(key);
        if (jdbc == null) {
//            ZenDbDs ds = propertiesJdbc().get(ZenDbDs.class, Long.parseLong(key));
            ZenDbDsMapper mapper = ApplicationContextUtils.get(ZenDbDsMapper.class);
//        ZenDbDs ds = Jdbcs.propertiesJdbc().get(ZenDbDs.class, Long.parseLong(pkDs));
            ZenDbDs ds = mapper.selectById(Long.parseLong(key));
            Assert.notNull(ds, "datasource is null");
            DSConfig dsConfig = new DSConfig(key,
                    ds.getDriver(), buildJdbcUrl(ds), ds.getUserName(), ds.getUserPwd(),
                    ds.getConnMin().longValue(),
                    ds.getConnMax().longValue(),
                    DEFAULT_MAX_WAIT,
                    DEFAULT_PHY_CONN_TIMEOUT,
                    DEFAULT_PHY_READ_TIMEOUT,
                    ds.getName(),
                    DataSourceFromEn.PROPERTIES);
            jdbc = new EasyJdbc(DataSourceFactory.buildDataSource(dsConfig));
            DATASOURCE_JDBCS.put(DEFAULT_PROPERTIES_JDBC_KEY, jdbc);
        }
        return jdbc;
    }

    /**
     * 获取手动提交的 Connection
     * @param pkDs
     * @return
     * @throws SQLException
     */
    public static Connection getConnectionManualCommit(Jdbc jdbc) throws SQLException {
//        Jdbc jdbc = getJdbc(pkDs);
        Connection connection = jdbc.getDataSource().getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    /**
     * 移除数据源
     * @param key
     * @return
     */
    public synchronized static boolean removeJdbc(String key) {
        if (DATASOURCE_JDBCS.containsKey(key)) {
            Jdbc jdbc = DATASOURCE_JDBCS.get(key);
            //todo  close ?
            DATASOURCE_JDBCS.remove(key);
            return true;
        }
        return false;
    }

    public static String buildJdbcUrl(ZenDbDs dsDataSource) {
        String connType = dsDataSource.getConnType();
        // 兼容枚举值和数字值
        boolean isUrl = Objects.equals(DsConnTypeEn.URL, connType) || Objects.equals("2", connType);
        boolean isHost = Objects.equals(DsConnTypeEn.HOST, connType) || Objects.equals("1", connType);
        if (isUrl) {
            return dsDataSource.getUrl();
        } else if (isHost) {
            String dbType = dsDataSource.getDbType();
            if (Objects.equals(DbTypeEn.MYSQL, dbType)) {
                return String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai",
                        dsDataSource.getHost(), dsDataSource.getPort(), dsDataSource.getDbSchema());
            } else if (Objects.equals(DbTypeEn.ORACLE, dbType)) {
                return String.format("jdbc:oracle:thin:@%s:%s:%s",
                        dsDataSource.getHost(), dsDataSource.getPort(), dsDataSource.getDbSchema());
            } else if (Objects.equals(DbTypeEn.POSTGRE_SQL, dbType)) {
                return String.format("jdbc:postgresql://%s:%s/%s",
                        dsDataSource.getHost(), dsDataSource.getPort(), dsDataSource.getDbSchema());
            } else if (Objects.equals(DbTypeEn.CLICK_HOUSE, dbType)) {
                return String.format("jdbc:clickhouse://%s:%s/%s",
                        dsDataSource.getHost(), dsDataSource.getPort(), dsDataSource.getDbSchema());
            } else {
                throw new RuntimeException(String.format("dbType: %s is not supported", dbType));
            }
        } else {
            throw new RuntimeException(String.format("connType: %s is not supported", connType));
        }
    }

}
