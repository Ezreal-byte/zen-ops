package com.ops.zen.sqlwindow;

import com.ops.zen.jdbc.EasyRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SQL执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlExecuteResult {

    /** 原始SQL */
    private String sql;

    /** SQL类型 QUERY/DDL/DML/UPDATE */
    private String sqlType;

    /** 是否成功 */
    private boolean success;

    /** 执行消息 */
    private String message;

    /** 影响行数（查询为null） */
    private Integer affectedRows;

    /** 查询结果列名 */
    private List<String> columns;

    /** 查询结果数据 */
    private List<EasyRecord> rows;

    /** 查询总条数（分页用） */
    private Long total;

    /** 执行耗时ms */
    private Long execTimeMs;

    /** 当前页码 */
    private Integer pageNum;

    /** 每页大小 */
    private Integer pageSize;
}
