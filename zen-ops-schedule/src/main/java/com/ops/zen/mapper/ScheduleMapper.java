package com.ops.zen.mapper;

import com.ops.zen.entity.Schedule;
import com.ops.zen.entity.ScheduleExample;
import com.ops.zen.entity.ScheduleWithBLOBs;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ScheduleMapper {
    long countByExample(ScheduleExample example);

    int deleteByExample(ScheduleExample example);

    int deleteByPrimaryKey(Long pkSchedule);

    int insert(ScheduleWithBLOBs record);

    int insertSelective(ScheduleWithBLOBs record);

    List<ScheduleWithBLOBs> selectByExampleWithBLOBs(ScheduleExample example);

    List<Schedule> selectByExample(ScheduleExample example);

    ScheduleWithBLOBs selectByPrimaryKey(Long pkSchedule);

    int updateByExampleSelective(@Param("record") ScheduleWithBLOBs record, @Param("example") ScheduleExample example);

    int updateByExampleWithBLOBs(@Param("record") ScheduleWithBLOBs record, @Param("example") ScheduleExample example);

    int updateByExample(@Param("record") Schedule record, @Param("example") ScheduleExample example);

    int updateByPrimaryKeySelective(ScheduleWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ScheduleWithBLOBs record);

    int updateByPrimaryKey(Schedule record);

    int insertBatch(List<Schedule> list);
}
