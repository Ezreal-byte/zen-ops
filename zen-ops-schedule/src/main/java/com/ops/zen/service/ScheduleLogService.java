package com.ops.zen.service;

import com.ops.zen.entity.request.ScheduleLogQueryRequest;
import com.ops.zen.entity.response.ScheduleLogContentResponse;
import com.ops.zen.entity.response.ScheduleLogQueryResponse;
import com.ops.zen.utils.map.PageResult;

/**
 * @author xyn
 * @date 2025/5/27 17:04
 * @description
 **/
public interface ScheduleLogService {
    void deleteBySchedule(Long pkSchedule);

    void delete(Long pkScheduleLog);

    ScheduleLogContentResponse getContent(Long pkScheduleLog, Integer offset);

    PageResult<ScheduleLogQueryResponse> queryBySchedule(ScheduleLogQueryRequest request);
}
