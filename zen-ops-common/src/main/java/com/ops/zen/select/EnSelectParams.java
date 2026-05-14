package com.ops.zen.select;

/**
 * @author xyn
 * @date 2025/4/9 20:58
 * @description
 **/
public class EnSelectParams implements ISelectParams {

    private String className;

    /**
     * 枚举类名
     */
    private Class<?> enClazz;

    /**
     * 枚举值分组
     */
    private String group;

    public EnSelectParams(Class<?> enClazz, String group) {
        this.enClazz = enClazz;
        this.group = group;
    }

    public EnSelectParams(Class<?> enClazz) {
        this.enClazz = enClazz;
    }

    public Class<?> getEnClazz() {
        return enClazz;
    }

    public EnSelectParams() {
    }

    public void setEnClazz(Class<?> enClazz) {
        this.enClazz = enClazz;
    }

    @Override
    public String type() {
        return SelectModelTypeEnum.ENUM;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
