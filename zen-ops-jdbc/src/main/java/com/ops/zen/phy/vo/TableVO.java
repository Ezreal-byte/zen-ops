package com.ops.zen.phy.vo;

/**
 * @author xiaoyingnan
 * @version 2024/4/11 14:53
 * <文件说明>
 **/
public class TableVO {

    private String tableName;

    private String comments;

    public TableVO() {
    }

    public TableVO(String tableName, String comments) {
        this.tableName = tableName;
        this.comments = comments;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
