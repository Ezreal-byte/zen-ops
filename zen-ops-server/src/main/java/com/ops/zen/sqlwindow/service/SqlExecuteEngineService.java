package com.ops.zen.sqlwindow.service;

import com.ops.zen.en.DsConnTypeEn;
import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.entity.ZenDbExecLog;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.service.DsDataSourceService;
import com.ops.zen.service.SqlExecuteLogService;
import com.ops.zen.sqlwindow.executor.SqlExecutor;
import com.ops.zen.sqlwindow.executor.SqlExecutorFactory;
import com.ops.zen.sqlwindow.util.SqlParseUtil;
import com.ops.zen.sqlwindow.vo.ColumnMetaVo;
import com.ops.zen.sqlwindow.vo.ColumnVo;
import com.ops.zen.sqlwindow.vo.DatabaseVo;
import com.ops.zen.sqlwindow.vo.SqlExecuteResultVo;
import com.ops.zen.sqlwindow.vo.TableVo;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL执行引擎Service
 */
@Service
@Slf4j
public class SqlExecuteEngineService {

    @Autowired
    private DsDataSourceService dsDataSourceService;

    @Autowired
    private SqlExecuteLogService sqlExecuteLogService;

    /**
     * 执行SQL（支持多条SQL，分号分隔）
     */
    public List<SqlExecuteResultVo> executeSql(Long pkDs, String sqlText, Integer pageNum, Integer pageSize, Long userId) {
        List<SqlExecuteResultVo> results = new ArrayList<>();
        if (sqlText == null || sqlText.trim().isEmpty()) {
            return results;
        }

        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        String defaultSchema = getDefaultSchema(ds);

        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);

            List<String> sqlList = splitSql(sqlText);
            for (String singleSql : sqlList) {
                if (singleSql.trim().isEmpty()) {
                    continue;
                }
                SqlExecuteResultVo result = executeSingleSql(executor, pkDs, defaultSchema, singleSql, pageNum, pageSize, userId);
                results.add(result);
            }
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
        return results;
    }

    private String getDefaultSchema(ZenDbDs ds) {
        if (DsConnTypeEn.HOST.equals(ds.getConnType())) {
            return ds.getDbSchema();
        } else if (DsConnTypeEn.URL.equals(ds.getConnType())) {
            String url = ds.getUrl();
            if (url == null || url.isEmpty()) {
                return null;
            }

            // 使用轻量级Executor解析schema（不需要JDBC连接）
            try {
                SqlExecutor executor = SqlExecutorFactory.createLite(ds.getDbType());
                return executor.parseSchemaFromUrl(url);
            } catch (Exception e) {
                log.warn("创建Executor解析schema失败: {}", url, e);
                return null;
            }
        }
        return null;
    }

    /**
     * 测试连接
     */
    public boolean testConnection(Long pkDs) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            return executor.testConnection();
        } catch (SQLException e) {
            log.error("连接测试失败", e);
            return false;
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 获取数据库基本信息
     */
    public Map<String, String> getDatabaseInfo(Long pkDs) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            return executor.getDatabaseInfo();
        } catch (SQLException e) {
            log.error("获取数据库信息失败", e);
            throw new RuntimeException("获取数据库信息失败: " + e.getMessage());
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 复制表
     */
    public void copyTable(Long pkDs, String database, String oldTableName, String newTableName) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            executor.copyTable(database, oldTableName, newTableName);
        } catch (SQLException e) {
            log.error("复制表失败", e);
            throw new RuntimeException("复制表失败: " + e.getMessage());
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 删除表
     */
    public void dropTable(Long pkDs, String database, String tableName) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            executor.dropTable(database, tableName);
        } catch (SQLException e) {
            log.error("删除表失败", e);
            throw new RuntimeException("删除表失败: " + e.getMessage());
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 获取数据库列表
     */
    public List<DatabaseVo> listDatabases(Long pkDs) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            return executor.listDatabases();
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库列表失败: " + e.getMessage(), e);
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 获取表列表
     */
    public List<TableVo> listTables(Long pkDs, String database) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            return executor.listTables(database);
        } catch (SQLException e) {
            throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 获取字段列表
     */
    public List<ColumnVo> listColumns(Long pkDs, String database, String table) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            return executor.listColumns(database, table);
        } catch (SQLException e) {
            throw new RuntimeException("获取字段列表失败: " + e.getMessage(), e);
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 执行单条SQL
     */
    private SqlExecuteResultVo executeSingleSql(SqlExecutor executor, Long pkDs, String dbSchema, String sql,
                                                Integer pageNum, Integer pageSize, Long userId) {
        long startTime = System.currentTimeMillis();
        String sqlType = SqlParseUtil.parseSqlType(sql);
        String dmlType = SqlParseUtil.parseDmlType(sql);
        SqlExecuteResultVo result = new SqlExecuteResultVo();
        result.setSql(sql);
        result.setSqlType(sqlType);
        result.setDmlType(dmlType);
        result.setComments(SqlParseUtil.extractComments(sql));
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        // 单表查询分析和主键获取
        boolean singleTable = SqlParseUtil.isSingleTableQuery(sql);
        result.setSingleTableQuery(singleTable);
        if (singleTable) {
            String tableName = SqlParseUtil.getSingleTableName(sql);
            result.setQueryTable(tableName);
            if (tableName != null) {
                // 优先使用SQL中指定的schema，否则使用数据源默认schema
                String sqlSchema = SqlParseUtil.getSingleTableSchema(sql);
                String effectiveSchema = sqlSchema != null ? sqlSchema : dbSchema;
                result.setQuerySchema(effectiveSchema);
                if (effectiveSchema != null) {
                    try {
                        String pkColumn = executor.getPkColumn(effectiveSchema, tableName);
                        result.setPkColumn(pkColumn);
                    } catch (Exception e) {
                        log.warn("获取主键失败: {}.{}", effectiveSchema, tableName, e);
                    }
                }
            }
        }

        try {
            if ("SELECT".equals(dmlType)) {
                PageResult<Map<String, Object>> pageResult = executor.executeQuery(sql, pageNum, pageSize);
                result.setSuccess(true);
                result.setMessage("查询成功");
                result.setRows(pageResult.getList());
                result.setTotal(pageResult.getTotalCount());
                if (pageResult.getList() != null && !pageResult.getList().isEmpty()) {
                    result.setColumns(new ArrayList<>(pageResult.getList().get(0).keySet()));
                } else if (pageResult.getColumns() != null && !pageResult.getColumns().isEmpty()) {
                    result.setColumns(pageResult.getColumns());
                }
                if (pageResult.getColumnsMeta() != null) {
                    result.setColumnsMeta((List) pageResult.getColumnsMeta());
                }
            } else {
                int affectedRows = executor.executeUpdate(sql);
                result.setSuccess(true);
                result.setMessage("执行成功");
                result.setAffectedRows(affectedRows);
            }
        } catch (Exception e) {
            log.error("SQL执行失败: {}", sql, e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }

        long execTime = System.currentTimeMillis() - startTime;
        result.setExecTimeMs(execTime);

        // 保存审计日志
        saveLog(pkDs, dbSchema, sql, sqlType, result, userId);

        return result;
    }

    /**
     * 分割SQL（按分号分隔，忽略引号内的分号，同时跳过注释）
     */
    public List<String> splitSql(String sql) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
                current.append(c);
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
                current.append(c);
            } else if (c == '\'') {
                inSingleQuote = true;
                current.append(c);
            } else if (c == '"') {
                inDoubleQuote = true;
                current.append(c);
            } else if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                // 跳过 -- 行注释，直到换行或字符串末尾
                while (i < sql.length() && sql.charAt(i) != '\n' && sql.charAt(i) != '\r') {
                    i++;
                }
                // 注释后面的换行符也跳过（不追加到 current）
                if (i < sql.length() && sql.charAt(i) == '\r' && i + 1 < sql.length() && sql.charAt(i + 1) == '\n') {
                    i++; // 跳过 \r\n 中的 \n
                }
            } else if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                // 跳过 /* */ 多行注释
                i += 2; // 跳过 /*
                while (i < sql.length() - 1 && !(sql.charAt(i) == '*' && sql.charAt(i + 1) == '/')) {
                    i++;
                }
                i++; // 跳过 /
            } else if (c == ';') {
                String trimmed = current.toString().trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        String trimmed = current.toString().trim();
        if (!trimmed.isEmpty()) {
            result.add(trimmed);
        }
        return result;
    }

    /**
     * 保存审计日志
     */
    private void saveLog(Long pkDs, String dbSchema, String sql, String sqlType, SqlExecuteResultVo result, Long userId) {
        try {
            // 如果是查询，将数据总数作为受影响行数
            Long affectedRows = result.getAffectedRows() != null ? result.getAffectedRows().longValue() : null;
            if ("QUERY".equals(sqlType) && result.getTotal() != null) {
                affectedRows = result.getTotal();
            }

            ZenDbExecLog logEntity = ZenDbExecLog.builder()
                    .pkDs(pkDs)
                    .dbSchema(dbSchema)
                    .sqlText(sql.length() > 4000 ? sql.substring(0, 4000) : sql)
                    .sqlType(sqlType)
                    .dmlType(result.getDmlType())
                    .singleTableQuery(result.getSingleTableQuery())
                    .pkColumn(result.getPkColumn())
                    .queryTable(result.getQueryTable())
                    .querySchema(result.getQuerySchema())
                    .execStatus(result.isSuccess() ? "SUCCESS" : "FAIL")
                    .execTimeMs(result.getExecTimeMs())
                    .errorMsg(result.getMessage() != null && result.getMessage().length() > 2000 ? result.getMessage().substring(0, 2000) : result.getMessage())
                    .affectedRows(affectedRows)
                    .pkCreatedby(userId)
                    .build();
            sqlExecuteLogService.saveLog(logEntity);
        } catch (Exception e) {
            log.error("保存SQL审计日志失败", e);
        }
    }

    /**
     * 表格编辑更新行（使用占位符参数，按元数据类型转换）
     * @param pkDs 数据源主键
     * @param dbSchema schema
     * @param tableName 表名
     * @param pkColumn 主键列名
     * @param changes 变更列表，每项包含 pkValue(主键值) 和 changes(字段名->新值字符串)
     * @param columnsMeta 列元数据
     * @return 影响行数
     */
    public int updateRows(Long pkDs, String dbSchema, String tableName, String pkColumn,
                          List<Map<String, Object>> changes, List<ColumnMetaVo> columnsMeta) {
        if (changes == null || changes.isEmpty()) {
            return 0;
        }
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        int totalAffected = 0;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            // 构建列名到元数据的映射（小写）
            java.util.HashMap<String, ColumnMetaVo> metaMap = new java.util.HashMap<>();
            if (columnsMeta != null) {
                for (ColumnMetaVo meta : columnsMeta) {
                    metaMap.put(meta.getName().toLowerCase(), meta);
                }
            }

            for (Map<String, Object> changeItem : changes) {
                Object pkValue = changeItem.get("pkValue");
                // 兼容：如果 pkValue 为空，尝试从 row 对象按列名大小写不敏感查找
                if (pkValue == null && changeItem.containsKey("row")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> row = (Map<String, Object>) changeItem.get("row");
                    if (row != null) {
                        pkValue = row.get(pkColumn);
                        if (pkValue == null) {
                            for (Map.Entry<String, Object> entry : row.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(pkColumn)) {
                                    pkValue = entry.getValue();
                                    break;
                                }
                            }
                        }
                    }
                }
                if (pkValue == null) {
                    log.warn("跳过更新：主键值获取失败，pkColumn={}", pkColumn);
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, String> fieldChanges = (Map<String, String>) changeItem.get("changes");
                if (fieldChanges == null || fieldChanges.isEmpty()) continue;

                // 构建 UPDATE sql（使用 @key 占位符）
                String fullTableName = dbSchema != null ? dbSchema + "." + tableName : tableName;
                StringBuilder setClause = new StringBuilder();
                EasyParams params = new EasyParams();

                int idx = 0;
                for (Map.Entry<String, String> entry : fieldChanges.entrySet()) {
                    String col = entry.getKey();
                    String val = entry.getValue();
                    if (idx > 0) setClause.append(", ");
                    String paramKey = "p" + idx;
                    setClause.append(col).append(" = @").append(paramKey);
                    ColumnMetaVo meta = metaMap.get(col.toLowerCase());
                    params.put(paramKey, convertValue(val, meta));
                    idx++;
                }

                // 主键参数
                String pkParamKey = "pk";
                String whereClause = pkColumn + " = @" + pkParamKey;
                ColumnMetaVo pkMeta = metaMap.get(pkColumn.toLowerCase());
                params.put(pkParamKey, convertValue(String.valueOf(pkValue), pkMeta));

                String sql = String.format("UPDATE %s SET %s WHERE %s", fullTableName, setClause, whereClause);
                log.info("执行更新SQL: {}", sql);

                int affected = executor.executeUpdate(sql, params);
                totalAffected += affected;
            }
        } catch (SQLException e) {
            throw new RuntimeException("更新失败: " + e.getMessage(), e);
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
        return totalAffected;
    }

    /**
     * 表格编辑插入新行（使用占位符参数，按元数据类型转换）
     * @param pkDs 数据源主键
     * @param dbSchema schema
     * @param tableName 表名
     * @param rows 行数据列表，每行是字段名->值字符串
     * @param columnsMeta 列元数据
     * @return 影响行数
     */
    public int insertRows(Long pkDs, String dbSchema, String tableName,
                          List<Map<String, String>> rows, List<ColumnMetaVo> columnsMeta) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        int totalAffected = 0;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            // 构建列名到元数据的映射（小写）
            java.util.HashMap<String, ColumnMetaVo> metaMap = new java.util.HashMap<>();
            if (columnsMeta != null) {
                for (ColumnMetaVo meta : columnsMeta) {
                    metaMap.put(meta.getName().toLowerCase(), meta);
                }
            }

            for (Map<String, String> row : rows) {
                if (row == null || row.isEmpty()) continue;

                String fullTableName = dbSchema != null ? dbSchema + "." + tableName : tableName;
                StringBuilder cols = new StringBuilder();
                StringBuilder vals = new StringBuilder();
                EasyParams params = new EasyParams();

                int idx = 0;
                for (Map.Entry<String, String> entry : row.entrySet()) {
                    String col = entry.getKey();
                    String val = entry.getValue();
                    if (idx > 0) {
                        cols.append(", ");
                        vals.append(", ");
                    }
                    cols.append(col);
                    String paramKey = "p" + idx;
                    vals.append("@").append(paramKey);
                    ColumnMetaVo meta = metaMap.get(col.toLowerCase());
                    params.put(paramKey, convertValue(val, meta));
                    idx++;
                }

                String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", fullTableName, cols, vals);
                log.info("执行插入SQL: {}", sql);

                int affected = executor.executeUpdate(sql, params);
                totalAffected += affected;
            }
        } catch (SQLException e) {
            throw new RuntimeException("插入失败: " + e.getMessage(), e);
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
        return totalAffected;
    }

    /**
     * 查询表数据总数
     */
    public long countTableData(Long pkDs, String dbSchema, String tableName) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);
            String fullTableName = dbSchema != null ? dbSchema + "." + tableName : tableName;
            String countSql = "SELECT COUNT(*) AS cnt FROM " + fullTableName;
            PageResult<Map<String, Object>> result = executor.executeQuery(countSql, 1, 1);
            if (result.getList() != null && !result.getList().isEmpty()) {
                Object cnt = result.getList().get(0).get("cnt");
                if (cnt != null) {
                    return Long.parseLong(cnt.toString());
                }
            }
            return 0L;
        } catch (Exception e) {
            throw new RuntimeException("查询表数据总数失败: " + e.getMessage(), e);
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 导出表数据为Excel（分片查询，避免OOM）
     */
    public void exportTableData(Long pkDs, String dbSchema, String tableName, java.io.OutputStream outputStream) {
        String fullTableName = dbSchema != null ? dbSchema + "." + tableName : tableName;
        String selectSql = "SELECT * FROM " + fullTableName;
        String countSql = "SELECT COUNT(*) AS cnt FROM " + fullTableName;
        exportBySql(pkDs, selectSql, countSql, tableName, outputStream);
    }

    /**
     * 导出SQL查询结果为Excel（分片查询，避免OOM）
     */
    public void exportQueryData(Long pkDs, String dbSchema, String sql, java.io.OutputStream outputStream) {
        String wrappedSql = "SELECT * FROM (" + sql + ") _export_subq";
        String countSql = "SELECT COUNT(*) AS cnt FROM (" + sql + ") _export_subq";
        String sheetName = "查询结果";
        exportBySql(pkDs, wrappedSql, countSql, sheetName, outputStream);
    }

    /**
     * 通用：按SQL分片查询并导出为Excel
     */
    private void exportBySql(Long pkDs, String selectSql, String countSql, String sheetName, java.io.OutputStream outputStream) {
        ZenDbDs ds = dsDataSourceService.get(pkDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        int pageSize = 5000;
        SqlExecutor executor = null;
        try {
            executor = SqlExecutorFactory.create(ds.getDbType(), pkDs);

            // 查总数
            PageResult<Map<String, Object>> countResult = executor.executeQuery(countSql, 1, 1);
            long total = 0L;
            if (countResult.getList() != null && !countResult.getList().isEmpty()) {
                Object cnt = countResult.getList().get(0).get("cnt");
                if (cnt != null) {
                    total = Long.parseLong(cnt.toString());
                }
            }
            if (total == 0) {
                throw new RuntimeException("查询结果为空，无需导出");
            }

            // 构建 XSheet
            com.ops.zen.utils.xlsx.XSheet xSheet = new com.ops.zen.utils.xlsx.XSheet();
            xSheet.setName(sheetName);

            // 分片查询
            long fetched = 0;
            boolean headerWritten = false;
            for (int pageNum = 1; fetched < total; pageNum++) {
                PageResult<Map<String, Object>> pageResult = executor.executeQuery(selectSql, pageNum, pageSize);
                List<Map<String, Object>> rows = pageResult.getList();
                if (rows == null || rows.isEmpty()) break;

                // 第一片时写表头
                if (!headerWritten) {
                    Map<String, Object> firstRow = rows.get(0);
                    for (String col : firstRow.keySet()) {
                        xSheet.addHeader(col, col);
                    }
                    headerWritten = true;
                }

                // 写数据行（格式化日期和数字为字符串）
                for (Map<String, Object> row : rows) {
                    Map<String, Object> formattedRow = new java.util.LinkedHashMap<>();
                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        formattedRow.put(entry.getKey(), formatExportValue(entry.getValue()));
                    }
                    xSheet.addRow(formattedRow);
                }

                fetched += rows.size();
                log.info("导出 {} 进度: {}/{}", sheetName, fetched, total);
            }

            // 写入Excel
            com.ops.zen.utils.xlsx.XlsxUtils.writeExcelByXSheet(xSheet, outputStream);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("导出数据失败: " + e.getMessage(), e);
        } finally {
            if (executor != null) {
                executor.close();
            }
        }
    }

    /**
     * 格式化导出值：日期类型统一格式化为 yyyy-MM-dd HH:mm:ss，数字类型转字符串避免精度丢失
     */
    private Object formatExportValue(Object value) {
        if (value == null) {
            return null;
        }
        // 日期时间类型统一格式化
        if (value instanceof java.sql.Timestamp) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((java.sql.Timestamp) value);
        }
        if (value instanceof java.sql.Date) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(((java.sql.Date) value).getTime()));
        }
        if (value instanceof java.sql.Time) {
            return new java.text.SimpleDateFormat("HH:mm:ss").format((java.sql.Time) value);
        }
        if (value instanceof java.util.Date) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((java.util.Date) value);
        }
        if (value instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) value).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (value instanceof java.time.LocalDate) {
            return ((java.time.LocalDate) value).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
        }
        if (value instanceof java.time.LocalTime) {
            return ((java.time.LocalTime) value).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        // 数字类型转字符串，避免前端精度丢失
        if (value instanceof Number) {
            return value.toString();
        }
        return value;
    }

    /**
     * 根据列元数据将字符串转换为正确的Java类型
     */
    private Object convertValue(String strValue, ColumnMetaVo meta) {
        if (strValue == null || strValue.trim().isEmpty()) {
            return null;
        }
        if (meta == null || meta.getSqlType() == null) {
            return strValue;
        }
        int sqlType = meta.getSqlType();
        try {
            switch (sqlType) {
                case Types.BIGINT:
                    return Long.valueOf(strValue);
                case Types.INTEGER:
                    return Integer.valueOf(strValue);
                case Types.SMALLINT:
                    return Short.valueOf(strValue);
                case Types.TINYINT:
                    return Byte.valueOf(strValue);
                case Types.NUMERIC:
                case Types.DECIMAL:
                    return new BigDecimal(strValue);
                case Types.FLOAT:
                    return Float.valueOf(strValue);
                case Types.DOUBLE:
                    return Double.valueOf(strValue);
                case Types.TIMESTAMP:
                    return Timestamp.valueOf(strValue);
                case Types.DATE:
                    return Date.valueOf(strValue);
                case Types.TIME:
                    return Time.valueOf(strValue);
                case Types.BOOLEAN:
                    return Boolean.valueOf(strValue);
                default:
                    return strValue;
            }
        } catch (Exception e) {
            log.warn("类型转换失败: value={}, sqlType={}, 使用原始字符串", strValue, sqlType);
            return strValue;
        }
    }

    /**
     * 记录接口操作审计日志（新建数据库、查看表结构、导出数据、复制表、删除表等）
     * 
     * @param pkDs 数据源主键
     * @param dbSchema 数据库/schema名称
     * @param operationType 操作类型描述（如：新建数据库、查看表结构、导出数据、复制表、删除表）
     * @param operationDetail 操作详细信息（如：表名、数据库名等）
     * @param userId 用户ID
     */
    public void recordApiOperation(Long pkDs, String dbSchema, String operationType, String operationDetail, Long userId) {
        try {
            // 构建SQL内容描述：操作类型 + 详细信息
            String sqlContent = operationType + ": " + operationDetail;
            
            ZenDbExecLog logEntity = ZenDbExecLog.builder()
                    .pkDs(pkDs)
                    .dbSchema(dbSchema)
                    .sqlText(sqlContent.length() > 4000 ? sqlContent.substring(0, 4000) : sqlContent)
                    .sqlType("接口操作")  // 固定类型
                    .dmlType(null)  // 接口操作无DML类型
                    .singleTableQuery(null)  // 可为空
                    .pkColumn(null)  // 可为空
                    .queryTable(null)  // 可为空
                    .querySchema(null)  // 可为空
                    .execStatus("SUCCESS")  // 能调用到说明成功
                    .execTimeMs(0L)  // 接口操作不记录执行时间
                    .errorMsg(null)  // 可为空
                    .affectedRows(null)  // 可为空
                    .pkCreatedby(userId)
                    .build();
            sqlExecuteLogService.saveLog(logEntity);
        } catch (Exception e) {
            log.error("保存接口操作审计日志失败: operationType={}", operationType, e);
        }
    }
}
