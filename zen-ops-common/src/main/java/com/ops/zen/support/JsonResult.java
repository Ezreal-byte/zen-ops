package com.ops.zen.support;

public class JsonResult<T> {

    private int code = 0;
    private T data;
    private String msg;
    private String stack;

    /**
     * 给前台一个是否是JsonResult包装结果的标记
     */
    private Boolean jsonResult = true;

    public JsonResult(T data) {
        this.data = data;
    }

    public JsonResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public JsonResult(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public Boolean getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(Boolean jsonResult) {
        this.jsonResult = jsonResult;
    }
}
