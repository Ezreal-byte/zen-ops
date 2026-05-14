package com.ops.zen.fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/14 10:13
 * @Description
 */
public interface FileService {

    /**
     * @param file 文件的body（InputStream）会关闭
     * @return
     * @throws IOException
     */
    String write(TempFile file) throws IOException;

    String write(InputStream inputStream) throws IOException;

    /**
     * 文件服务输出目标，输出流，和createFileUID配合使用
     *
     * @param uid
     * @return
     * @throws IOException
     */
    OutputStream getOutputStream(String uid) throws IOException;

    String createFileUID();

    InputStream read(String id) throws FileNotFoundException;

    byte[] readByte(String id) throws IOException;

    TempFile getFile(String id) throws FileNotFoundException;

    void delete(String id);

}
