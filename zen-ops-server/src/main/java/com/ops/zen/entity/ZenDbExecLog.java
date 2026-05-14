package com.ops.zen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.ops.zen.json.LocalDateTime2StringDeserializer;
import com.ops.zen.json.LocalDateTime2StringSerializer;
import com.ops.zen.json.Long2StringDeserializer;
import com.ops.zen.json.Long2StringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL执行审计日志
 * @Date 2026-04-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("zen_db_exec_log")
public class ZenDbExecLog implements Serializable {

    @TableId(value = "PK_LOG", type = IdType.INPUT)
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkLog;

    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkDs;

    private String dbSchema;

    private String sqlText;

    private String sqlType;

    /** DML子类型 INSERT/UPDATE/DELETE */
    private String dmlType;

    /** 是否为单表查询 */
    private Boolean singleTableQuery;

    /** 主键列名（单表查询时有值） */
    private String pkColumn;

    /** 查询的表名（单表查询时有值） */
    private String queryTable;

    /** 查询的schema（SQL指定时优先，否则为数据源默认schema） */
    private String querySchema;

    private String execStatus;

    private Long execTimeMs;

    private String errorMsg;

    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long affectedRows;

    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkCreatedby;

    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtCreated;
}
