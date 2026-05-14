package com.ops.zen.ds;

import java.util.Objects;

/**
 * 数据源配置，可以用于minioclient配置
 * <p>
 * datasource.config.driverClassName=org.postgresql.Driver
 * datasource.config.url=jdbc:postgresql://172.16.2.30:5432/ops?currentSchema=soar
 * datasource.config.username=ops
 * datasource.config.password=1
 * datasource.config.initialSize=5
 * datasource.config.maxActive=30
 * datasource.config.maxWait=3000
 *
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public class DSConfig {

    // Oralce 读写超时时间
    public static final long PHY_READ_TIME_OUT_MS = 4 * 60 * 1000;

    // Oracle 连接超时时间
    public static final long PHY_CONNECT_TIMEOUT_MS = 10000;

    // 连接池获取连接最大等待时间
    public static final long MAX_WAIT_IN_MS = 10000;

    /**
     * 连接池最小连接数
     */
    public static final long SIZE_CONN_MIN = 1;

    /**
     * 连接池最大连接数
     */
    public static final long SIZE_CONN_MAX = 5;


    // 数据源名称，唯一id，参与equals和hash的算法
    private String dataSourceName;

    /**
     * 数据源名称（一般指中文名，或有意义的名称），不参与hash和equals
     */
    private String name;

    /**
     * {@link DataSourceFromEn}
     * 数据来源类型
     */
    private String dataSourceFromTp;

    private String driverClassName;

    private String url;

    private String username;

    private String password;

    /**
     * 初始连接数（空闲连接数）
     */
    private Long initialSize = SIZE_CONN_MIN;

    /**
     * 最大连接数
     */
    private Long maxActive = SIZE_CONN_MAX;

    /**
     * 最大等待时间，单位毫秒，从连接池获取连接最大等待时间 ms
     */
    private Long maxWait = MAX_WAIT_IN_MS;

    /**
     * 数据库底层连接超时时间 ms
     */
    private Long phyConnTimeout = PHY_CONNECT_TIMEOUT_MS;

    /**
     * 数据库底层读写超时时间 ms
     */
    private Long phyReadTimeout = PHY_READ_TIME_OUT_MS;


    public DSConfig() {
    }

    public DSConfig(String name, String driverClassName, String url, String username, String password,
                    Long initialSize, Long maxActive,
                    Long maxWait, Long phyConnTimeout, Long phyReadTimeout,
                    String dataSourceName, String dataSourceFromTp) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.dataSourceName = dataSourceName;
        this.dataSourceFromTp = dataSourceFromTp;
        this.name = name;

        this.initialSize = (initialSize == null || initialSize <= 0) ? SIZE_CONN_MIN : initialSize;
        this.maxActive = (maxActive == null || maxActive <= 0) ? SIZE_CONN_MAX : maxActive;

        this.maxWait = (maxWait == null || maxWait <= 0) ? MAX_WAIT_IN_MS : maxWait;
        this.phyConnTimeout = (phyConnTimeout == null || phyConnTimeout <= 0) ? PHY_CONNECT_TIMEOUT_MS : phyConnTimeout;
        this.phyReadTimeout = (phyReadTimeout == null || phyReadTimeout <= 0) ? PHY_READ_TIME_OUT_MS : phyReadTimeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Long initialSize) {
        this.initialSize = initialSize;
    }

    public Long getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Long maxActive) {
        this.maxActive = maxActive;
    }

    public Long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Long maxWait) {
        this.maxWait = maxWait;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getDataSourceFromTp() {
        return dataSourceFromTp;
    }

    public void setDataSourceFromTp(String dataSourceFromTp) {
        this.dataSourceFromTp = dataSourceFromTp;
    }

    public Long getPhyConnTimeout() {
        return phyConnTimeout;
    }

    public void setPhyConnTimeout(Long phyConnTimeout) {
        this.phyConnTimeout = phyConnTimeout;
    }

    public Long getPhyReadTimeout() {
        return phyReadTimeout;
    }

    public void setPhyReadTimeout(Long phyReadTimeout) {
        this.phyReadTimeout = phyReadTimeout;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DSConfig dsConfig = (DSConfig) o;
        return Objects.equals(dataSourceName, dsConfig.dataSourceName) &&
                Objects.equals(dataSourceFromTp, dsConfig.dataSourceFromTp) &&
                Objects.equals(driverClassName, dsConfig.driverClassName) &&
                Objects.equals(url, dsConfig.url) &&
                Objects.equals(username, dsConfig.username) &&
                Objects.equals(password, dsConfig.password) &&
                Objects.equals(initialSize, dsConfig.initialSize) &&
                Objects.equals(maxActive, dsConfig.maxActive) &&
                Objects.equals(maxWait, dsConfig.maxWait) &&
                Objects.equals(phyConnTimeout, dsConfig.phyConnTimeout) &&
                Objects.equals(phyReadTimeout, dsConfig.phyReadTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSourceName, dataSourceFromTp, driverClassName, url, username, password, initialSize, maxActive, maxWait, phyConnTimeout, phyReadTimeout);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
