package com.ops.zen.sql.vo;


import com.ops.zen.phy.meta.EtlFieldMeta;

import java.util.List;

/**
 * @author Ezreal
 * @date 2023/8/15 14:05
 * @description
 **/
public class FFSqlExecResult {

    /**
     * 耗时
     */
    private long elapsedTime;//耗时毫秒数

    /**
     * SQL执行类型  SELECT | OTHER
     */
    private String execType;

    /**
     * 执行结果
     * execType = SELECT  ->   PageResult
     * execType = OTHER   ->   影响的行数
     */
    private String result;

    private List<EtlFieldMeta> fieldMetas;

    /**
     * 执行的SQL
     */
    private String sql;

    /**
     * 是不是单表  关系着数据是否可以被编辑
     */
    private boolean singletonTable;

    /**
     * 表明
     */
    private String tableName;

    /**
     * 主键名称
     */
    private String pkName;

    /**
     * 主键类型
     * {@link com.uis.nx.soar.etl.meta.EtlFieldTpEn}
     */
    private String pkColType;

    /**
     * 事务ID
     */
    private String connectionId;


    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getExecType() {
        return execType;
    }

    public void setExecType(String execType) {
        this.execType = execType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<EtlFieldMeta> getFieldMetas() {
        return fieldMetas;
    }

    public void setFieldMetas(List<EtlFieldMeta> fieldMetas) {
        this.fieldMetas = fieldMetas;
    }

    public boolean isSingletonTable() {
        return singletonTable;
    }

    public void setSingletonTable(boolean singletonTable) {
        this.singletonTable = singletonTable;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    public String getPkColType() {
        return pkColType;
    }

    public void setPkColType(String pkColType) {
        this.pkColType = pkColType;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
