package com.ops.zen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.zen.entity.ZenDbExecLog;
import com.ops.zen.sqlwindow.vo.SqlAuditLogVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZenDbExecLogMapper extends BaseMapper<ZenDbExecLog> {

    /**
     * 连表查询审计日志及用户信息
     */
    List<SqlAuditLogVo> selectLogsWithUser(
            @Param("pkCreatedby") Long pkCreatedby,
            @Param("dbSchema") String dbSchema,
            @Param("sqlType") String sqlType,
            @Param("execStatus") String execStatus,
            @Param("keyword") String keyword);
}
