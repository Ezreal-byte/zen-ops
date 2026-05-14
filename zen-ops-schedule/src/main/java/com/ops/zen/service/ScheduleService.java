package com.ops.zen.service;

import com.ops.zen.entity.request.ScheduleAddUpdateRequest;
import com.ops.zen.entity.request.ScheduleQueryRequest;
import com.ops.zen.entity.response.ScheduleGetResponse;
import com.ops.zen.entity.response.ScheduleQueryResponse;
import com.ops.zen.utils.map.PageResult;

/**
 * @author xyn
 * @date 2025/5/27 17:04
 * @description
 **/
public interface ScheduleService {

    PageResult<ScheduleQueryResponse> queryByPage(ScheduleQueryRequest request);

    String add(ScheduleAddUpdateRequest request);

    String update(ScheduleAddUpdateRequest request);

    ScheduleGetResponse get(Long pkSchedule);

    String delete(Long pkSchedule);


    Long countByScheduleGroup(Long pkScheduleGroup);
}
