package com.ops.zen.phy.vo;


import com.ops.zen.utils.en.EnumDescription;

/**
 * 数据库字段类型枚举
 *
 * @Author xyn
 * @Date 2021/11/11 9:21
 * @Description
 */
public interface EtlFieldTpEn {

    @EnumDescription(remark = "字符串")
    String STRING = "STRING";

    @EnumDescription(remark = "数字")
    String NUMBER = "NUMBER";

    @EnumDescription(remark = "日期时间")
    String DATETIME = "DATETIME";

    @EnumDescription(remark = "blob")
    String BLOB = "BLOB";

    @EnumDescription(remark = "clob")
    String CLOB = "CLOB";

    @EnumDescription(remark = "不支持")
    String UNSUPPORT = "UNSUPPORT";
}
