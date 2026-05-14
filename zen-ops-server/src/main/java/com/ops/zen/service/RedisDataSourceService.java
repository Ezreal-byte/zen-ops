package com.ops.zen.service;

import com.ops.zen.entity.ZenRedisDs;

import java.util.List;

/**
 * Redis数据源Service
 * @Date 2026-05-06
 */
public interface RedisDataSourceService {

    /**
     * 新增数据源
     */
    String add(ZenRedisDs redisDataSource);

    /**
     * 删除数据源
     */
    String delete(Long pkRedisDs);

    /**
     * 更新数据源
     */
    String update(ZenRedisDs redisDataSource);

    /**
     * 获取数据源
     */
    ZenRedisDs get(Long pkRedisDs);

    /**
     * 获取当前用户的数据源列表
     */
    List<ZenRedisDs> listByCurrentUser();

    /**
     * 测试连接
     */
    String testConnection(Long pkRedisDs);

    /**
     * 设置默认数据源（同时取消该用户其他数据源的默认状态）
     */
    String setDefault(Long pkRedisDs);
}
