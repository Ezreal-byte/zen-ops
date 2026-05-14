package com.ops.zen.jdbc.dialect;

import com.ops.zen.utils.Assert;
//import org.nutz.dao.Dao;
//import org.nutz.dao.impl.jdbc.mysql.MysqlJdbcExpert;
//import org.nutz.dao.impl.jdbc.oracle.OracleJdbcExpert;
//import org.nutz.dao.impl.jdbc.psql.PsqlJdbcExpert;
//import org.nutz.dao.impl.jdbc.sqlserver2000.Sqlserver2000JdbcExpert;
//import org.nutz.dao.impl.jdbc.sqlserver2005.Sqlserver2005JdbcExpert;
//import org.nutz.dao.impl.jdbc.sqlserver2012.Sqlserver2012JdbcExpert;
//import org.nutz.dao.jdbc.JdbcExpert;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class SoarDbHelper {

//    public static String getTp(Dao dao) {
//        JdbcExpert jdbcExpert = dao.getJdbcExpert();
//        Assert.notNull(jdbcExpert, "dao的jdbcExpert为null");
//        if (jdbcExpert instanceof OracleJdbcExpert) {
//            return SoarDialectEn.ORACLE;
//        } else if (jdbcExpert instanceof Sqlserver2000JdbcExpert) {
//            return SoarDialectEn.MSSQL2000;
//        } else if (jdbcExpert instanceof Sqlserver2005JdbcExpert) {
//            return SoarDialectEn.MSSQL2005;
//        } else if (jdbcExpert instanceof Sqlserver2012JdbcExpert) {
//            return SoarDialectEn.MSSQL2012;
//        } else if (jdbcExpert instanceof PsqlJdbcExpert) {
//            return SoarDialectEn.POSTGRE;
//        } else if (jdbcExpert instanceof MysqlJdbcExpert) {
//            return SoarDialectEn.MYSQL;
//        } else {
//            throw new RuntimeException("未支持的nutdao方言类型" + jdbcExpert.getClass());
//        }
//    }

    public static String parseSqlFileName(String sqlFileName, DialectEn dbTpEn) {
        String dbTp = parseDialect(dbTpEn);
        if (dbTp == null) {
            throw new RuntimeException(String.format("不支持的数据库类型：%s", dbTpEn));
        }
        return parseSqlFileName(sqlFileName, dbTp);
    }

    public static String parseSqlFileName(String sqlFileName, String dbTp) {
        Assert.notNull(sqlFileName, "sql file name can not be null");
        Assert.notNull(dbTp, "db type name can not be null");
        if (sqlFileName.endsWith(".md") || sqlFileName.endsWith(".sql")) {
            int i = sqlFileName.lastIndexOf(".");
            return sqlFileName.substring(0, i).concat("-").concat(dbTp).concat(sqlFileName.substring(i));
        } else {
            return sqlFileName.concat("-").concat(dbTp);
        }
    }

    public static String parseDialect(DialectEn dialect) {
        String dbTp = null;

        switch (dialect) {
            case ORACLE:
                dbTp = SoarDialectEn.ORACLE;
                break;
            case CLICKHOUSE:
                dbTp = SoarDialectEn.CLICKHOUSE;
                break;
            case MYSQL:
                dbTp = SoarDialectEn.ORACLE;
                break;
            case MSSQL:
                dbTp = SoarDialectEn.MSSQL2000;
                break;
            case POSTGRE:
                dbTp = SoarDialectEn.POSTGRE;
                break;
            default:
        }
        return dbTp;
    }
}
