package com.ops.zen.entity.request;

import java.io.File;

/**
 * @author Ezreal
 * @date 2023/9/1 14:27
 * @description 批量文件上传描述(上传文件夹)
 **/
public class SftpUploadFileRequest {

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小字节数
     */
    private long size;//文件大小

    /**
     * 文件绝对路径
     */
    private String webkitRelativePath;

    /**
     * 磁盘文件 前端不直接传递 后端处理 通过id从fsService里获取  set进来
     */
    private File file;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getWebkitRelativePath() {
        return webkitRelativePath;
    }

    public void setWebkitRelativePath(String webkitRelativePath) {
        this.webkitRelativePath = webkitRelativePath;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
