package com.ops.zen.sqlwindow.executor;

import com.ops.zen.en.DbTypeEn;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.utils.Jdbcs;

/**
 * SQL执行器工厂
 */
public class SqlExecutorFactory {

    public static SqlExecutor create(String dbType, Long pkDs) {
        Jdbc jdbc = Jdbcs.getJdbc(String.valueOf(pkDs));
        if (jdbc == null) {
            throw new RuntimeException("无法获取Jdbc实例，数据源ID: " + pkDs);
        }
        return create(dbType, jdbc);
    }

    public static SqlExecutor create(String dbType, Jdbc jdbc) {
        if (DbTypeEn.MYSQL.equalsIgnoreCase(dbType)) {
            return new MySqlSqlExecutor(jdbc);
        } else if (DbTypeEn.ORACLE.equalsIgnoreCase(dbType)) {
            return new OracleSqlExecutor(jdbc);
        } else if (DbTypeEn.POSTGRE_SQL.equalsIgnoreCase(dbType)) {
            return new PostgreSqlSqlExecutor(jdbc);
        } else if (DbTypeEn.CLICK_HOUSE.equalsIgnoreCase(dbType)) {
            return new ClickHouseSqlExecutor(jdbc);
        } else {
            throw new RuntimeException("不支持的数据库类型: " + dbType);
        }
    }

    /**
     * 创建轻量级Executor（仅用于本地URL解析等不需要JDBC连接的操作）
     */
    public static SqlExecutor createLite(String dbType) {
        return create(dbType, (Jdbc) null);
    }
}
