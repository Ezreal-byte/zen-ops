package com.ops.zen.utils.en;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/23 13:07
 * @Description
 */
public class SelectModel {

    private String label;//对应remark

    private String value;

    private String group;

    private String extType;

    private Class<?> extTypeClazz;

    private String shortName;

    private String extDefaultValue;

    public SelectModel() {
    }

    public SelectModel(String label, String value, String group) {
        this.value = value;
        this.label = label;
        this.group = group;
    }

    public SelectModel(String label, String value, String group, Class<?> extType, String shortName, String extDefaultValue) {
        this.label = label;
        this.value = value;
        this.group = group;
        this.extTypeClazz = extType;
        if (this.extTypeClazz != null) {
            this.extType = this.extTypeClazz.getName();
        }
        this.shortName = shortName;
        this.extDefaultValue = extDefaultValue;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getExtType() {
        return extType;
    }

    public void setExtType(String extType) {
        this.extType = extType;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getExtDefaultValue() {
        return extDefaultValue;
    }

    public void setExtDefaultValue(String extDefaultValue) {
        this.extDefaultValue = extDefaultValue;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Class<?> getExtTypeClazz() {
        return extTypeClazz;
    }

    public void setExtTypeClazz(Class<?> extTypeClazz) {
        this.extTypeClazz = extTypeClazz;
    }
}
