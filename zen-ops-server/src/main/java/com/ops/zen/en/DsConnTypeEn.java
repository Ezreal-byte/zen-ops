package com.ops.zen.en;

import com.ops.zen.utils.en.EnumDescription;

/**
 * @author xyn
 * @date 2025/4/9 20:49
 * @description  数据库链接类型枚举
 **/
public interface DsConnTypeEn {

    @EnumDescription(remark = "主机")
    String HOST = "HOST";

    @EnumDescription(remark = "URL")
    String URL = "URL";

}
