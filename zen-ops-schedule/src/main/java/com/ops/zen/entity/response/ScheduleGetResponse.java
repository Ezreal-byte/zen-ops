package com.ops.zen.entity.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.entity.request.ScheduleAddUpdateRequest;
import com.ops.zen.json.Number2BooleanDeserializer;
import com.ops.zen.json.Number2BooleanSerializer;
import lombok.Data;

/**
 * @author xyn
 * @date 2025/5/27 17:13
 * @description
 **/
@Data
public class ScheduleGetResponse extends ScheduleAddUpdateRequest {

    /**
     * 上次运行状态1成功0失败
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte lastRunStatus;

    /**
     * 运行时信息
     */
    private String clobRunTime;

}
