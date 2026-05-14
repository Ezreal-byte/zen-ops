package com.ops.zen.en;

import com.ops.zen.utils.en.EnumDescription;

/**
 * @author xyn
 * @date 2025/4/11 21:25
 * @description
 **/
public interface SshLoginTypeEn {

    @EnumDescription(remark = "密码登录")
    String PASSWORD = "0";

    @EnumDescription(remark = "私钥登录")
    String PRIVATE_KEY = "1";

}
