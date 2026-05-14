package com.ops.zen.jdbc.sql;

import java.util.List;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class SQL {

    private List<Object> paramList;

    private List<String> paramSortedKey;
    private String prepareSql;

    private String exampleSql;

    private String originSql;

    private Map<String, Object> vars;

    private Map<String, Object> params;

    public List<Object> getParamList() {
        return paramList;
    }

    public void setParamList(List<Object> paramList) {
        this.paramList = paramList;
    }

    public String getPrepareSql() {
        return prepareSql;
    }

    public void setPrepareSql(String prepareSql) {
        this.prepareSql = prepareSql;
    }

    public String getOriginSql() {
        return originSql;
    }

    public void setOriginSql(String originSql) {
        this.originSql = originSql;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public void setVars(Map<String, Object> vars) {
        this.vars = vars;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public SQL() {
    }

    public SQL(List<Object> paramList, List<String> paramSortedKey, String prepareSql, String exampleSql, String originSql,
               Map<String, Object> vars, Map<String, Object> params) {
        this.paramList = paramList;
        this.paramSortedKey = paramSortedKey;
        this.prepareSql = prepareSql;
        this.exampleSql = exampleSql;
        this.originSql = originSql;
        this.vars = vars;
        this.params = params;
    }

    public String getExampleSql() {
        return exampleSql;
    }

    public void setExampleSql(String exampleSql) {
        this.exampleSql = exampleSql;
    }

    public List<String> getParamSortedKey() {
        return paramSortedKey;
    }

    public void setParamSortedKey(List<String> paramSortedKey) {
        this.paramSortedKey = paramSortedKey;
    }
}
