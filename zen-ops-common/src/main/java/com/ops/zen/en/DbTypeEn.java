package com.ops.zen.en;

import com.ops.zen.utils.en.EnumDescription;

/**
 * @author xyn
 * @date 2025/4/9 20:49
 * @description 数据库类枚举
 **/
public interface DbTypeEn {


    @EnumDescription(remark = "ORACLE")
    String ORACLE = "ORACLE";

    @EnumDescription(remark = "MYSQL")
    String MYSQL = "MYSQL";

    @EnumDescription(remark = "POSTGRE_SQL")
    String POSTGRE_SQL = "POSTGRE_SQL";

    @EnumDescription(remark = "CLICK_HOUSE")
    String CLICK_HOUSE = "CLICK_HOUSE";

    @EnumDescription(remark = "SQL_SERVER")
    String SQL_SERVER = "SQL_SERVER";
}
