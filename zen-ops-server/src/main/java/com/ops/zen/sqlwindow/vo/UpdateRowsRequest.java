package com.ops.zen.sqlwindow.vo;

import java.util.List;
import java.util.Map;

/**
 * 表格编辑更新请求参数
 */
public class UpdateRowsRequest {
    private Long pkDs;
    private String dbSchema;
    private String tableName;
    private String pkColumn;
    private List<Map<String, Object>> changes;
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

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    public List<Map<String, Object>> getChanges() {
        return changes;
    }

    public void setChanges(List<Map<String, Object>> changes) {
        this.changes = changes;
    }

    public List<ColumnMetaVo> getColumnsMeta() {
        return columnsMeta;
    }

    public void setColumnsMeta(List<ColumnMetaVo> columnsMeta) {
        this.columnsMeta = columnsMeta;
    }
}
