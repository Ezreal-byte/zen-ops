package com.ops.zen.phy;


import com.ops.zen.cache.Pair;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.phy.vo.EtlFieldTpEn;
import com.ops.zen.phy.vo.EtlTableMeta;
import com.ops.zen.phy.vo.IndexSqlVO;
import com.ops.zen.phy.vo.TableVO;

import java.util.List;

/**
 * 表工具
 *
 * @Author xyn
 * @Description
 */
public interface PhyTableTool {

    /**
     * 建表
     *
     * @param jdbc
     * @param meta
     * @param dbName
     */
    void create(Jdbc jdbc, EtlTableMeta meta, String dbName);

    /**
     * 删除 并且 创建索引
     * @param jdbc
     * @param meta
     * @return
     */
    List<IndexSqlVO> dropAndCreateIndex(Jdbc jdbc, EtlTableMeta meta);

    /**
     * 创建或删除索引  创建还是删除由vo.getType()确定
     * 不会使用vo里的SQL直接执行  根据name seq, tableIndex重新生成
     * @param jdbc
     * @param vo
     * @return
     */
    IndexSqlVO dropOrCreateIndex(Jdbc jdbc, IndexSqlVO vo);

    /**
     * 创建索引，可能执行多条索引创建语句
     *
     * @param jdbc
     * @param meta
     * @return 失败信息列表
     */
    List<String> createIndex(Jdbc jdbc, EtlTableMeta meta);

    int drop(Jdbc jdbc, EtlTableMeta meta, String dbName);

    boolean hasTable(Jdbc jdbc, EtlTableMeta meta, String dbName);

    /**
     * 建表语句，多条DDL，建表，注释，主键创建等
     *
     * @param meta
     * @param dbName
     * @return
     */
    List<String> buildCreateSqls(EtlTableMeta meta, String dbName);

    /**
     * 创建或删除表时是否需要数据库名做限定，已知clickhouse需要数据库名做限定
     *
     * @param dbName
     * @return
     */
    default String qualifyDDLDbName(String dbName) {
        return "";
    }

    /**
     * 创建索引语句
     *
     * @param meta
     * @return
     */
    List<String> buildCreateIndexSqls(EtlTableMeta meta);


    /**
     * 创建索引语句  其中包含drop语句
     *
     * @param meta
     * @return
     */
    List<IndexSqlVO> buildDropAndCreateIndexSqls(EtlTableMeta meta);

    /**
     * 数据库表名元数据查询语句：
     * "select table_name value, table_name label from user_tables WHERE table_name like @keyword"
     *
     * @param dbName
     * @return
     */
    String getSelectTableNameSql(String dbName);

    /**
     * 根据表名逆向生成元数据
     *
     * @param jdbc
     * @param dbName
     * @param tableName
     * @param des
     * @return
     */
    EtlTableMeta getMetaByDsTableName(Jdbc jdbc, String dbName, String tableName, String des);

    /**
     * 获取主键名称 和 对应对的数据类型
     * @param jdbc
     * @param tableName
     * @return <p>key: 主键字段名  value: 主键数据类型: {@link EtlFieldTpEn}</p>
     */
    Pair<String, String> getPrimaryColumNameType(Jdbc jdbc, String tableName);

    List<TableVO> userTables(Jdbc jdbc);
}
