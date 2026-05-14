package com.ops.zen.tpl.beetl.ex;

/**
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public class VerifyFailed extends RuntimeException {

    public VerifyFailed(String s) {
        super(s);
    }
}
