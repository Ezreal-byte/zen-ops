package com.ops.zen.entity.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.json.Long2StringDeserializer;
import com.ops.zen.json.Long2StringSerializer;
import lombok.Data;

/**
 * @author xyn
 * @date 2025/5/27 17:13
 * @description
 **/
@Data
public class ScheduleGroupListResponse {

    /**
     * 分组主键
     */
    @JsonSerialize(using = Long2StringSerializer.class)
    @JsonDeserialize(using = Long2StringDeserializer.class)
    private Long pkScheduleGroup;

    /**
     * 分组名称
     */
    private String name;

}
