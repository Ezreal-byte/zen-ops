package com.ops.zen.service;

import com.ops.zen.entity.response.ScheduleGroupListResponse;

import java.util.List;

/**
 * @author xyn
 * @date 2025/5/27 17:04
 * @description
 **/
public interface ScheduleGroupService {
    String getName(Long pkScheduleGroup);

    List<ScheduleGroupListResponse> queryList();

    String delete(Long pkScheduleGroup);
}
