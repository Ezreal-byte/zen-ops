package com.ops.zen.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/3 11:08
 * @Description
 */
public abstract class UpDownLoader {


    public static void downLoad(String fileName, String fileContent, HttpServletResponse response)
            throws IOException {
        setHeaderWithoutSize(response, fileName);
        OutputStream os = response.getOutputStream();
        IOUtils.write(fileContent, os);
        os.flush();
        os.close();
    }

    public static void downLoad(String fileName, byte[] fileContent, HttpServletResponse response)
            throws IOException {
        setHeaderWithoutSize(response, fileName);
        OutputStream os = response.getOutputStream();
        IOUtils.write(fileContent, os);
        os.flush();
        os.close();
    }

    public static void downLoad(String fileName, InputStream fileContent, HttpServletResponse response)
            throws IOException {
        setHeaderWithoutSize(response, fileName);
        OutputStream os = response.getOutputStream();
        IOUtils.write(fileContent, os);
        os.flush();
        os.close();
    }

    public static void downLoad(String filePath, HttpServletResponse response)
            throws IOException {
        File file = new File(filePath);
        setHeaderWithoutSize(response, file.getName() + ".zip");
        OutputStream os = response.getOutputStream();
        ZipUtils.zip(file, os);
        os.flush();
        os.close();
    }

    public static void downLoadZipDir(String dirPath, String fileName, HttpServletResponse response)
            throws IOException {
        File file = new File(dirPath);
        downLoadZipDir(file, fileName, response);
    }

    public static void downLoadZipDir(File dirPath, String fileName, HttpServletResponse response)
            throws IOException {
        setHeaderWithoutSize(response, fileName + ".zip");
        OutputStream os = response.getOutputStream();
        ZipUtils.zip(dirPath, os);
        os.flush();
        os.close();
    }

    private static void setHeaderWithoutSize(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        //response.setHeaderWithoutSize("Content-Disposition",String.format("attachment; filename=\"%s\"", fileName));
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));
        //response.addHeader("Content-Length", length + "");
        response.setContentType("application/octet-stream;charset=UTF-8");
    }

    private static void setHeaderWithSize(HttpServletResponse response, String fileName, long size) throws UnsupportedEncodingException {
        //response.setHeaderWithoutSize("Content-Disposition",String.format("attachment; filename=\"%s\"", fileName));
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));
        response.addHeader("Content-Length", size + "");
        response.setContentType("application/octet-stream;charset=UTF-8");
    }

    public static void image(String filePath, HttpServletResponse response)
            throws IOException {
        File file = new File(filePath);
        response.setHeader("content-type", "image/png");
        OutputStream os = response.getOutputStream();
        IOUtils.write(new FileInputStream(file), os);
        os.flush();
        os.close();
    }

    public static void downLoad(String fileName, InputStream fileContent, HttpServletResponse response, long size)
            throws IOException {
        setHeaderWithSize(response, fileName, size);
        OutputStream os = response.getOutputStream();
        IOUtils.write(fileContent, os);
        os.flush();
        os.close();
    }
}
