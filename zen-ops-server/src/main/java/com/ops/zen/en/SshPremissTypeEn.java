package com.ops.zen.en;

import com.ops.zen.utils.en.EnumDescription;

/**
 * @author xyn
 * @date 2025/4/11 15:31
 * @description ssh权限枚举
 **/
public interface SshPremissTypeEn {

    @EnumDescription(remark = "拥有者")
    String CREATE_USER = "0";

    @EnumDescription(remark = "被分享者")
    String SHARE_USER = "1";
}
