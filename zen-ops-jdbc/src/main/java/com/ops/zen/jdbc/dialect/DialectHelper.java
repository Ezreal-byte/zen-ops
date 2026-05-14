package com.ops.zen.jdbc.dialect;


import com.ops.zen.jdbc.Jdbc;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class DialectHelper {

    public static String getPagerSql(DialectEn dialect, String sql, int pageNum, int pageSize) {
        if (pageSize == Jdbc.PAGE_SIZE_MAX && pageNum == 1) {
            //约定这种情况不走分页SQL
            return sql;
        }
        String rtSql = null;
        int offset = pageSize * (pageNum - 1);
        String wrapSql = trimSemicolon(sql);
        switch (dialect) {
            case ORACLE:
                rtSql = String.format("SELECT * FROM (SELECT T.*, ROWNUM RN FROM (%s) T WHERE ROWNUM <= %d) WHERE RN > %d",
                        wrapSql,
                        (pageNum) * pageSize,
                        (pageNum - 1) * pageSize);
                break;
            case CLICKHOUSE:
                rtSql = String.format("SELECT * FROM (%s) t LIMIT %d OFFSET %d", wrapSql, pageSize, offset);
                break;
            case MYSQL:
                rtSql = String.format("SELECT * FROM (%s) t LIMIT %d,%d", wrapSql, (pageNum - 1) * pageSize, pageSize);
                break;
            case POSTGRE:
                rtSql = String.format("SELECT * FROM (%s) t LIMIT %d OFFSET %d", wrapSql, pageSize, offset);
                break;
            case MSSQL:
                // only for >=2012  sql必须有排序，否则语法错误，但是如果有排序，方法getTotalSql会报错
                // rtSql = sql + String.format(" OFFSET %d ROWS FETCH NEXT %d ROW ONLY", pageNum * pageSize, pageSize); // only for >=2012 TODO 2005、2000
//
                // for >=2005 from nutdao Sqlserver2005JdbcExpert
                String xSql = validateAndParseMsSql(wrapSql);

                String pre = String.format("select * from(select row_number()over(order by __tc__)__rn__,* from(select top %d 0 __tc__, ",
                        offset + pageSize);
                String last = String.format(")t)tt where __rn__ > %d", offset);
                rtSql = (pre + xSql + last);
                break;
            default:
                throw new RuntimeException(String.format("未支持的分页方言 %s", dialect));
                //TODO OTHERS
        }
        return rtSql;
    }

    private static String trimSemicolon(String sql) {
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String validateAndParseMsSql(String sql) {
        int firstSelectIndex = sql.toUpperCase().indexOf("SELECT");
        String prefix = sql.substring(0, firstSelectIndex);
        // TODO prefix允许的值\n、\r、\t、空格等
        return sql.substring(firstSelectIndex + 6);
    }

    /**
     * @param dialectEn
     * @param sql
     * @return
     * @edit on 2022/8/9 在原有的SQL前后添加换行 以解决存在表达式是模板解析报错问题
     */
    public static String getTotalSql(DialectEn dialectEn, String sql) {
        return String.format("select count(1) count from (\r\n%s\r\n) talias__", sql); // 以_开头时的表别名-Oracle非法
    }
}
