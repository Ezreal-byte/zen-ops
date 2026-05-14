package com.ops.zen.mapper;

import com.ops.zen.entity.ScheduleLog;
import com.ops.zen.entity.ScheduleLogExample;
import com.ops.zen.entity.ScheduleLogWithBLOBs;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ScheduleLogMapper {
    long countByExample(ScheduleLogExample example);

    int deleteByExample(ScheduleLogExample example);

    int deleteByPrimaryKey(Long pkScheduleLog);

    int insert(ScheduleLogWithBLOBs record);

    int insertSelective(ScheduleLogWithBLOBs record);

    List<ScheduleLogWithBLOBs> selectByExampleWithBLOBs(ScheduleLogExample example);

    List<ScheduleLog> selectByExample(ScheduleLogExample example);

    ScheduleLogWithBLOBs selectByPrimaryKey(Long pkScheduleLog);

    int updateByExampleSelective(@Param("record") ScheduleLogWithBLOBs record, @Param("example") ScheduleLogExample example);

    int updateByExampleWithBLOBs(@Param("record") ScheduleLogWithBLOBs record, @Param("example") ScheduleLogExample example);

    int updateByExample(@Param("record") ScheduleLog record, @Param("example") ScheduleLogExample example);

    int updateByPrimaryKeySelective(ScheduleLogWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ScheduleLogWithBLOBs record);

    int updateByPrimaryKey(ScheduleLog record);

    int insertBatch(List<ScheduleLog> list);
}
