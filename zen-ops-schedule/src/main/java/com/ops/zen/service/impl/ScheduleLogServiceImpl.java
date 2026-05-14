package com.ops.zen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ops.zen.en.RunStatusEn;
import com.ops.zen.entity.ScheduleLogExample;
import com.ops.zen.entity.ScheduleLogWithBLOBs;
import com.ops.zen.entity.request.ScheduleLogQueryRequest;
import com.ops.zen.entity.response.ScheduleLogContentResponse;
import com.ops.zen.entity.response.ScheduleLogQueryResponse;
import com.ops.zen.mapper.ScheduleLogMapper;
import com.ops.zen.service.ScheduleLogService;
import com.ops.zen.utils.map.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xyn
 * @date 2025/5/27 17:04
 * @description
 **/
@Service
public class ScheduleLogServiceImpl implements ScheduleLogService {

    @Autowired
    private ScheduleLogMapper scheduleLogMapper;

    @Override
    public ScheduleLogContentResponse getContent(Long pkScheduleLog, Integer offset) {
        ScheduleLogWithBLOBs scheduleLog = scheduleLogMapper.selectByPrimaryKey(pkScheduleLog);
        Byte statusRun = scheduleLog.getStatusRun();
        if (statusRun.equals(RunStatusEn.SUCCESS)) {
            String clobLog = scheduleLog.getClobLog();
            String resultContent = offset == null || offset == 0 ? clobLog :
                    // 截取偏移量行之后的日志
                    clobLog.substring(clobLog.indexOf("\n", offset));
                    ScheduleLogContentResponse response = new ScheduleLogContentResponse();
            response.setPkScheduleLog(pkScheduleLog);
            response.setContent(resultContent);
            response.setOffset(-1);
            return response;
        }
        //todo 从缓存里取
        return null;
    }

    @Override
    public void deleteBySchedule(Long pkSchedule) {
        ScheduleLogExample example = new ScheduleLogExample();
        ScheduleLogExample.Criteria criteria = example.createCriteria();
        criteria.andPkScheduleEqualTo(pkSchedule);
        scheduleLogMapper.deleteByExample(example);
    }

    @Override
    public void delete(Long pkScheduleLog) {
        scheduleLogMapper.deleteByPrimaryKey(pkScheduleLog);
    }

    @Override
    public PageResult<ScheduleLogQueryResponse> queryBySchedule(ScheduleLogQueryRequest request) {

        ScheduleLogExample example = new ScheduleLogExample();
        ScheduleLogExample.Criteria criteria = example.createCriteria();
        criteria.andPkScheduleEqualTo(request.getPkSchedule());
        if (Objects.nonNull(request.getDtRunStart())) {
            criteria.andDtRunGreaterThanOrEqualTo(request.getDtRunStart());
        }
        if (Objects.nonNull(request.getDtRunEnd())) {
            criteria.andDtRunLessThanOrEqualTo(request.getDtRunEnd());
        }
        if (Objects.nonNull(request.getStatusRun())) {
            criteria.andStatusRunEqualTo(request.getStatusRun());
        }
        example.setOrderByClause("pk_schedule_log desc");
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<ScheduleLogWithBLOBs> list = scheduleLogMapper.selectByExampleWithBLOBs(example);
        PageInfo pageInfo = new PageInfo<>(list);
        List<ScheduleLogQueryResponse> responseList = new ArrayList<>();
        for (ScheduleLogWithBLOBs item : list) {
            ScheduleLogQueryResponse response = new ScheduleLogQueryResponse();
            response.setPkScheduleLog(item.getPkScheduleLog());
            response.setPkSchedule(item.getPkSchedule());
            response.setDtRun(item.getDtRun());
            response.setStatusRun(item.getStatusRun());
            response.setResultRun(item.getResultRun());
            response.setElapsedTime(item.getElapsedTime());
            response.setClobLog(item.getClobLog());
            responseList.add(response);
        }
        return PageResult.of(responseList, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }
}
