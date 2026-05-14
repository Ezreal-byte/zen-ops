package com.ops.zen.select;

/**
 * @author xyn
 * @date 2025/4/9 20:58
 * @description
 **/
public class SQLCustomSelectParams implements ISelectParams {
    @Override
    public String type() {
        return null;
    }

    /*   private String dataSource;

    *//**
     * 自定义sql要求必须返回label和value两个字段，里面可以使用的nutzsql参数为【@keyword】
     *//*
    private String sql;

    private FuzzyTypeEn fuzzyTypeEn = FuzzyTypeEn.RIGHT;

    *//**
     * keyword是否使用小写，取决于oracle查询时字段使用lower or upper function
     *//*
    private boolean keywordLowerCase = false;


    *//**
     * 用于sqlParams的延迟获取，可以在表达式中添加缓存的参数，清理缓存以后会生效
     *//*
    private Supplier<SqlParams> sqlParamsSupplier;

    private SqlParams sqlParams;

    private Function<List<SelectModel>, List<SelectModel>> postProcessFunc;

    public SQLCustomSelectParams() {
    }

    public SQLCustomSelectParams(String dataSource, String sql) {
        this.dataSource = dataSource;
        this.sql = sql;
    }

    public SQLCustomSelectParams(String dataSource, String sql, FuzzyTypeEn fuzzyTypeEn, boolean keywordLowerCase) {
        this.dataSource = dataSource;
        this.sql = sql;
        this.fuzzyTypeEn = fuzzyTypeEn;
        this.keywordLowerCase = keywordLowerCase;
    }

    *//**
     * @param dataSource
     * @param sql        自定义sql要求必须返回label和value两个字段，里面可以使用的nutzsql参数为【@keyword】
     * @param sqlParams
     *//*
    public SQLCustomSelectParams(String dataSource, String sql, SqlParams sqlParams) {
        this.dataSource = dataSource;
        this.sql = sql;
        this.sqlParams = sqlParams;
    }

    public SQLCustomSelectParams(String dataSource, String sql, Supplier<SqlParams> sqlParamsSupplier) {
        this.dataSource = dataSource;
        this.sql = sql;
        this.sqlParamsSupplier = sqlParamsSupplier;
    }

    public SQLCustomSelectParams(String dataSource, String sql, Supplier<SqlParams> sqlParamsSupplier, Function<List<SelectModel>, List<SelectModel>> postProcessFunc) {
        this(dataSource, sql, sqlParamsSupplier);
        this.postProcessFunc = postProcessFunc;
    }

    public SQLCustomSelectParams(String dataSource, String sql, FuzzyTypeEn fuzzyTypeEn, boolean keywordLowerCase, Supplier<SqlParams> sqlParamsSupplier, Function<List<SelectModel>, List<SelectModel>> postProcessFunc) {
        this(dataSource, sql, sqlParamsSupplier);
        this.postProcessFunc = postProcessFunc;
        this.fuzzyTypeEn = fuzzyTypeEn;
        this.keywordLowerCase = keywordLowerCase;
    }

    @Override
    public String type() {
        return SelectModelTypeEnum.SQL_CUSTOM;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public SqlParams getSqlParams() {
        if (this.sqlParams != null) {
            return sqlParams;
        } else {
            return sqlParamsSupplier != null ? sqlParamsSupplier.get() : null;// 延迟获取，可以在表达式中添加缓存的参数，清理缓存以后会生效
        }
    }

    public void setSqlParams(SqlParams sqlParams) {
        this.sqlParams = sqlParams;
    }

    public List<SelectModel> postProcess(List<SelectModel> models) {
        if (postProcessFunc != null) {
            return postProcessFunc.apply(models);
        } else {
            return models;
        }
    }

//    public FuzzyTypeEn getFuzzyTypeEn() {
//        return fuzzyTypeEn;
//    }
//
//    public void setFuzzyTypeEn(FuzzyTypeEn fuzzyTypeEn) {
//        this.fuzzyTypeEn = fuzzyTypeEn;
//    }

    public boolean isKeywordLowerCase() {
        return keywordLowerCase;
    }

    public void setKeywordLowerCase(boolean keywordLowerCase) {
        this.keywordLowerCase = keywordLowerCase;
    }*/
}
