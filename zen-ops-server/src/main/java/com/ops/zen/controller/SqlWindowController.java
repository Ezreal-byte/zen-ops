package com.ops.zen.controller;

import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.service.DsDataSourceService;
import com.ops.zen.sqlwindow.service.SqlExecuteEngineService;
import com.ops.zen.sqlwindow.vo.*;
import com.ops.zen.utils.UpDownLoader;
import com.ops.zen.utils.UserContext;
import com.ops.zen.utils.map.KvMap;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * SQL窗口执行控制器
 */
@RestController
@RequestMapping("/sql-window")
@Slf4j
public class SqlWindowController {

    @Autowired
    private SqlExecuteEngineService sqlExecuteEngineService;

    @Autowired
    private DsDataSourceService dsDataSourceService;

    /**
     * 获取数据库基本信息
     */
    @GetMapping("/db-info/{pkDs}")
    public Map<String, String> getDatabaseInfo(@PathVariable Long pkDs) {
        return sqlExecuteEngineService.getDatabaseInfo(pkDs);
    }

    /**
     * 复制表
     */
    @PostMapping("/table/copy/{pkDs}")
    public KvMap copyTable(@PathVariable Long pkDs,
                          @RequestParam String database,
                          @RequestParam String oldTable,
                          @RequestParam String newTable) {
        sqlExecuteEngineService.copyTable(pkDs, database, oldTable, newTable);
        
        // 记录审计日志
        Long userId = UserContext.getUserId();
        String detail = String.format("数据库[%s] 表[%s] -> 新表[%s]", database, oldTable, newTable);
        sqlExecuteEngineService.recordApiOperation(pkDs, database, "复制表", detail, userId);
        
        return new KvMap();
    }

    /**
     * 删除表
     */
    @PostMapping("/table/delete/{pkDs}")
    public KvMap deleteTable(@PathVariable Long pkDs,
                            @RequestParam String database,
                            @RequestParam String table) {
        sqlExecuteEngineService.dropTable(pkDs, database, table);
        
        // 记录审计日志
        Long userId = UserContext.getUserId();
        String detail = String.format("数据库[%s] 表[%s]", database, table);
        sqlExecuteEngineService.recordApiOperation(pkDs, database, "删除表", detail, userId);
        
        return new KvMap();
    }

    @PostMapping("test-connection/{pkDs}")
    public KvMap testConnection(@PathVariable Long pkDs) {
        boolean connected = sqlExecuteEngineService.testConnection(pkDs);
        KvMap kv = new KvMap();
        kv.put("connected", connected);
        kv.put("msg", connected ? "连接成功" : "连接失败");
        return kv;
    }

    @PostMapping("execute")
    public List<SqlExecuteResultVo> executeSql(@RequestBody ExecuteSqlRequest request) {
        Long userId = UserContext.getUserId();
        return sqlExecuteEngineService.executeSql(
                request.getPkDs(),
                request.getSqlText(),
                request.getPageNum() != null ? request.getPageNum() : 1,
                request.getPageSize() != null ? request.getPageSize() : 100,
                userId
        );
    }

    @GetMapping("databases/{pkDs}")
    public List<DatabaseVo> listDatabases(@PathVariable Long pkDs) {
        return sqlExecuteEngineService.listDatabases(pkDs);
    }

    @GetMapping("tables/{pkDs}")
    public List<TableVo> listTables(@PathVariable Long pkDs, @RequestParam String database) {
        return sqlExecuteEngineService.listTables(pkDs, database);
    }

    @GetMapping("columns/{pkDs}")
    public List<ColumnVo> listColumns(@PathVariable Long pkDs, @RequestParam String database, @RequestParam String table) {
        // 记录审计日志（查看表结构）
        Long userId = UserContext.getUserId();
        String detail = String.format("数据库[%s] 表[%s]", database, table);
        sqlExecuteEngineService.recordApiOperation(pkDs, database, "查看表结构", detail, userId);
        
        return sqlExecuteEngineService.listColumns(pkDs, database, table);
    }

    @GetMapping("datasource/list")
    public List<ZenDbDs> listAllDataSources() {
        PageResult<ZenDbDs> page = dsDataSourceService.queryByPage(null, 1, 1000);
        return page.getList();
    }

    @PostMapping("update-rows")
    public int updateRows(@RequestBody UpdateRowsRequest request) {
        return sqlExecuteEngineService.updateRows(
                request.getPkDs(),
                request.getDbSchema(),
                request.getTableName(),
                request.getPkColumn(),
                request.getChanges(),
                request.getColumnsMeta()
        );
    }

    @PostMapping("insert-rows")
    public int insertRows(@RequestBody InsertRowsRequest request) {
        return sqlExecuteEngineService.insertRows(
                request.getPkDs(),
                request.getDbSchema(),
                request.getTableName(),
                request.getRows(),
                request.getColumnsMeta()
        );
    }

    @GetMapping("table/count")
    public KvMap countTableData(@RequestParam Long pkDs,
                                @RequestParam String database,
                                @RequestParam String table) {
        long count = sqlExecuteEngineService.countTableData(pkDs, database, table);
        KvMap kv = new KvMap();
        kv.put("count", count);
        return kv;
    }

    @GetMapping(value = "table/export")
    public void exportTableData(@RequestParam Long pkDs,
                                @RequestParam String database,
                                @RequestParam String table,
                                HttpServletResponse response) throws IOException {
        // 记录审计日志（导出数据）
        Long userId = UserContext.getUserId();
        String detail = String.format("数据库[%s] 表[%s]", database, table);
        sqlExecuteEngineService.recordApiOperation(pkDs, database, "导出数据", detail, userId);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sqlExecuteEngineService.exportTableData(pkDs, database, table, baos);
        UpDownLoader.downLoad(table + ".xlsx", new ByteArrayInputStream(baos.toByteArray()), response);
    }

    @GetMapping(value = "query/export")
    public void exportQueryData(@RequestParam Long pkDs,
                                @RequestParam String database,
                                @RequestParam String sql,
                                HttpServletResponse response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sqlExecuteEngineService.exportQueryData(pkDs, database, sql, baos);
        UpDownLoader.downLoad("查询结果.xlsx", new ByteArrayInputStream(baos.toByteArray()), response);
    }
}
