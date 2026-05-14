package com.ops.zen.utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @Author xyn
 * @Date 2022/11/7 19:44
 * @Description
 */
public class Base64Utils {

    public static String encode(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static String encode(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        byte[] encode = Base64.getEncoder().encode(bytes);
        try {
            return new String(encode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decode(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return Base64.getDecoder().decode(str);
    }

    public static String decode2Str(String str) {
        try {
            if (StringUtils.isBlank(str)) {
                return null;
            }
            return new String(decode(str), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
