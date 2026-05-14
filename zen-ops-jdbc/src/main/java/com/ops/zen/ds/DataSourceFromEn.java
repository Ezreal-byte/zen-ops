package com.ops.zen.ds;


import com.ops.zen.utils.en.EnumDescription;

/**
 * 数据来源
 *
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public interface DataSourceFromEn {

    @EnumDescription(remark = "来源properties")
    String PROPERTIES = "PROPERTIES";

    @EnumDescription(remark = "来源数据库配置")
    String CONFIG = "CONFIG";

}
