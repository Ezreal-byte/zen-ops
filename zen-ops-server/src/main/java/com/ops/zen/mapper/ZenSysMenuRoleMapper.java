package com.ops.zen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.zen.entity.ZenSysMenuRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZenSysMenuRoleMapper extends BaseMapper<ZenSysMenuRole> {
    int insertBatch(@Param("list") List<ZenSysMenuRole> list);
}
