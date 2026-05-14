package com.ops.zen.phy.meta;

/**
 * @Author xyn
 * @Date 2021/6/16 9:39
 * @Description
 */
public class DBFieldMeta {

    private int sqlType;

    private int precision;

    private int scale;

    private String colName;

    private String colType;

    private Class<?> javaType;

    private String javaTypeShortName;

    private String colNameCamel;

    public DBFieldMeta(int sqlType, int precision, int scale, String colName, String colType, Class<?> javaType, String javaTypeShortName, String colNameCamel) {
        this.sqlType = sqlType;
        this.precision = precision;
        this.scale = scale;
        this.colName = colName;
        this.colType = colType;
        this.javaType = javaType;
        this.javaTypeShortName = javaTypeShortName;
        this.colNameCamel = colNameCamel;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public String getJavaTypeShortName() {
        return javaTypeShortName;
    }

    public void setJavaTypeShortName(String javaTypeShortName) {
        this.javaTypeShortName = javaTypeShortName;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColNameCamel() {
        return colNameCamel;
    }

    public void setColNameCamel(String colNameCamel) {
        this.colNameCamel = colNameCamel;
    }

    public String getColType() {
        return colType;
    }

    public void setColType(String colType) {
        this.colType = colType;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
}
