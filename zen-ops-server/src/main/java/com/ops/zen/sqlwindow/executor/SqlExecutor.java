package com.ops.zen.sqlwindow.executor;

import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.sqlwindow.vo.ColumnVo;
import com.ops.zen.sqlwindow.vo.DatabaseVo;
import com.ops.zen.sqlwindow.vo.TableVo;
import com.ops.zen.utils.map.PageResult;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * SQL执行器标准接口
 */
public interface SqlExecutor {

    /**
     * 测试连接
     */
    boolean testConnection() throws SQLException;

    /**
     * 获取数据库/Schema列表
     */
    List<DatabaseVo> listDatabases() throws SQLException;

    /**
     * 获取指定数据库的表列表
     */
    List<TableVo> listTables(String database) throws SQLException;

    /**
     * 获取指定表的字段列表
     */
    List<ColumnVo> listColumns(String database, String table) throws SQLException;

    /**
     * 执行查询SQL（分页）
     */
    PageResult<Map<String, Object>> executeQuery(String sql, int pageNum, int pageSize) throws SQLException;

    /**
     * 执行更新/DDL/DML SQL
     * @return 影响行数
     */
    int executeUpdate(String sql) throws SQLException;

    /**
     * 执行带参数的更新/DDL/DML SQL
     * @return 影响行数
     */
    int executeUpdate(String sql, EasyParams params) throws SQLException;

    /**
     * 获取指定表的主键列名
     */
    String getPkColumn(String database, String table) throws SQLException;

    /**
     * 关闭资源
     */
    void close();

    /**
     * 从JDBC URL中解析默认schema（本地解析，不需要建立连接）
     * @param url JDBC URL
     * @return schema名称，解析失败返回null
     */
    String parseSchemaFromUrl(String url);

    /**
     * 获取数据库基本信息（如版本、会话数等）
     * @return KV格式的数据库信息
     */
    Map<String, String> getDatabaseInfo() throws SQLException;

    /**
     * 复制表（包括表结构和数据）
     * @param database 数据库名
     * @param oldTableName 原表名
     * @param newTableName 新表名
     */
    void copyTable(String database, String oldTableName, String newTableName) throws SQLException;

    /**
     * 删除表
     * @param database 数据库名
     * @param tableName 表名
     */
    void dropTable(String database, String tableName) throws SQLException;
}
