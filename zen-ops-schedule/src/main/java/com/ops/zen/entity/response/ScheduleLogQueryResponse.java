package com.ops.zen.entity.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.Long2StringDeserializer;
import com.ops.zen.json.Long2StringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xyn
 * @date 2025/5/27 18:03
 * @description
 **/
@Data
public class ScheduleLogQueryResponse {

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
    private LocalDateTime dtRun;

    /**
     * 运行状态 1成功0失败
     */
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
     * 运行日志
     */
    private String clobLog;
}
