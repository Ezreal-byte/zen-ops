package com.ops.zen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.zen.entity.ZenSysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZenSysRoleMapper extends BaseMapper<ZenSysRole> {
    int insertBatch(@Param("list") List<ZenSysRole> list);
}
