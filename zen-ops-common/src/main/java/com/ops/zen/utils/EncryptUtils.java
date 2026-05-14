package com.ops.zen.utils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密算法工具类
 */
public class EncryptUtils {

    private static final char[] HEX_CODE = "0123456789abcdef".toCharArray();

    public static final String KEY_MD5 = "MD5";

    public static final String KEY_SHA1 = "SHA-1";

    public static final String KEY_SHA256 = "SHA-256";

    /**
     * 数据摘要算法（哈希）
     *
     * @param algorithm 支持 MD5 SHA-1 SHA-256
     * @param source    支持 File InputStream String byte[]
     * @return
     */
    public static String digest(String algorithm, Object source) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            if (source instanceof File) {
                source = new FileInputStream((File) source);
            }
            if (source instanceof byte[]) {
                return toHexString(messageDigest.digest((byte[]) source));
            } else if (source instanceof String) {
                return toHexString(messageDigest.digest(((String) source).getBytes()));
            } else if (source instanceof InputStream) {
                InputStream stream = (InputStream) source;
                byte[] buf = new byte[8192]; // 8K
                int len;
                while ((len = stream.read(buf)) > 0) {
                    messageDigest.update(buf, 0, len);
                }
                return toHexString(messageDigest.digest());
            } else {
                throw new RuntimeException(String.format("Input parameter source type: %s is not supported", source != null ? source.getClass() : null));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (source instanceof InputStream) {
                IOUtils.close((Closeable) source);
            }
        }
    }

    /**
     * 字节数组转16进制字符串，字母小写
     *
     * @param data
     * @return
     */
    public static String toHexString(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(HEX_CODE[(b >> 4) & 0xF]);
            r.append(HEX_CODE[(b & 0xF)]);
        }
        return r.toString();
    }

}
