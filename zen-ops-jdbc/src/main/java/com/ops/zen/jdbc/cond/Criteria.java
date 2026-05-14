package com.ops.zen.jdbc.cond;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class Criteria {

    private String name;

    private String logicOp;

    private Object value;

    private boolean and;

    public Criteria(String name, String logicOp, Object value) {
        this.name = name;
        this.logicOp = logicOp;
        this.value = value;
    }

    public Criteria(String name, String logicOp, Object value, boolean and) {
        this.name = name;
        this.logicOp = logicOp;
        this.value = value;
        this.and = and;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogicOp() {
        return logicOp;
    }

    public void setLogicOp(String logicOp) {
        this.logicOp = logicOp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isAnd() {
        return and;
    }

    public void setAnd(boolean and) {
        this.and = and;
    }
}
