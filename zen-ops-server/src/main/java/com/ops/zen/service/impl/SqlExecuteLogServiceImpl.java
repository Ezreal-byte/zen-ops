package com.ops.zen.service.impl;

import com.ops.zen.entity.ZenDbExecLog;
import com.ops.zen.mapper.ZenDbExecLogMapper;
import com.ops.zen.service.SqlExecuteLogService;
import com.ops.zen.utils.pk.SnowPkGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SqlExecuteLogServiceImpl implements SqlExecuteLogService {

    @Autowired
    private ZenDbExecLogMapper mapper;

    @Override
    public void saveLog(ZenDbExecLog sqlLog) {
        if (sqlLog.getPkLog() == null) {
            sqlLog.setPkLog(SnowPkGenerator.generateSnow());
        }
        if (sqlLog.getDtCreated() == null) {
            sqlLog.setDtCreated(LocalDateTime.now());
        }
        mapper.insert(sqlLog);
    }
}
