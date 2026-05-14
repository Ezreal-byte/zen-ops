package com.ops.zen.entity.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.Long2StringDeserializer;
import com.ops.zen.json.Long2StringSerializer;
import com.ops.zen.json.Number2BooleanDeserializer;
import com.ops.zen.json.Number2BooleanSerializer;
import lombok.Data;

/**
 * @author xyn
 * @date 2025/5/27 17:10
 * @description
 **/
@Data
public class ScheduleQueryResponse {

    /**
     * 任务主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkSchedule;

    /**
     * 分组主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkScheduleGroup;


    private String groupName;


    /**
     * 定时任务名称
     */
    private String name;

    /**
     * 任务类
     */
    private String jobClass;

    /**
     * 触发器类型 simple简单触发器固定间隔 cron日程触发器
     */
    private String triggerType;

    /**
     * 表达式, CRON或秒
     */
    private String triggerExpr;

    /**
     * 负责人
     */
    private String principal;

    /**
     * 是否激活1激活0未激活
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isActive;

    /**
     * 上次运行状态1成功0失败
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte lastRunStatus;

    /**
     * 描述
     */
    private String des;
}
