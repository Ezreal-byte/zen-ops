package com.ops.zen.sqlwindow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * SQL执行结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlExecuteResultVo {

    /** 原始SQL */
    private String sql;

    /** SQL类型 QUERY/DDL/DML/UPDATE */
    private String sqlType;

    /** DML子类型 INSERT/UPDATE/DELETE */
    private String dmlType;

    /** 是否成功 */
    private boolean success;

    /** 执行消息 */
    private String message;

    /** 影响行数（查询为null） */
    private Integer affectedRows;

    /** 查询结果列名 */
    private List<String> columns;

    /** 列元数据 */
    private List<ColumnMetaVo> columnsMeta;

    /** 查询结果数据 */
    private List<Map<String, Object>> rows;

    /** 查询总条数（分页用） */
    private Long total;

    /** 执行耗时ms */
    private Long execTimeMs;

    /** 当前页码 */
    private Integer pageNum;

    /** 每页大小 */
    private Integer pageSize;

    /** 是否为单表查询 */
    private Boolean singleTableQuery;

    /** 主键列名（单表查询时有值） */
    private String pkColumn;

    /** 查询的表名（单表查询时有值） */
    private String queryTable;

    /** 查询的schema（SQL指定时优先，否则为数据源默认schema） */
    private String querySchema;

    /** SQL中的注释列表 */
    private List<String> comments;
}
