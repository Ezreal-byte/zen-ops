package com.ops.zen.phy.impl;


import com.ops.zen.en.DbTypeEn;
import com.ops.zen.phy.PhyTableTool;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2021/11/16 10:47
 * @Description
 */
public class TableTools {

    static Map<String, PhyTableTool> tools = new HashMap<>();

    static {
        tools.put(DbTypeEn.MYSQL, new PhyTableToolMysql());
        tools.put(DbTypeEn.ORACLE, new PhyTableToolOracle());
        tools.put(DbTypeEn.SQL_SERVER, new PhyTableToolMSsql());
        tools.put(DbTypeEn.CLICK_HOUSE, new PhyTableToolClickHouse());
        tools.put(DbTypeEn.POSTGRE_SQL, new PhyTableToolPostgreSQL());
    }

    public static PhyTableTool tool(String tp) {
        PhyTableTool phyTableTool = tools.get(tp);
        if (phyTableTool == null) {
            throw new RuntimeException(String.format("未支持的数据库类型 %s", tp));
        }
        return phyTableTool;
    }


//    public static PhyTableTool toolByDialectEn(String tp) {
//        switch (tp) {
//            case SoarDialectEn.POSTGRE:
//                return tools.get(DbTypeEn.POSTGRE_SQL);
//            case SoarDialectEn.ORACLE:
//                return tools.get(DbTypeEn.ORACLE);
//            case SoarDialectEn.MYSQL:
//                return tools.get(DbTypeEn.MYSQL);
//            case SoarDialectEn.MSSQL2000:
//            case SoarDialectEn.MSSQL2005:
//            case SoarDialectEn.MSSQL2012:
//                return tools.get(DbTypeEn.SQL_SERVER);
//            case SoarDialectEn.CLICKHOUSE:
//                return tools.get(DbTypeEn.CLICK_HOUSE);
//            default:
//                throw new RuntimeException(String.format("未支持的方言类型 %s", tp));
//        }
//    }
}
