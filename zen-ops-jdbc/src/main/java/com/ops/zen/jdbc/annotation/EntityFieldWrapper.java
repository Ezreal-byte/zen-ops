package com.ops.zen.jdbc.annotation;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class EntityFieldWrapper {

    private String name;

    /**
     * 是否为主键
     *
     * @return
     */
    private boolean pk;

    /**
     * 字段值
     */
    private Object value;

    public EntityFieldWrapper() {
    }

    public EntityFieldWrapper(String name, boolean pk) {
        this.name = name;
        this.pk = pk;
    }

    public EntityFieldWrapper(String name, boolean pk, Object value) {
        this.name = name;
        this.pk = pk;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPk() {
        return pk;
    }

    public boolean pk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
