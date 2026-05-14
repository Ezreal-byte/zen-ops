package com.ops.zen.entity.response;

import lombok.Data;

/**
 * @author xyn
 * @date 2025/5/27 17:56
 * @description
 **/
@Data
public class ScheduleLogContentResponse {


    private Long pkScheduleLog;

    private String content;

    /**
     * offset = -1代表执行结束
     */
    private Integer offset;

}
