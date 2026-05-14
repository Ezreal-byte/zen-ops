package com.ops.zen.sql.parser;


import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.jdbc.sql.EasyVars;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * 单条SQL解析结果
 */
public class SinglePatchSql {

    /**
     * 原始sql，如declare开头，可能由多条语句构成，语句间通过“;”分割
     */
    private List<String> origins;

    /**
     * 完整的sql
     */
    private String unitSql;

    /**
     * sql文件中可能存在的以该关键字开头的代码块或表达式
     * {@link SQLKeywordEn}
     */
    private String keyword;

    /**
     * sql所在文件名
     */
    private String fileName;

    /**
     * 数据库名称：ipf、cdr、ihis..
     */
    private String dbName;

    /**
     * DDL, DML, COMMENT
     */
    private SQLTpEn sqlTp;

    /**
     * 注释-可能多个注释
     */
    private List<SinglePatchSql> comments = new ArrayList<>();

    /**
     * sql行号
     */
    private Integer lineNum;

    /**
     * 返回DML影响的行数或DDL时返回0
     * {@link PreparedStatement#executeUpdate()}
     * <p>
     * {@link com.uis.nx.soar.base.jdbc.Jdbc#execute(String, EasyParams, EasyVars)}
     */
    private Integer executeResult;

    /**
     * 执行异常信息
     */
    private String executeExMessage;

    public List<String> getOrigins() {
        return origins;
    }

    public void setOrigins(List<String> origins) {
        this.origins = origins;
    }

    public String getUnitSql() {
        return unitSql;
    }

    public void setUnitSql(String unitSql) {
        this.unitSql = unitSql;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public SQLTpEn getSqlTp() {
        return sqlTp;
    }

    public void setSqlTp(SQLTpEn sqlTp) {
        this.sqlTp = sqlTp;
    }

    public List<SinglePatchSql> getComments() {
        return comments;
    }

    public void setComments(List<SinglePatchSql> comments) {
        this.comments = comments;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    public Integer getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(Integer executeResult) {
        this.executeResult = executeResult;
    }

    public String getExecuteExMessage() {
        return executeExMessage;
    }

    public void setExecuteExMessage(String executeExMessage) {
        this.executeExMessage = executeExMessage;
    }
}
