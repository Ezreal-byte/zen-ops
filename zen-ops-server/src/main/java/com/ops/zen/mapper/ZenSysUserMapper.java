package com.ops.zen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.zen.entity.ZenSysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ZenSysUserMapper extends BaseMapper<ZenSysUser> {

    @Select("select * from zen_sys_user where pk_user = #{pkUser}")
    ZenSysUser selectByPrimaryKeyWithBlobs(@Param("pkUser") Long pkUser);

    int insertBatch(@Param("list") List<ZenSysUser> list);
}
