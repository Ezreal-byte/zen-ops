package com.ops.zen.mapper;

import com.ops.zen.entity.ScheduleGroup;
import com.ops.zen.entity.ScheduleGroupExample;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ScheduleGroupMapper {
    long countByExample(ScheduleGroupExample example);

    int deleteByExample(ScheduleGroupExample example);

    int deleteByPrimaryKey(Long pkScheduleGroup);

    int insert(ScheduleGroup record);

    int insertSelective(ScheduleGroup record);

    List<ScheduleGroup> selectByExample(ScheduleGroupExample example);

    ScheduleGroup selectByPrimaryKey(Long pkScheduleGroup);

    int updateByExampleSelective(@Param("record") ScheduleGroup record, @Param("example") ScheduleGroupExample example);

    int updateByExample(@Param("record") ScheduleGroup record, @Param("example") ScheduleGroupExample example);

    int updateByPrimaryKeySelective(ScheduleGroup record);

    int updateByPrimaryKey(ScheduleGroup record);

    int insertBatch(List<ScheduleGroup> list);
}
