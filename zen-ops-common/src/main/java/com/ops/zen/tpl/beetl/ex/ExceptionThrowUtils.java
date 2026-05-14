package com.ops.zen.tpl.beetl.ex;

/**
 * 用于beetl的异常处理
 * 在脚本中使用：--ex.verifyFailed("患者编码和检查号必填其一");
 *
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public class ExceptionThrowUtils {

    public static void throwRtEx(String s) {
        throw new RuntimeException(s);
    }

    public static void verifyFailed(String s) {
        throw new VerifyFailed(s);
    }
}
