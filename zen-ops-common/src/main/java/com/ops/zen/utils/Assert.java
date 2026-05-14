/**
 *
 */
package com.ops.zen.utils;

import java.util.List;

/**
 * @author xiaoyingnan
 *
 */
public class Assert {

    public static <T extends Exception> void assertNotNull(boolean isNull, T ex) throws T {
        if (isNull) {
            throw ex;
        }
    }

    public static <T extends Exception> void assertTrue(boolean isTrue, T ex) throws T {
        if (!isTrue) {
            throw ex;
        }
    }

    public static void isTrue(boolean b) {
        if (!b) {
            throw new IllegalArgumentException("参数不能为空");
        }
    }

    public static void isTrue(boolean b, String format, Object... params) {
        if (!b) {
            throw new RuntimeException(String.format(format, params));
        }
    }

    public static void isTrue(boolean b, String falseStr) {
        if (!b)
            throw new RuntimeException(falseStr);
    }


    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, String format, Object... params) {
        if (object == null) {
            throw new IllegalArgumentException(String.format(format, params));
        }
    }

    public static void notEmpty(Object object, String message) {
        if (object == null || object.toString().length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Object object, String format, Object... params) {
        if (object == null || object.toString().length() == 0) {
            throw new IllegalArgumentException(String.format(format, params));
        }
    }

    public static void notEmpty(List list, String errorStr) {
        if (list == null || list.size() == 0) {
            throw new IllegalArgumentException(errorStr);
        }
    }

}
