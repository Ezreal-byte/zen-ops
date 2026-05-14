package com.ops.zen.service;

import com.ops.zen.entity.ZenFsoDs;

import java.util.List;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 对象存储数据源服务层
 */
public interface FsoDataSourceService {

    /**
     * 新增数据源
     */
    String add(ZenFsoDs fsoDataSource);

    /**
     * 删除数据源
     */
    String delete(Long pkFsoDs);

    /**
     * 更新数据源
     */
    String update(ZenFsoDs fsoDataSource);

    /**
     * 查询详情
     */
    ZenFsoDs get(Long pkFsoDs);

    /**
     * 查询当前用户的数据源列表
     */
    List<ZenFsoDs> listByCurrentUser();

    /**
     * 测试连接
     */
    String testConnection(Long pkFsoDs);

    /**
     * 使用临时配置测试连接
     */
    String testConnectionWithConfig(ZenFsoDs fsoDataSource);

    /**
     * 设置默认数据源（同时取消该用户其他数据源的默认状态）
     */
    String setDefault(Long pkFsoDs);
}
