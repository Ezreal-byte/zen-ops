package com.ops.zen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.zen.entity.ZenSysRoleUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZenSysRoleUserMapper extends BaseMapper<ZenSysRoleUser> {
    int insertBatch(@Param("list") List<ZenSysRoleUser> list);
}
