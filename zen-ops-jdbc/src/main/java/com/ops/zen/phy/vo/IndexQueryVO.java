package com.ops.zen.phy.vo;

/**
 * @author Ezreal
 * @date 2021/11/25 15:53
 * @description
 **/
public class IndexQueryVO {

    private String indexName;//索引名称

    private String columnName;//字段名称

    private String uniqueness; // UNIQUE 表示唯一索引

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getUniqueness() {
        return uniqueness;
    }

    public void setUniqueness(String uniqueness) {
        this.uniqueness = uniqueness;
    }
}
