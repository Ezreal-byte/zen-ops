package com.ops.zen.sql.vo;


import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.phy.meta.EtlFieldMeta;

import java.util.List;
import java.util.Map;

/**
 * @author Ezreal
 * @date 2023/8/16 14:43
 * @description
 **/
public class FFSqlUpdateVO {

    /**
     * 数据源
     */
    private String pkDs;

    /**
     * 单表表面
     */
    private String tableName;

    /**
     * 主键字段名
     */
    private String pkName;

    /**
     * 主键数据类型
     * {@link com.uis.nx.soar.etl.meta.EtlFieldTpEn}
     */
    private String pkColType;

    /**
     * 表的元数据  根据元数据组装SQL
     */
    private List<EtlFieldMeta> fieldMetas;// 元数据

    /**
     * 旧数据
     */
    private Map<String, EasyRecord> oldRows;

    /**
     * 新数据
     * key  行号  使用相同的行号获取旧数据
     */
    private Map<String, EasyRecord> newRows;

    public String getPkDs() {
        return pkDs;
    }

    public void setPkDs(String pkDs) {
        this.pkDs = pkDs;
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

    public List<EtlFieldMeta> getFieldMetas() {
        return fieldMetas;
    }

    public void setFieldMetas(List<EtlFieldMeta> fieldMetas) {
        this.fieldMetas = fieldMetas;
    }

    public Map<String, EasyRecord> getOldRows() {
        return oldRows;
    }

    public void setOldRows(Map<String, EasyRecord> oldRows) {
        this.oldRows = oldRows;
    }

    public Map<String, EasyRecord> getNewRows() {
        return newRows;
    }

    public void setNewRows(Map<String, EasyRecord> newRows) {
        this.newRows = newRows;
    }
}
