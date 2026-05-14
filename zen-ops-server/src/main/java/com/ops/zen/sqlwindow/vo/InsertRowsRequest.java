package com.ops.zen.sqlwindow.vo;

import java.util.List;
import java.util.Map;

/**
 * 表格编辑插入请求参数
 */
public class InsertRowsRequest {
    private Long pkDs;
    private String dbSchema;
    private String tableName;
    private List<Map<String, String>> rows;
    private List<ColumnMetaVo> columnsMeta;

    public Long getPkDs() {
        return pkDs;
    }

    public void setPkDs(Long pkDs) {
        this.pkDs = pkDs;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, String>> rows) {
        this.rows = rows;
    }

    public List<ColumnMetaVo> getColumnsMeta() {
        return columnsMeta;
    }

    public void setColumnsMeta(List<ColumnMetaVo> columnsMeta) {
        this.columnsMeta = columnsMeta;
    }
}
