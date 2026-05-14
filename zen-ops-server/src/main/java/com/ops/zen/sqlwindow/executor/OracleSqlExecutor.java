package com.ops.zen.sqlwindow.executor;

import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.sqlwindow.vo.ColumnVo;
import com.ops.zen.sqlwindow.vo.DatabaseVo;
import com.ops.zen.sqlwindow.vo.TableVo;
import com.ops.zen.jdbc.sql.EasyParams;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Oracle SQL执行器
 */
@Slf4j
public class OracleSqlExecutor extends AbstractSqlExecutor {

    public OracleSqlExecutor(Jdbc jdbc) {
        super(jdbc);
    }

    @Override
    public List<DatabaseVo> listDatabases() throws SQLException {
        String sql = "SELECT USERNAME AS DATABASE_NAME FROM ALL_USERS ORDER BY USERNAME";
        List<EasyRecord> records = jdbc.query(sql, null, null);
        List<DatabaseVo> list = new ArrayList<>();
        if (records != null) {
            for (EasyRecord r : records) {
                list.add(DatabaseVo.builder().databaseName(r.getString("database_name")).build());
            }
        }
        return list;
    }

    @Override
    public List<TableVo> listTables(String database) throws SQLException {
        String sql = "SELECT TABLE_NAME, COMMENTS FROM ALL_TAB_COMMENTS WHERE OWNER = @owner AND TABLE_TYPE = 'TABLE' ORDER BY TABLE_NAME";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("owner", database.toUpperCase()), null);
        List<TableVo> list = new ArrayList<>();
        if (records != null) {
            for (EasyRecord r : records) {
                list.add(TableVo.builder()
                        .tableName(r.getString("table_name"))
                        .comments(r.getString("comments"))
                        .build());
            }
        }
        return list;
    }

    @Override
    public List<ColumnVo> listColumns(String database, String table) throws SQLException {
        String sql = "SELECT T.COLUMN_NAME, T.DATA_TYPE, C.COMMENTS, T.DATA_DEFAULT AS COLUMN_DEFAULT, T.NULLABLE AS IS_NULLABLE " +
                "FROM ALL_TAB_COLUMNS T LEFT JOIN ALL_COL_COMMENTS C ON T.OWNER = C.OWNER AND T.TABLE_NAME = C.TABLE_NAME AND T.COLUMN_NAME = C.COLUMN_NAME " +
                "WHERE T.OWNER = @owner AND T.TABLE_NAME = @tableName ORDER BY T.COLUMN_ID";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("owner", database.toUpperCase()).put("tableName", table.toUpperCase()), null);
        List<ColumnVo> list = new ArrayList<>();
        if (records != null) {
            for (EasyRecord r : records) {
                list.add(ColumnVo.builder()
                        .columnName(r.getString("column_name"))
                        .dataType(r.getString("data_type"))
                        .comments(r.getString("comments"))
                        .columnDefault(r.getString("column_default"))
                        .isNullable(r.getString("is_nullable"))
                        .build());
            }
        }
        return list;
    }

    @Override
    public String getPkColumn(String database, String table) throws SQLException {
        String sql = "SELECT COLUMN_NAME FROM ALL_CONS_COLUMNS A JOIN ALL_CONSTRAINTS B " +
                "ON A.CONSTRAINT_NAME = B.CONSTRAINT_NAME AND A.OWNER = B.OWNER " +
                "WHERE B.CONSTRAINT_TYPE = 'P' AND A.OWNER = @owner AND A.TABLE_NAME = @tableName AND ROWNUM = 1";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("owner", database.toUpperCase()).put("tableName", table.toUpperCase()), null);
        if (records != null && !records.isEmpty()) {
            return records.get(0).getString("column_name");
        }
        return null;
    }

    @Override
    public String parseSchemaFromUrl(String url) {
        // Oracle: jdbc:oracle:thin:@//host:port/schema 或 jdbc:oracle:thin:@host:port:schema
        try {
            String prefix = "jdbc:oracle:thin:@";
            if (!url.startsWith(prefix)) {
                return null;
            }
            String remaining = url.substring(prefix.length());

            // 格式1: //host:port/schema
            if (remaining.startsWith("//")) {
                remaining = remaining.substring(2);
                int slash = remaining.indexOf('/');
                if (slash != -1) {
                    String schemaPart = remaining.substring(slash + 1);
                    int questionMark = schemaPart.indexOf('?');
                    return questionMark != -1 ? schemaPart.substring(0, questionMark) : schemaPart;
                }
            }
            // 格式2: host:port:schema
            else {
                int lastColon = remaining.lastIndexOf(':');
                if (lastColon != -1) {
                    return remaining.substring(lastColon + 1);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Oracle解析URL schema失败: {}", url, e);
            return null;
        }
    }

    @Override
    public Map<String, String> getDatabaseInfo() throws SQLException {
        Map<String, String> info = new LinkedHashMap<>();
        try (Connection conn = jdbc.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            info.put("数据库版本", metaData.getDatabaseProductVersion());

            // 查询当前用户
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL");
                if (rs.next()) {
                    info.put("当前用户", rs.getString(1));
                }
            }

            // 查询会话数
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM V$SESSION");
                if (rs.next()) {
                    info.put("当前会话数", rs.getString(1));
                }
            }
        }
        return info;
    }

    @Override
    public void copyTable(String database, String oldTableName, String newTableName) throws SQLException {
        try (Connection conn = jdbc.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                // Oracle 使用 CREATE TABLE ... AS SELECT
                String sql = "CREATE TABLE " + newTableName + " AS SELECT * FROM " + oldTableName;
                stmt.execute(sql);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public void dropTable(String database, String tableName) throws SQLException {
        try (Connection conn = jdbc.getDataSource().getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                // 删除表（使用 CASCADE CONSTRAINTS 删除约束）
                String sql = "DROP TABLE " + tableName + " CASCADE CONSTRAINTS";
                stmt.execute(sql);
            }
        }
    }
}
