package com.ops.zen.controller.ws.ssh.sftp;

import com.jcraft.jsch.ChannelSftp;

import java.time.LocalDateTime;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class ChannelSftpWrapper {

    private ChannelSftp ftp;

    private LocalDateTime dtLastVisit;

    public ChannelSftpWrapper(ChannelSftp ftp) {
        this.ftp = ftp;
        dtLastVisit = LocalDateTime.now();
    }

    public ChannelSftp getFtp() {
        return ftp;
    }

    public void setFtp(ChannelSftp ftp) {
        this.ftp = ftp;
    }

    public LocalDateTime getDtLastVisit() {
        return dtLastVisit;
    }

    public void setDtLastVisit(LocalDateTime dtLastVisit) {
        this.dtLastVisit = dtLastVisit;
    }
}
