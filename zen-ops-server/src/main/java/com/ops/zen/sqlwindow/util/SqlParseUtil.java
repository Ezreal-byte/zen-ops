package com.ops.zen.sqlwindow.util;

import com.ops.zen.sqlwindow.SqlTypeEn;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL解析工具类（基于JSqlParser）
 */
@Slf4j
public class SqlParseUtil {

    /**
     * 解析SQL类型，返回值只有 DDL 或 DML 两种
     */
    public static String parseSqlType(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof CreateTable || statement instanceof Alter || statement instanceof Drop || statement instanceof Truncate) {
                return SqlTypeEn.DDL;
            } else {
                // Select/Insert/Update/Delete/Show/Desc 等全部归为 DML
                return SqlTypeEn.DML;
            }
        } catch (Exception e) {
            log.warn("SQL解析失败，降级为简单判断: {}", sql, e);
            return fallbackParseSqlType(sql);
        }
    }

    /**
     * 解析DML子类型：SELECT / INSERT / UPDATE / DELETE
     */
    public static String parseDmlType(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                return "SELECT";
            } else if (statement instanceof Insert) {
                return "INSERT";
            } else if (statement instanceof Update) {
                return "UPDATE";
            } else if (statement instanceof Delete) {
                return "DELETE";
            }
        } catch (Exception e) {
            log.warn("DML类型解析失败: {}", sql);
        }
        return null;
    }

    /**
     * 获取SQL中涉及的表名列表
     */
    public static List<String> getTableNames(String sql) {
        List<String> tables = new ArrayList<>();
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            TablesNamesFinder finder = new TablesNamesFinder();
            if (statement instanceof Select) {
                tables.addAll(finder.getTableList((Select) statement));
            } else if (statement instanceof Insert) {
                tables.addAll(finder.getTableList((Insert) statement));
            } else if (statement instanceof Update) {
                tables.addAll(finder.getTableList((Update) statement));
            } else if (statement instanceof Delete) {
                tables.addAll(finder.getTableList((Delete) statement));
            }
        } catch (Exception e) {
            log.warn("获取表名失败: {}", sql, e);
        }
        return tables;
    }

    /**
     * 判断是否为单表查询
     */
    public static boolean isSingleTableQuery(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select)) {
                return false;
            }
            Select select = (Select) statement;
            SelectBody selectBody = select.getSelectBody();
            if (!(selectBody instanceof PlainSelect)) {
                return false;
            }
            PlainSelect plainSelect = (PlainSelect) selectBody;
            // 检查是否有JOIN、UNION等
            if (plainSelect.getJoins() != null && !plainSelect.getJoins().isEmpty()) {
                return false;
            }
            // 检查是否只有一张表
            TablesNamesFinder finder = new TablesNamesFinder();
            List<String> tables = finder.getTableList(select);
            return tables.size() == 1;
        } catch (Exception e) {
            log.warn("单表查询判断失败: {}", sql, e);
            return false;
        }
    }

    /**
     * 获取单表查询的表名（不含schema前缀）
     * 如 SQL 为 SELECT * FROM zen_ops.cg_tpl，则返回 cg_tpl
     */
    public static String getSingleTableName(String sql) {
        String fullName = getSingleFullTableName(sql);
        if (fullName == null) return null;
        // 去掉 schema 前缀：zen_ops.cg_tpl -> cg_tpl
        int dotIdx = fullName.indexOf('.');
        String table = dotIdx >= 0 ? fullName.substring(dotIdx + 1) : fullName;
        return stripBackticks(table);
    }

    /**
     * 获取单表查询中的schema（SQL中指定的情况下）
     * 如 SQL 为 SELECT * FROM zen_ops.cg_tpl，则返回 zen_ops
     * 如 SQL 为 SELECT * FROM cg_tpl（未指定schema），则返回 null
     */
    public static String getSingleTableSchema(String sql) {
        String fullName = getSingleFullTableName(sql);
        if (fullName == null) return null;
        int dotIdx = fullName.indexOf('.');
        return dotIdx >= 0 ? stripBackticks(fullName.substring(0, dotIdx)) : null;
    }

    /**
     * 去除反引号、双引号等标识符引用符号
     */
    private static String stripBackticks(String name) {
        if (name == null) return null;
        // 去除首尾的反引号、双引号、方括号
        String trimmed = name.trim();
        if ((trimmed.startsWith("`") && trimmed.endsWith("`"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    /**
     * 获取单表查询的完整表名（可能包含schema前缀）
     */
    private static String getSingleFullTableName(String sql) {
        try {
            if (!isSingleTableQuery(sql)) {
                return null;
            }
            List<String> tables = getTableNames(sql);
            return tables.isEmpty() ? null : tables.get(0);
        } catch (Exception e) {
            log.warn("获取单表名失败: {}", sql, e);
            return null;
        }
    }

    /**
     * 降级解析：简单字符串判断
     */
    private static String fallbackParseSqlType(String sql) {
        String trimmed = removeComments(sql).trim();
        if (trimmed.isEmpty()) {
            return SqlTypeEn.DML;
        }
        String firstWord = trimmed.split("\\s+")[0].toUpperCase();
        switch (firstWord) {
            case "CREATE":
            case "DROP":
            case "ALTER":
            case "TRUNCATE":
                return SqlTypeEn.DDL;
            default:
                // SELECT/INSERT/UPDATE/DELETE/SHOW/DESC 等全部归为 DML
                return SqlTypeEn.DML;
        }
    }

    private static String removeComments(String sql) {
        String noSingleLine = sql.replaceAll("--[^\n]*", "");
        return noSingleLine.replaceAll("/\\*[\\s\\S]*?\\*/", "");
    }

    /**
     * 提取SQL中的注释（-- 行注释 和 /* 多行注释）
     */
    public static List<String> extractComments(String sql) {
        List<String> comments = new ArrayList<>();
        if (sql == null || sql.isEmpty()) {
            return comments;
        }
        // 提取 -- 行注释
        Pattern singleLine = Pattern.compile("--[^\\r\\n]*");
        Matcher m1 = singleLine.matcher(sql);
        while (m1.find()) {
            comments.add(m1.group().trim());
        }
        // 提取 /* */ 多行注释
        Pattern multiLine = Pattern.compile("/\\*[\\s\\S]*?\\*/");
        Matcher m2 = multiLine.matcher(sql);
        while (m2.find()) {
            comments.add(m2.group().trim());
        }
        return comments;
    }
}
