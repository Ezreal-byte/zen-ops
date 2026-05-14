package com.ops.zen.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class ThreadUtils {

    public static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleepWithoutEx(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public static StackTraceElement[] getStackTrace() {
        return Thread.currentThread().getStackTrace();
//        new Throwable().getStackTrace();
    }
}
