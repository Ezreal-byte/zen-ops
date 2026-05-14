package com.ops.zen.utils.xlsx;

/**
 * @Author xiaoyingnan
 * @Date 2021/3/30 17:14
 * @Description
 */
public class XCell {

    private String value;

    public XCell(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
