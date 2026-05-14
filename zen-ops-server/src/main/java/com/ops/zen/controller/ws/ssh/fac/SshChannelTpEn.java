package com.ops.zen.controller.ws.ssh.fac;


import com.ops.zen.utils.en.EnumDescription;

/**
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public interface SshChannelTpEn {

    @EnumDescription(remark = "SHELL")
    String SHELL = "shell";

    @EnumDescription(remark = "SFTP")
    String SFTP = "sftp";

}
