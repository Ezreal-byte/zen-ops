package com.ops.zen.sqlwindow.vo;


/**
 * SQL窗口执行SQL请求参数
 */
public class ExecuteSqlRequest {
    private Long pkDs;
    private String sqlText;
    private Integer pageNum;
    private Integer pageSize;

    public Long getPkDs() {
        return pkDs;
    }

    public void setPkDs(Long pkDs) {
        this.pkDs = pkDs;
    }

    public String getSqlText() {
        return sqlText;
    }

    public void setSqlText(String sqlText) {
        this.sqlText = sqlText;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
