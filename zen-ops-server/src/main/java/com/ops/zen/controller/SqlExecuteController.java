package com.ops.zen.controller;

import com.google.common.base.Stopwatch;
import com.ops.zen.cache.Pair;
import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.fs.FileService;
import com.ops.zen.fs.FsFactory;
import com.ops.zen.fs.TempFile;
import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.jdbc.EntityHelper;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.jdbc.sql.EasyVars;
import com.ops.zen.jdbc.sql.SQL;
import com.ops.zen.phy.PhyTableTool;
import com.ops.zen.phy.meta.EtlFieldMeta;
import com.ops.zen.phy.meta.EtlFieldMetaHelper;
import com.ops.zen.service.DsDataSourceService;
import com.ops.zen.sql.FFSQLHelper;
import com.ops.zen.sql.parser.SQLKeywordEn;
import com.ops.zen.sql.parser.SinglePatchSql;
import com.ops.zen.sql.parser.SqlPatchInstallParser;
import com.ops.zen.sql.vo.*;
import com.ops.zen.utils.*;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xyn
 * @date 2025/5/9 14:23
 * @description
 **/
@Deprecated
@RestController
@RequestMapping("/sql/execute")
@Slf4j
public class SqlExecuteController {


    private static final int pageSize = 20;
    private FileService fileService = FsFactory.tempFileService();

    private static List<FFSqlConnectionVO> SQL_RESULT_WINDOW_CONNS = new LinkedList<>();


    @Autowired
    private DsDataSourceService dsDataSourceService;

    @GetMapping("dataSources")
    public List<ZenDbDs> dataSources() {
        return dsDataSourceService.queryByPage("", 1, 200).getList();
    }

    /**
     * 执行sql
     * @param pkDs
     * @param sql   SQL片段  可能是多个SQL
     * @return
     */
    @PostMapping("/execSql")
    public List<FFSqlExecResult> execute(@RequestParam String pkDs,
                                         @RequestBody String sql) throws Exception {

        //1.获取jdbc
        Jdbc jdbc = Jdbcs.getJdbc(pkDs);
        //2. 方言工具
        PhyTableTool tool = FFSQLHelper.phyTableTool(pkDs);
        //3. 解析SQL
        List<SinglePatchSql> singlePatchSqls = SqlPatchInstallParser.parse(sql, "", "");
        //3. SQL里面如果没有update delete insert语句  直接提交   如果存在以上语句  按顺序执行 不提交
        boolean autoCommit = singlePatchSqls.stream().noneMatch(r -> Arrays.asList(SQLKeywordEn.DML_UPDATE, SQLKeywordEn.DML_INSERT, SQLKeywordEn.DML_DELETE).contains(r.getKeyword()));
        Connection connection = null;
        //存在非select sql的时候   才获取connection
        if (singlePatchSqls.stream().anyMatch(r -> !r.getKeyword().equalsIgnoreCase("select"))) {
            connection = Jdbcs.getConnectionManualCommit(jdbc);
            connection.setAutoCommit(autoCommit);
        }
        String connectionId = UUIDUtils.randomUUID();
        //在一个事务里执行
        List<FFSqlExecResult> resultList = new ArrayList<>();
        for (SinglePatchSql single : singlePatchSqls) {
            String keyword = single.getKeyword();
            if (Objects.equals(keyword, SQLKeywordEn.COMMENT_ONE) || Objects.equals(keyword, SQLKeywordEn.COMMENT_MULTI)) {
                continue;
            }
            boolean isSelect = keyword.equalsIgnoreCase(SQLKeywordEn.DML_SELECT);
            FFSqlExecResult result = new FFSqlExecResult();
            // 如果是查询  返回查询结果  如果是更新 DDL等  返回行数
            Stopwatch stopwatch = Stopwatch.createStarted();
            String unitSql = single.getUnitSql();
            if (isSelect) {
                // 1. 查询数据
                PageResult<EasyRecord> pageResult = jdbc.queryPage(unitSql, null, null, 1, pageSize);
                FFSQLHelper.coverDateValue(pageResult.getList());
                // 2. 获取元数据 ?
                SQL compile = jdbc.compile(unitSql, new EasyParams(), new EasyVars());
                List<EtlFieldMeta> fieldMetas = EtlFieldMetaHelper.fieldsMeta(jdbc.getDataSource(), compile, true);
                result.setFieldMetas(fieldMetas);
                result.setResult(JsonUtils.toJsonStringAvoidLongOverflow(JsonUtils.toJSONString(pageResult)));
            } else {
                //TODO 是否需要判断只有在UPDATE INSERT DELETE 时  使用事务   现在是只要有以上三项  除了SELECT都非自动提交
                //除了查询的SQL  使用原生的 PreparedStatement 执行
                try (PreparedStatement ps = connection.prepareStatement(unitSql)) {
                    int i = ps.executeUpdate();
                    result.setResult(String.valueOf(i));
                    if (!autoCommit) {
                        result.setConnectionId(connectionId);
                        SQL_RESULT_WINDOW_CONNS.add(FFSQLHelper.generateConnectionVO(connectionId, pkDs, keyword, i, connection));
                    }
                }
            }
            result.setSql(unitSql);
            result.setExecType(keyword);
            result.setElapsedTime(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
            //是否是单表并且包含主键  表名  主键名  主键类型
            Pair<Boolean, String> pair = FFSQLHelper.parseSqlSingleTableName(unitSql);
            boolean singletonTable = pair.getKey();
            result.setSingletonTable(singletonTable);
            if (singletonTable) {
                String tableName = pair.getValue();
                result.setTableName(tableName);
                if (Objects.nonNull(tool)) {
                    Pair<String, String> primaryColumNameType = tool.getPrimaryColumNameType(jdbc, tableName);
                    result.setPkName(primaryColumNameType.getKey());
                    result.setPkColType(primaryColumNameType.getValue());
                }
            }
            resultList.add(result);
        }
        if (autoCommit && Objects.nonNull(connection)) {
            IOUtils.close(connection);
        }
        return resultList;

    }


    /**
     * 获取下一页数据
     * @param pkDs
     * @param pageNum
     * @param sql
     * @return
     * @throws SQLException
     */
    @RequestMapping(value = "nextPageData", method = RequestMethod.POST)
    @ResponseBody
    public String nextPageData(@RequestParam String pkDs, @RequestParam int pageNum,
                               @RequestBody String sql) throws SQLException {
        //1.获取jdbc
        Jdbc jdbc = Jdbcs.getJdbc(pkDs);
        //2. 查询
        List<EasyRecord> list = jdbc.query(sql, pageNum, pageSize);
        FFSQLHelper.coverDateValue(list);
        //将返回值用map封装  否则toJsonStringAvoidLongOverflow 无法处理
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        return JsonUtils.toJsonStringAvoidLongOverflow(JsonUtils.toJSONString(map));
    }

    /**
     * 删除数据
     *
     * */
    @RequestMapping(value = "deleteRow", method = RequestMethod.GET)
    @ResponseBody
    public FFSqlDMLResult deleteRow(@RequestParam String pkDs,
                                    @RequestParam String tableName,
                                    @RequestParam String pkName,
                                    @RequestParam String pkVal,
                                    @RequestParam String pkColType) throws SQLException {

        FFSqlDMLResult rt = new FFSqlDMLResult();
        //1.获取jdbc
        Jdbc jdbc = Jdbcs.getJdbc(pkDs);
        Connection connection = Jdbcs.getConnectionManualCommit(jdbc);
        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, pkName);
        List<Object> paramsList = new ArrayList<>();
        paramsList.add(FFSQLHelper.coverCellValue(pkVal, pkColType));//@pkVal
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            EntityHelper.preparedStatementSet(ps, paramsList);
            int exeRt = ps.executeUpdate();
            rt.setUpdateRows(exeRt);
        }
        String connectionId = UUIDUtils.randomUUID();
        SQL_RESULT_WINDOW_CONNS.add(FFSQLHelper.generateConnectionVO(connectionId, pkDs, "DELETE", rt.getUpdateRows(), connection));
        rt.setConnectionId(connectionId);
        return rt;
    }


    /**
     * 更新数据
     * @param vo
     * @return
     *
     * 需要事务
     */
    @RequestMapping(value = "updateRows", method = RequestMethod.POST)
    @ResponseBody
    public FFSqlDMLResult updateRows(@RequestBody FFSqlUpdateVO vo) throws SQLException {
//        Jdbc jdbc = FFSQLHelper.getJdbc();
        FFSqlDMLResult rt = new FFSqlDMLResult();
        Jdbc jdbc = Jdbcs.getJdbc(vo.getPkDs());
        Connection connection = Jdbcs.getConnectionManualCommit(jdbc);

        String tableName = vo.getTableName();
        String pkName = vo.getPkName();
        String pkColType = vo.getPkColType();
        List<EtlFieldMeta> fieldMetas = vo.getFieldMetas();
        // 根据原数据生成SQL
        List<String> setList = new ArrayList<>();
        //key: 字段名 小写  value: 数据类型
        for (EtlFieldMeta fieldMeta : fieldMetas) {
            String colName = fieldMeta.getName().toLowerCase();
            setList.add(colName + " = ?");
        }
        String sqlStr = String.format(
                "update %s set %s where %s = ?",
                tableName, StringUtils.concate(setList, ", "), pkName
        );
        Map<String, EasyRecord> oldRows = vo.getOldRows();
        for (Map.Entry<String, EasyRecord> entry : vo.getNewRows().entrySet()) {
            String index = entry.getKey();
            EasyRecord newData = entry.getValue();//新数据
            List<Object> paramsList = FFSQLHelper.toListParams(newData, fieldMetas);
            EasyRecord oldRecord = oldRows.get(index);
            Object value = FFSQLHelper.coverCellValue(oldRecord.getString(pkName), pkColType);//最后一个参数 新主键
            paramsList.add(value);
            try (PreparedStatement ps = connection.prepareStatement(sqlStr)) {
                EntityHelper.preparedStatementSet(ps, paramsList);
                int exeRt = ps.executeUpdate();
                rt.plusUpdateRows(exeRt);
            }
        }
        String connectionId = UUIDUtils.randomUUID();
        SQL_RESULT_WINDOW_CONNS.add(FFSQLHelper.generateConnectionVO(connectionId, vo.getPkDs(), "UPDATE", rt.getUpdateRows(), connection));
        rt.setConnectionId(connectionId);
        return rt;
    }

    /**
     * 新增数据
     * @param vo
     * @return
     *
     * 需要事务
     */
    @RequestMapping(value = "addRows", method = RequestMethod.POST)
    @ResponseBody
    public FFSqlDMLResult addRows(@RequestBody FFSqlAddVO vo) throws SQLException {
        FFSqlDMLResult rt = new FFSqlDMLResult();

        Jdbc jdbc = Jdbcs.getJdbc(vo.getPkDs());
        Connection connection = Jdbcs.getConnectionManualCommit(jdbc);
        String tableName = vo.getTableName();
        List<EtlFieldMeta> fieldMetas = vo.getFieldMetas();

        //key: 字段名 小写  value: 数据类型
        List<String> fieldNameList = new ArrayList<>();
        List<String> paramNameList = new ArrayList<>();

        for (EtlFieldMeta fieldMeta : fieldMetas) {
            String colName = fieldMeta.getName().toLowerCase();
            fieldNameList.add(colName);
            paramNameList.add("? ");
        }
        String sqlStr = String.format("INSERT INTO %s( %s ) VALUES( %s )", tableName,
                StringUtils.concate(fieldNameList, ", "),
                StringUtils.concate(paramNameList, ", ")
        );
        for (EasyRecord record : vo.getList()) {
            List<Object> paramsList = FFSQLHelper.toListParams(record, fieldMetas);
            try (PreparedStatement ps = connection.prepareStatement(sqlStr)) {
                EntityHelper.preparedStatementSet(ps, paramsList);
                int exeRt = ps.executeUpdate();
                rt.plusUpdateRows(exeRt);
            }
        }
        String connectionId = UUIDUtils.randomUUID();
        SQL_RESULT_WINDOW_CONNS.add(FFSQLHelper.generateConnectionVO(connectionId, vo.getPkDs(), "ADD", rt.getUpdateRows(), connection));
        rt.setConnectionId(connectionId);
        return rt;
    }


    /**
     * 获取下载BLOB的文件ID
     * @param base64
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "getDownloadId", method = RequestMethod.POST)
    @ResponseBody
    public String getDownloadId(@RequestBody String base64) throws IOException {
        byte[] binary = new byte[0];
        try {
            binary = Base64Utils.decode(base64);
        } catch (Exception e) {
            throw new RuntimeException("base64转字节数组失败, 可能不是一个标准的base64字符串", e);
        }
        String fileName = DateTimeUtils.currentYYYYMMDDHHMMSS() + "." + FFSQLHelper.getFileTypeByBinary(binary);
        TempFile file = new TempFile();
        file.setName(fileName);
        file.setBody(new ByteArrayInputStream(binary));
        return fileService.write(file);
    }


    /**
     * 提交或回滚事务
     * @param key
     * @param commit
     * @return
     * @throws SQLException
     */
    @RequestMapping(value = "commitOrRollback", method = RequestMethod.GET)
    @ResponseBody
    public String commitOrRollback(@RequestParam String key, @RequestParam boolean commit/*true提交，false回滚*/) throws SQLException {
        List<FFSqlConnectionVO> list = SQL_RESULT_WINDOW_CONNS.stream().filter(r -> Objects.equals(key, r.getConnectionId())).collect(Collectors.toList());
        Assert.notEmpty(list, String.format("%s关联的连接不存在，请检查", key));
        Connection connection = list.get(0).getConnection();
        try {
            if (connection == null) {
                throw new RuntimeException(String.format("%s关联的连接不存在，请检查", key));
            } else {
                if (commit) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            }
        } finally {
            IOUtils.close(connection);
            SQL_RESULT_WINDOW_CONNS.removeIf(r -> Objects.equals(key, r.getConnectionId()));
        }
        return null;
    }

    /**
     * 当前未提交未回滚的事务列表
     * @return
     */
    @RequestMapping(value = "transactionList", method = RequestMethod.GET)
    @ResponseBody
    public List<FFSqlConnectionVO> transactionList() {
        return SQL_RESULT_WINDOW_CONNS;
    }
}
