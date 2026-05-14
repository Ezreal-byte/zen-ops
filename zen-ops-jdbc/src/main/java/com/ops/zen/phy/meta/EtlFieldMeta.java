package com.ops.zen.phy.meta;

import com.ops.zen.phy.vo.EtlFieldTpEn;

import java.util.Objects;

/**
 * etl数据库字段元数据
 *
 * @Author xyn
 * @Date 2021/11/11 9:21
 * @Description
 */
public class EtlFieldMeta {

    /**
     * 字段类型 {@link EtlFieldTpEn}
     */
    String type;

    /**
     * 字段名
     */
    String name;

    /**
     * 中文名
     */
    String cnName;

    /**
     * 描述，备注
     */
    String des;

    /**
     * 精度，长度
     */
    int precision;

    /**
     * 小数点右侧精度
     */
    int scale;

    /**
     * 数据库真实类型，如mysql的tinyint
     */
    String colType;

    /**
     * {@link java.sql.Types}
     */
    int sqlType;

    /**
     * 默认值
     */
    String defValue;

    /**
     * 是否可为null
     */
    boolean nullable = true;

    /**
     * 维度、测度{@link FldAnalyTp}
     */
    String biAnalyTp;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
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

    public String getColType() {
        return colType;
    }

    public void setColType(String colType) {
        this.colType = colType;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public String getDefValue() {
        return defValue;
    }

    public void setDefValue(String defValue) {
        this.defValue = defValue;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getBiAnalyTp() {
        return biAnalyTp;
    }

    public void setBiAnalyTp(String biAnalyTp) {
        this.biAnalyTp = biAnalyTp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtlFieldMeta that = (EtlFieldMeta) o;
        return precision == that.precision &&
                scale == that.scale &&
                sqlType == that.sqlType &&
                nullable == that.nullable &&
                Objects.equals(type, that.type) &&
                Objects.equals(name, that.name) &&
//                Objects.equals(des, that.des) &&
                Objects.equals(colType, that.colType) &&
                Objects.equals(biAnalyTp, that.biAnalyTp) &&
                Objects.equals(defValue, that.defValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, des, precision, scale, colType, sqlType, defValue, nullable, biAnalyTp);
    }
}
