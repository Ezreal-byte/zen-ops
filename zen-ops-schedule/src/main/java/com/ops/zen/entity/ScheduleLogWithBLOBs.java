package com.ops.zen.entity;

import lombok.Data;

@Data
public class ScheduleLogWithBLOBs extends ScheduleLog {
    /**
     * 运行日志
     */
    private String clobLog;

    /**
     * 运行时配置
     */
    private String runCfg;
}
