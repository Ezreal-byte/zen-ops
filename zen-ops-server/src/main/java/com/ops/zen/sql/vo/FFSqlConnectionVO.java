package com.ops.zen.sql.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Connection;

/**
 * @author xiaoyingnan
 * @version 2023/9/12 14:21
 * <文件说明>
 **/
public class FFSqlConnectionVO {

    /**
     * connection的唯一ID
     */
    private String connectionId;

    /**
     * 操作ID  可能多个操作指向同一个connection
     */
    private String opId;

    /**
     * 数据源主键
     */
    private String pkDs;

    /**
     * 数据源名称
     */
    private String dsName;

    /**
     * 操作类型
     */
    private String opTp;

    /**
     * 受影响的行数
     */
    private int updateRows;

    /**
     * 操作时间
     */
    private String dt;

    @JsonIgnore
    private Connection connection;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getOpId() {
        return opId;
    }

    public void setOpId(String opId) {
        this.opId = opId;
    }

    public String getPkDs() {
        return pkDs;
    }

    public void setPkDs(String pkDs) {
        this.pkDs = pkDs;
    }

    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public String getOpTp() {
        return opTp;
    }

    public void setOpTp(String opTp) {
        this.opTp = opTp;
    }

    public int getUpdateRows() {
        return updateRows;
    }

    public void setUpdateRows(int updateRows) {
        this.updateRows = updateRows;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
