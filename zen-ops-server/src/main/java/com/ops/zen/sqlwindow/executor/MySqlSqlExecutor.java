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
 * MySQL SQL执行器
 */
@Slf4j
public class MySqlSqlExecutor extends AbstractSqlExecutor {

    public MySqlSqlExecutor(Jdbc jdbc) {
        super(jdbc);
    }

    @Override
    public List<DatabaseVo> listDatabases() throws SQLException {
        String sql = "SELECT SCHEMA_NAME AS DATABASE_NAME FROM INFORMATION_SCHEMA.SCHEMATA ORDER BY SCHEMA_NAME";
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
        String sql = "SELECT TABLE_NAME, TABLE_COMMENT AS COMMENTS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = @schema AND TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("schema", database), null);
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
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT AS COMMENTS, COLUMN_DEFAULT, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = @tableName ORDER BY ORDINAL_POSITION";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("schema", database).put("tableName", table), null);
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
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = @schema AND TABLE_NAME = @tableName AND CONSTRAINT_NAME = 'PRIMARY' LIMIT 1";
        List<EasyRecord> records = jdbc.query(sql, new EasyParams().put("schema", database).put("tableName", table), null);
        if (records != null && !records.isEmpty()) {
            return records.get(0).getString("column_name");
        }
        return null;
    }

    @Override
    public String parseSchemaFromUrl(String url) {
        // MySQL: jdbc:mysql://host:port/schema?params
        try {
            String prefix = "jdbc:mysql://";
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
            log.warn("MySQL解析URL schema失败: {}", url, e);
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
                ResultSet rs = stmt.executeQuery("SELECT DATABASE()");
                if (rs.next()) {
                    info.put("当前数据库", rs.getString(1));
                }
            }
                
            // 查询连接数
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'");
                if (rs.next()) {
                    info.put("当前会话数", rs.getString(2));
                }
            }
                
            // 查询最大连接数
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'max_connections'");
                if (rs.next()) {
                    info.put("最大连接数", rs.getString(2));
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
                // 切换到指定数据库
                stmt.execute("USE `" + database + "`");
                
                // 复制表结构
                String createSql = "CREATE TABLE `" + newTableName + "` LIKE `" + oldTableName + "`";
                stmt.execute(createSql);
                
                // 复制数据
                String insertSql = "INSERT INTO `" + newTableName + "` SELECT * FROM `" + oldTableName + "`";
                stmt.execute(insertSql);
                
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
                // 切换到指定数据库
                stmt.execute("USE `" + database + "`");
                
                // 删除表
                String dropSql = "DROP TABLE `" + tableName + "`";
                stmt.execute(dropSql);
            }
        }
    }
}
