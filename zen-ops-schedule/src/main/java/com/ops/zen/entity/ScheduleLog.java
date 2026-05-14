package com.ops.zen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.ops.zen.json.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定时任务日志
 * @Date 2025-05-27 18:15:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("zen_schedule_log")
public class ScheduleLog implements Serializable {
    /**
     * 主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkScheduleLog;

    /**
     * 任务主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkSchedule;

    /**
     * 运行时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtRun;

    /**
     * 运行状态 1成功0失败 2执行中
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte statusRun;

    /**
     * 运行结果
     */
    private String resultRun;

    /**
     * 耗时 ms
     */
    private String elapsedTime;

    /**
     * 运行节点标志
     */
    private String runNode;

    /**
     * 是否手动
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isManual;

    /**
     * 创建人
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkCreatedby;

    /**
     * 创建时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtCreated;

    /**
     * 修改人
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkModifiedby;

    /**
     * 修改时间
     */
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    private LocalDateTime dtModified;

    /**
     * 删除标志
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte ds;

    /**
     * 版本
     */
    private Double version;
}
