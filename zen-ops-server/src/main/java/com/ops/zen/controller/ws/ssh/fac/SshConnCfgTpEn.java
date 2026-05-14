package com.ops.zen.controller.ws.ssh.fac;


import com.ops.zen.utils.en.EnumDescription;

/**
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public interface SshConnCfgTpEn {

    @EnumDescription(remark = "明文传输-用户名密码方式")
    String PLAINTEXT_PWD = "PL_PWD";

    @EnumDescription(remark = "明文传输-私钥-私钥密码方式")
    String PLAINTEXT_PRV_KEY = "PL_PRV_KEY";

    @EnumDescription(remark = "标识ID")
    String ID = "ID";
}
