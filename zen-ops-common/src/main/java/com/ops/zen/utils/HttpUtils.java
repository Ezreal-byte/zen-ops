package com.ops.zen.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaoyingnan
 * @version 2020/8/25 16:57
 * <文件说明>
 **/
public class HttpUtils {
    /**
     * 获取调用者的ip地址
     *
     * @param request
     * @return
     */
    public static String getIpByRequest(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");//nginx or others 大部分的代理或者网关都会加上x-forwarded-for
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");//apache
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");//weblogic
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        ip = ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
        return ip;
    }

    /**
     * 将内容以流的形式显示出去
     *
     * @param plainText
     * @param response
     * @throws IOException
     */
    public static void flushHtmlStr(String plainText, HttpServletResponse response) throws IOException {
        response.setContentType(String.format("%s;charset=UTF-8", HttpContentTypeEn.HTML));
        OutputStream os = response.getOutputStream();
        IOUtils.write(plainText, os);
        os.flush();
        os.close();
    }

    /**
     * @param plainText
     * @param response
     * @throws IOException
     */
    public static void flushJsonStr(String plainText, HttpServletResponse response) throws IOException {
        response.setContentType(String.format("%s;charset=UTF-8", HttpContentTypeEn.JSON));
        OutputStream os = response.getOutputStream();
        IOUtils.write(plainText, os);
        os.flush();
        os.close();
    }

    /**
     * @param plainText
     * @param response
     * @throws IOException
     */
    public static void flushXmlStr(String plainText, HttpServletResponse response) throws IOException {
        response.setContentType(String.format("%s;charset=UTF-8", HttpContentTypeEn.XML));
        OutputStream os = response.getOutputStream();
        IOUtils.write(plainText, os);
        os.flush();
        os.close();
    }

    /**
     * @param plainText
     * @param response
     * @throws IOException
     */
    public static void flushStr(String plainText, HttpServletResponse response, String contentType) throws IOException {
        response.setContentType(String.format("%s;charset=UTF-8", contentType));
        OutputStream os = response.getOutputStream();
        IOUtils.write(plainText, os);
        os.flush();
        os.close();
    }

    public static void flushJpeg(HttpServletResponse response, byte[] blobHeader) throws IOException {
        response.setContentType(String.format("%s;charset=UTF-8", HttpContentTypeEn.JPEG));
        OutputStream os = response.getOutputStream();
        IOUtils.write(blobHeader, os);
        os.flush();
        os.close();
    }

    public static Map<String, String> queryString2Map(String queryString) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(queryString))
            return map;
        String[] split = queryString.split("&");
        if (split.length == 0)
            return map;
        for (String s : split) {
            String[] split1 = s.split("=");
            if (split1.length == 2) {
                map.put(split1[0], split1[1]);
            }
        }
        return map;
    }
}
