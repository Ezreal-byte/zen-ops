package com.ops.zen.utils;

import java.util.UUID;

/**
 * @author Ezreal
 * @date 2021/6/16 10:05
 * @description
 **/
public class UUIDUtils {

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    public static String uuidWithoutHorizonBar() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String uuidWithoutHorizonBar(String suffix) {
        return String.format("%s-%s", uuidWithoutHorizonBar(), suffix);
    }

}
