package com.ops.zen.jdbc.sql;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class SQLSource {

    private String sqlFileName;

    private String sqlId;

    private String comment;

    private String sqlContent;

    private int lineNumber;

    public SQLSource(String sqlFileName, String sqlId, String comment, String sqlContent, int lineNumber) {
        this.sqlFileName = sqlFileName;
        this.sqlId = sqlId;
        this.comment = comment;
        this.sqlContent = sqlContent;
        this.lineNumber = lineNumber;
    }

    public String getSqlFileName() {
        return sqlFileName;
    }

    public void setSqlFileName(String sqlFileName) {
        this.sqlFileName = sqlFileName;
    }

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSqlContent() {
        return sqlContent;
    }

    public void setSqlContent(String sqlContent) {
        this.sqlContent = sqlContent;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
