package com.ops.zen.utils.ex;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/9 8:56
 * @Description
 */
public abstract class Exceptions {

    public static String trace(Exception e) {
        return trace((Throwable) e);
    }

    public static String trace(Throwable e) {
        if (e == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out));
        byte[] outBytes = out.toByteArray();
        return new String(outBytes, StandardCharsets.UTF_8);
    }

    public static String trace(Object e) {
        if (e instanceof Exception) {
            return trace((Exception) e);
        }
        return null;
    }

    public static void throwAsRuntimeException(Exception e) {
        throwAsRuntimeException((Throwable) e);
    }

    public static RuntimeException wrapAsRt(Throwable e) {
        if(e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException(e);
    }

    public static void throwAsRuntimeException(String msg) {
        throw new RuntimeException(msg);
    }

    public static void throwAsRuntimeException(String msg, Exception e) {
        throw new RuntimeException(msg, e);
    }

    public static void throwAsRuntimeException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            throw new RuntimeException(throwable);
        }

    }

    public static RuntimeException methodNotImplementionYet() {
        return new RuntimeException("方法未实现");
    }

    public static String calledStackTrace(int depth) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        int length = stackTrace.length - 1;
        if (stackTrace == null || length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= depth && i < stackTrace.length; i++) {
            sb.append(stackTrace[i].toString()).append("\n");
        }
        return sb.toString();
    }
}
