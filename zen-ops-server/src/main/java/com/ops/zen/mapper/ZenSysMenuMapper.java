package com.ops.zen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.zen.entity.ZenSysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZenSysMenuMapper extends BaseMapper<ZenSysMenu> {
    int insertBatch(@Param("list") List<ZenSysMenu> list);
}
