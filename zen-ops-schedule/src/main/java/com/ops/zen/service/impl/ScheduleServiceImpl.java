package com.ops.zen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ops.zen.en.BooleanEn;
import com.ops.zen.entity.Schedule;
import com.ops.zen.entity.ScheduleExample;
import com.ops.zen.entity.ScheduleGroup;
import com.ops.zen.entity.ScheduleWithBLOBs;
import com.ops.zen.entity.request.ScheduleAddUpdateRequest;
import com.ops.zen.entity.request.ScheduleQueryRequest;
import com.ops.zen.entity.response.ScheduleGetResponse;
import com.ops.zen.entity.response.ScheduleQueryResponse;
import com.ops.zen.mapper.ScheduleGroupMapper;
import com.ops.zen.mapper.ScheduleMapper;
import com.ops.zen.service.ScheduleLogService;
import com.ops.zen.service.ScheduleService;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.CollectionUtils;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.map.PageResult;
import com.ops.zen.utils.pk.SnowPkGenerator;
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
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ScheduleLogService logService;

    @Autowired
    private ScheduleGroupMapper groupMapper;

    @Override
    public PageResult<ScheduleQueryResponse> queryByPage(ScheduleQueryRequest request) {
        ScheduleExample example = new ScheduleExample();
        ScheduleExample.Criteria criteria = example.createCriteria();
        if (Objects.nonNull(request.getPkScheduleGroup())) {
            criteria.andPkScheduleGroupEqualTo(request.getPkScheduleGroup());
        }
        if (StringUtils.isNotEmpty(request.getName())) {
            criteria.andNameLike("%" + request.getName() + "%");
        }
        if (StringUtils.isNotEmpty(request.getJobClass())) {
            criteria.andJobClassLike("%" + request.getJobClass() + "%");
        }
        if (StringUtils.isNotEmpty(request.getTriggerType())) {
            criteria.andTriggerTypeEqualTo(request.getTriggerType());
        }
        if (StringUtils.isNotEmpty(request.getPrincipal())) {
            criteria.andPrincipalLike("%" + request.getPrincipal() + "%");
        }
        if (Objects.nonNull(request.getIsActive())) {
            criteria.andIsActiveEqualTo(request.getIsActive());
        }
        if (StringUtils.isNotEmpty(request.getDes())) {
            criteria.andDesEqualTo(request.getDes());
        }
        example.setOrderByClause("pk_schedule desc");
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<ScheduleWithBLOBs> list = scheduleMapper.selectByExampleWithBLOBs(example);
        PageInfo pageInfo = new PageInfo<>(list);
        List<ScheduleQueryResponse> responseList = new ArrayList<>();
        for (ScheduleWithBLOBs item : list) {
            ScheduleQueryResponse response = new ScheduleQueryResponse();
            response.setPkSchedule(item.getPkSchedule());
            response.setPkScheduleGroup(item.getPkScheduleGroup());
            response.setName(item.getName());
            response.setJobClass(item.getJobClass());
            response.setTriggerType(item.getTriggerType());
            response.setTriggerExpr(item.getTriggerExpr());
            response.setPrincipal(item.getPrincipal());
            response.setIsActive(item.getIsActive());
            response.setDes(item.getDes());
            response.setLastRunStatus(item.getLastRunStatus());
            response.setGroupName(getGroupName(item.getPkScheduleGroup()));
            responseList.add(response);
        }
        return PageResult.of(responseList, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    private String getGroupName(Long pkScheduleGroup) {
        ScheduleGroup scheduleGroup = groupMapper.selectByPrimaryKey(pkScheduleGroup);
        return Objects.isNull(scheduleGroup) ? null : scheduleGroup.getName();
    }

    @Override
    public String add(ScheduleAddUpdateRequest request) {
        ScheduleWithBLOBs schedule = new ScheduleWithBLOBs();
        schedule.setPkSchedule(SnowPkGenerator.generateSnow());
        schedule.setPkScheduleGroup(request.getPkScheduleGroup());
        schedule.setName(request.getName());
        schedule.setJobClass(request.getJobClass());
        schedule.setTriggerType(request.getTriggerType());
        schedule.setTriggerExpr(request.getTriggerExpr());
        schedule.setPrincipal(request.getPrincipal());
        schedule.setIsActive(request.getIsActive());
        schedule.setDes(request.getDes());
        schedule.setClobCfg(request.getClobCfg());
        scheduleMapper.insertSelective(schedule);

        if (Objects.equals(request.getIsActive(), BooleanEn.TRUE)) {
            //todo 新增到定时任务调度池
        }
        return "";
    }

    @Override
    public String update(ScheduleAddUpdateRequest request) {
        Assert.notNull(request.getPkSchedule(), "pkSchedule can not be null");
        Schedule oldSchedule = scheduleMapper.selectByPrimaryKey(request.getPkSchedule());
        ScheduleWithBLOBs schedule = new ScheduleWithBLOBs();
        schedule.setPkSchedule(request.getPkSchedule());
        schedule.setPkScheduleGroup(request.getPkScheduleGroup());
        schedule.setName(request.getName());
        schedule.setJobClass(request.getJobClass());
        schedule.setTriggerType(request.getTriggerType());
        schedule.setTriggerExpr(request.getTriggerExpr());
        schedule.setPrincipal(request.getPrincipal());
        schedule.setIsActive(request.getIsActive());
        schedule.setDes(request.getDes());
        schedule.setClobCfg(request.getClobCfg());
        scheduleMapper.updateByPrimaryKeySelective(schedule);

        if (!Objects.equals(oldSchedule.getIsActive(), request.getIsActive())) {
            // todo 修改了是否激活状态，需要修改定时任务调度池
        }
        return null;
    }

    @Override
    public ScheduleGetResponse get(Long pkSchedule) {
        ScheduleExample example = new ScheduleExample();
        ScheduleExample.Criteria criteria = example.createCriteria();
        criteria.andPkScheduleEqualTo(pkSchedule);
        List<ScheduleWithBLOBs> scheduleWithBLOBs = scheduleMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(scheduleWithBLOBs)) {
            ScheduleWithBLOBs schedule = scheduleWithBLOBs.get(0);
            ScheduleGetResponse response = new ScheduleGetResponse();
            response.setPkSchedule(schedule.getPkSchedule());
            response.setPkScheduleGroup(schedule.getPkScheduleGroup());
            response.setName(schedule.getName());
            response.setJobClass(schedule.getJobClass());
            response.setTriggerType(schedule.getTriggerType());
            response.setTriggerExpr(schedule.getTriggerExpr());
            response.setPrincipal(schedule.getPrincipal());
            response.setIsActive(schedule.getIsActive());
            response.setDes(schedule.getDes());
            response.setClobCfg(schedule.getClobCfg());
            response.setClobRunTime(schedule.getClobRunTime());
            response.setLastRunStatus(schedule.getLastRunStatus());
            return response;
        }
        return null;
    }

    @Override
    public String delete(Long pkSchedule) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(pkSchedule);
        if (Objects.equals(schedule.getIsActive(), BooleanEn.TRUE)) {
            // todo 从定时任务调度池中删除
        }
        scheduleMapper.deleteByPrimaryKey(pkSchedule);
        logService.deleteBySchedule(pkSchedule);
        return null;
    }

    @Override
    public Long countByScheduleGroup(Long pkScheduleGroup) {
        ScheduleExample example = new ScheduleExample();
        ScheduleExample.Criteria criteria = example.createCriteria();
        criteria.andPkScheduleGroupEqualTo(pkScheduleGroup);
        return scheduleMapper.countByExample(example);
    }
}
