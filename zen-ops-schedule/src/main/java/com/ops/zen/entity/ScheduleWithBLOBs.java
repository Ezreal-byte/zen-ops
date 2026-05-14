package com.ops.zen.entity;

import lombok.Data;

@Data
public class ScheduleWithBLOBs extends Schedule {
    /**
     * 配置信息
     */
    private String clobCfg;

    /**
     * 运行时信息
     */
    private String clobRunTime;
}
