package com.ops.zen.service.impl;

import com.ops.zen.entity.ScheduleGroup;
import com.ops.zen.entity.response.ScheduleGroupListResponse;
import com.ops.zen.mapper.ScheduleGroupMapper;
import com.ops.zen.service.ScheduleGroupService;
import com.ops.zen.service.ScheduleService;
import com.ops.zen.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xyn
 * @date 2025/5/27 17:04
 * @description
 **/
@Service
public class ScheduleGroupServiceImpl implements ScheduleGroupService {

    @Autowired
    ScheduleGroupMapper scheduleGroupMapper;

    @Autowired
    ScheduleService scheduleService;

    @Override
    public String getName(Long pkScheduleGroup) {
        ScheduleGroup scheduleGroup = scheduleGroupMapper.selectByPrimaryKey(pkScheduleGroup);
        return Objects.isNull(scheduleGroup) ? null : scheduleGroup.getName();
    }

    @Override
    public List<ScheduleGroupListResponse> queryList() {
        List<ScheduleGroup> list = scheduleGroupMapper.selectByExample(null);

        return list.stream().map(item -> {
            ScheduleGroupListResponse row = new ScheduleGroupListResponse();
            row.setPkScheduleGroup(item.getPkScheduleGroup());
            row.setName(item.getName());
            return row;
        }).collect(Collectors.toList());
    }

    @Override
    public String delete(Long pkScheduleGroup) {
        Long count = scheduleService.countByScheduleGroup(pkScheduleGroup);
        Assert.isTrue(count == 0, "该分组下存在任务，不能删除");
        scheduleGroupMapper.deleteByPrimaryKey(pkScheduleGroup);
        return null;
    }
}
