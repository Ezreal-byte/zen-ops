package com.ops.zen.sqlwindow.executor;

import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.sqlwindow.vo.ColumnVo;
import com.ops.zen.sqlwindow.vo.DatabaseVo;
import com.ops.zen.sqlwindow.vo.TableVo;
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
 * ClickHouse SQL执行器
 */
@Slf4j
public class ClickHouseSqlExecutor extends AbstractSqlExecutor {

    public ClickHouseSqlExecutor(Jdbc jdbc) {
        super(jdbc);
    }

    @Override
    public List<DatabaseVo> listDatabases() throws SQLException {
        String sql = "SELECT name AS DATABASE_NAME FROM system.databases WHERE name NOT IN ('system','information_schema') ORDER BY name";
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
        String sql = "SELECT name AS TABLE_NAME FROM system.tables WHERE database = @db AND engine NOT IN ('View','MaterializedView') ORDER BY name";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("db", database), null);
        List<TableVo> list = new ArrayList<>();
        if (records != null) {
            for (EasyRecord r : records) {
                list.add(TableVo.builder()
                        .tableName(r.getString("table_name"))
                        .comments("")
                        .build());
            }
        }
        return list;
    }

    @Override
    public List<ColumnVo> listColumns(String database, String table) throws SQLException {
        String sql = "SELECT name AS COLUMN_NAME, type AS DATA_TYPE, comment AS COMMENTS, " +
                "default_expression AS COLUMN_DEFAULT, 'YES' AS IS_NULLABLE " +
                "FROM system.columns WHERE database = @db AND table = @tableName ORDER BY position";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("db", database).put("tableName", table), null);
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
        // ClickHouse 传统表引擎（MergeTree系列）使用 ORDER BY 作为排序键，不是传统主键
        // 这里查询 ORDER BY 的第一个字段作为逻辑主键
        String sql = "SELECT primary_key FROM system.tables WHERE database = @db AND name = @tableName LIMIT 1";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("db", database).put("tableName", table), null);
        if (records != null && !records.isEmpty()) {
            String pk = records.get(0).getString("primary_key");
            if (pk != null && !pk.isEmpty()) {
                // 取第一个字段
                return pk.split(",")[0].trim();
            }
        }
        return null;
    }

    @Override
    public String parseSchemaFromUrl(String url) {
        // ClickHouse: jdbc:clickhouse://host:port/schema?params
        try {
            String prefix = "jdbc:clickhouse://";
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
            log.warn("ClickHouse解析URL schema失败: {}", url, e);
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
                ResultSet rs = stmt.executeQuery("SELECT currentDatabase()");
                if (rs.next()) {
                    info.put("当前数据库", rs.getString(1));
                }
            }
        }
        return info;
    }

    @Override
    public void copyTable(String database, String oldTableName, String newTableName) throws SQLException {
        try (Connection conn = jdbc.getDataSource().getConnection()) {
            // ClickHouse 使用 CREATE TABLE ... AS
            String sql = "CREATE TABLE " + database + "." + newTableName + " AS " + database + "."  + oldTableName;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
    }

    @Override
    public void dropTable(String database, String tableName) throws SQLException {
        try (Connection conn = jdbc.getDataSource().getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                // 删除表
                String sql = "DROP TABLE " + database + "." + tableName;
                stmt.execute(sql);
            }
        }
    }
}
