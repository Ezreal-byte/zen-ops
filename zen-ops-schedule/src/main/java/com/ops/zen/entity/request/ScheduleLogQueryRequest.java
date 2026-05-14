package com.ops.zen.entity.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.LocalDateTime2StringDeserializer;
import com.ops.zen.json.LocalDateTime2StringSerializer;
import com.ops.zen.json.Long2StringDeserializer;
import com.ops.zen.json.Long2StringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xyn
 * @date 2025/5/27 17:08
 * @description
 **/
@Data
public class ScheduleLogQueryRequest {

    /**
     * 任务主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkSchedule;

    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    private LocalDateTime dtRunStart;

    @JsonDeserialize(using = LocalDateTime2StringDeserializer.class)
    @JsonSerialize(using = LocalDateTime2StringSerializer.class)
    private LocalDateTime dtRunEnd;

    /**
     * 运行状态 1成功0失败
     */
    private Byte statusRun;

    private int pageNum = 1;

    private int pageSize = 20;

}
