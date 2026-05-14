package com.ops.zen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ops.zen.entity.ZenRedisDs;
import org.apache.ibatis.annotations.Mapper;

/**
 * Redis数据源Mapper
 * @Date 2026-05-06
 */
@Mapper
public interface ZenRedisDsMapper extends BaseMapper<ZenRedisDs> {
}
