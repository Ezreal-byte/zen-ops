package com.ops.zen.entity.response;

import com.jcraft.jsch.ChannelSftp;

import java.util.List;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class SftpInitVo {

    private String id;

    private List<ChannelSftp.LsEntry> files;

    private String initPath;

    public SftpInitVo(String id, List<ChannelSftp.LsEntry> files, String initPath) {
        this.id = id;
        this.files = files;
        this.initPath = initPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ChannelSftp.LsEntry> getFiles() {
        return files;
    }

    public void setFiles(List<ChannelSftp.LsEntry> files) {
        this.files = files;
    }

    public String getInitPath() {
        return initPath;
    }

    public void setInitPath(String initPath) {
        this.initPath = initPath;
    }
}
