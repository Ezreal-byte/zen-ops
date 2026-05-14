package com.ops.zen.service;

import com.ops.zen.entity.ZenDbExecLog;

/**
 * SQL执行审计日志Service
 */
public interface SqlExecuteLogService {

    /**
     * 保存执行日志
     */
    void saveLog(ZenDbExecLog log);
}
