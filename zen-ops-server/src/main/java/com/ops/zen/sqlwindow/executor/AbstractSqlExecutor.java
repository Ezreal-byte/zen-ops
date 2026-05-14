package com.ops.zen.sqlwindow.executor;

import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.sqlwindow.vo.ColumnMetaVo;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL执行器抽象基类
 */
@Slf4j
public abstract class AbstractSqlExecutor implements SqlExecutor {

    protected Jdbc jdbc;

    public AbstractSqlExecutor(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean testConnection() throws SQLException {
        try {
            jdbc.getDataSource().getConnection().close();
            return true;
        } catch (SQLException e) {
            log.error("连接测试失败", e);
            return false;
        }
    }

    @Override
    public PageResult<Map<String, Object>> executeQuery(String sql, int pageNum, int pageSize) throws SQLException {
        PageResult<EasyRecord> pageResult = jdbc.queryPage(sql, null, null, pageNum, pageSize);
        PageResult<Map<String, Object>> result = new PageResult<>();
        result.setPageNum(pageResult.getPageNum());
        result.setPageSize(pageResult.getPageSize());
        result.setTotalCount(pageResult.getTotalCount());

        // 从 ResultSetMetaData 获取列名顺序和元数据
        List<String> columnOrder = getColumnOrderFromMetaData(sql);
        List<ColumnMetaVo> columnsMeta = getColumnsMeta(sql);

        List<Map<String, Object>> rows = new ArrayList<>();
        if (pageResult.getList() != null) {
            for (EasyRecord record : pageResult.getList()) {
                Map<String, Object> map = new LinkedHashMap<>();
                if (columnOrder != null && !columnOrder.isEmpty()) {
                    for (String col : columnOrder) {
                        Object value = record.get(col);
                        // 数字类型转为字符串，避免前端长数字精度丢失/溢出
                        if (value instanceof Number) {
                            value = value.toString();
                        }
                        map.put(col, value);
                    }
                } else {
                    // 降级：按 record 原有顺序
                    for (Map.Entry<String, Object> entry : record.entrySet()) {
                        Object value = entry.getValue();
                        if (value instanceof Number) {
                            value = value.toString();
                        }
                        map.put(entry.getKey(), value);
                    }
                }
                rows.add(map);
            }
        }
        result.setList(rows);
        result.setColumns(columnOrder);
        result.setColumnsMeta(columnsMeta);
        return result;
    }

    /**
     * 通过 PreparedStatement 获取 ResultSetMetaData 的列名顺序
     */
    private List<String> getColumnOrderFromMetaData(String sql) {
        List<String> columns = new ArrayList<>();
        try (Connection conn = jdbc.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSetMetaData metaData = ps.getMetaData();
            if (metaData != null) {
                int count = metaData.getColumnCount();
                for (int i = 1; i <= count; i++) {
                    String label = metaData.getColumnLabel(i);
                    if (label == null || label.isEmpty()) {
                        label = metaData.getColumnName(i);
                    }
                    columns.add(label.toLowerCase());
                }
            }
        } catch (SQLException e) {
            log.warn("从ResultSetMetaData获取列顺序失败: {}", sql, e);
            return null;
        }
        return columns;
    }

    /**
     * 通过 PreparedStatement 获取 ResultSetMetaData 的列元数据
     */
    private List<ColumnMetaVo> getColumnsMeta(String sql) {
        List<ColumnMetaVo> metas = new ArrayList<>();
        try (Connection conn = jdbc.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSetMetaData metaData = ps.getMetaData();
            if (metaData != null) {
                int count = metaData.getColumnCount();
                for (int i = 1; i <= count; i++) {
                    String label = metaData.getColumnLabel(i);
                    if (label == null || label.isEmpty()) {
                        label = metaData.getColumnName(i);
                    }
                    int sqlType = metaData.getColumnType(i);
                    String colType = metaData.getColumnTypeName(i);
                    int precision = metaData.getPrecision(i);
                    int scale = metaData.getScale(i);

                    ColumnMetaVo vo = new ColumnMetaVo();
                    vo.setName(label.toLowerCase());
                    vo.setSqlType(sqlType);
                    vo.setColType(colType);
                    vo.setPrecision(precision);
                    vo.setScale(scale);
                    vo.setType(toSimpleType(sqlType));
                    metas.add(vo);
                }
            }
        } catch (SQLException e) {
            log.warn("从ResultSetMetaData获取列元数据失败: {}", sql, e);
            return null;
        }
        return metas;
    }

    private String toSimpleType(int sqlType) {
        switch (sqlType) {
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME:
                return "DATETIME";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
                return "STRING";
            case Types.CLOB:
            case Types.NCLOB:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                return "CLOB";
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                return "NUMBER";
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "BLOB";
            default:
                return "STRING";
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return jdbc.execute(sql);
    }

    @Override
    public int executeUpdate(String sql, EasyParams params) throws SQLException {
        return jdbc.execute(sql, params, null);
    }

    @Override
    public String getPkColumn(String database, String table) throws SQLException {
        return null;
    }

    @Override
    public void close() {
        // 连接池由外部管理，此处不主动关闭
    }

    /**
     * 从JDBC URL中解析默认schema
     * @param url JDBC URL
     * @return schema名称，解析失败返回null
     */
    public String parseSchemaFromUrl(String url) {
        return null;
    }

    @Override
    public Map<String, String> getDatabaseInfo() throws SQLException {
        // 默认实现，子类可覆盖
        Map<String, String> info = new LinkedHashMap<>();
        try (Connection conn = jdbc.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            info.put("数据库版本", metaData.getDatabaseProductVersion());
            info.put("数据库产品", metaData.getDatabaseProductName());
            info.put("驱动版本", metaData.getDriverVersion());
        }
        return info;
    }

    @Override
    public void copyTable(String database, String oldTableName, String newTableName) throws SQLException {
        throw new UnsupportedOperationException("该数据库不支持复制表操作");
    }

    @Override
    public void dropTable(String database, String tableName) throws SQLException {
        throw new UnsupportedOperationException("该数据库不支持删除表操作");
    }
}
