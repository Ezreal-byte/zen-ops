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
 * PostgreSQL SQL执行器
 */
@Slf4j
public class PostgreSqlSqlExecutor extends AbstractSqlExecutor {

    public PostgreSqlSqlExecutor(Jdbc jdbc) {
        super(jdbc);
    }

    @Override
    public List<DatabaseVo> listDatabases() throws SQLException {
        String sql = "SELECT DATNAME AS DATABASE_NAME FROM PG_DATABASE WHERE DATISTEMPLATE = FALSE ORDER BY DATNAME";
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
        String sql = "SELECT TABLE_NAME, OBJ_DESCRIPTION((TABLE_SCHEMA||'.'||TABLE_NAME)::REGCLASS, 'pg_class') AS COMMENTS " +
                "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = @schema AND TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME";
        String schema = database != null && !database.isEmpty() ? database : "public";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("schema", schema), null);
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
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, COL_DESCRIPTION((TABLE_SCHEMA||'.'||TABLE_NAME)::REGCLASS, ORDINAL_POSITION) AS COMMENTS, " +
                "COLUMN_DEFAULT, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = @tableName ORDER BY ORDINAL_POSITION";
        String schema = database != null && !database.isEmpty() ? database : "public";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("schema", schema).put("tableName", table), null);
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
        String sql = "SELECT KCU.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS TC " +
                "JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU ON TC.CONSTRAINT_NAME = KCU.CONSTRAINT_NAME AND TC.TABLE_SCHEMA = KCU.TABLE_SCHEMA " +
                "WHERE TC.CONSTRAINT_TYPE = 'PRIMARY KEY' AND TC.TABLE_SCHEMA = @schema AND TC.TABLE_NAME = @tableName LIMIT 1";
        String schema = database != null && !database.isEmpty() ? database : "public";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("schema", schema).put("tableName", table), null);
        if (records != null && !records.isEmpty()) {
            return records.get(0).getString("column_name");
        }
        return null;
    }

    @Override
    public String parseSchemaFromUrl(String url) {
        // PostgreSQL: jdbc:postgresql://host:port/schema?params
        try {
            String prefix = "jdbc:postgresql://";
            if (!url.startsWith(prefix)) {
                return null;
            }
            String remaining = url.substring(prefix.length());
            int schemaStart = remaining.indexOf('/', 1);
            if (schemaStart == -1) {
                return null;
            }
            String schemaPart = remaining.substring(schemaStart + 1);
            int questionMark = schemaPart.indexOf('?');
            if (questionMark != -1) {
                schemaPart = schemaPart.substring(0, questionMark);
            }
            int slash = schemaPart.indexOf('/');
            if (slash != -1) {
                schemaPart = schemaPart.substring(0, slash);
            }
            return schemaPart.isEmpty() ? null : schemaPart;
        } catch (Exception e) {
            log.warn("PostgreSQL解析URL schema失败: {}", url, e);
            return null;
        }
    }

    @Override
    public Map<String, String> getDatabaseInfo() throws SQLException {
        Map<String, String> info = new LinkedHashMap<>();
        try (Connection conn = jdbc.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            info.put("数据库版本", metaData.getDatabaseProductVersion());
                
            // 查询当前数据库
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT current_database()");
                if (rs.next()) {
                    info.put("当前数据库", rs.getString(1));
                }
            }
                
            // 查询活跃连接数
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM pg_stat_activity");
                if (rs.next()) {
                    info.put("活跃连接数", rs.getString(1));
                }
            }
                
            // 查询最大连接数
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SHOW max_connections");
                if (rs.next()) {
                    info.put("最大连接数", rs.getString(1));
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
                // PostgreSQL 使用 CREATE TABLE ... AS 一次性复制结构和数据
                String sql = "CREATE TABLE " + newTableName + " AS TABLE " + oldTableName;
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
                // 删除表（使用 CASCADE 删除依赖对象）
                String sql = "DROP TABLE " + tableName + " CASCADE";
                stmt.execute(sql);
            }
        }
    }
}
