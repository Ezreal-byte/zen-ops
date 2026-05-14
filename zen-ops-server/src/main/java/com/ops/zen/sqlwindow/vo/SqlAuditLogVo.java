package com.ops.zen.sqlwindow.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.LocalDateTime2StringDeserializer;
import com.ops.zen.json.LocalDateTime2StringSerializer;
import com.ops.zen.json.Long2StringDeserializer;
import com.ops.zen.json.Long2StringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SQL审计日志VO（连表查询返回）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlAuditLogVo implements Serializable {

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

    /** 用户中文姓名 */
    private String userName;

    /** 用户登录名 */
    private String userLoginName;
}
