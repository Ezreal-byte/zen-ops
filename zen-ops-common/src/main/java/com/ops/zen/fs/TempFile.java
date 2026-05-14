package com.ops.zen.fs;

import java.io.File;
import java.io.InputStream;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/14 10:14
 * @Description
 */
public class TempFile {

    private String name;

    private String originName;

    /**
     * 文件id，唯一标识一个文件
     */
    private String id;

    /**
     * 扩展名
     */
    private String extension;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 输入流，读取文件使用
     */
    private InputStream body;

    private File diskFile;

    public TempFile(String id) {
        this.id = id;
    }

    public TempFile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public InputStream getBody() {
        return body;
    }

    public void setBody(InputStream body) {
        this.body = body;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public File getDiskFile() {
        return diskFile;
    }

    public void setDiskFile(File diskFile) {
        this.diskFile = diskFile;
    }
}
