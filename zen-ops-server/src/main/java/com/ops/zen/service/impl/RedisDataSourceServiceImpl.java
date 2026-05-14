package com.ops.zen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ops.zen.entity.ZenRedisDs;
import com.ops.zen.mapper.ZenRedisDsMapper;
import com.ops.zen.service.RedisDataSourceService;
import com.ops.zen.utils.RedisClientUtil;
import com.ops.zen.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis数据源Service实现
 * @Date 2026-05-06
 */
@Service
@Slf4j
public class RedisDataSourceServiceImpl implements RedisDataSourceService {

    @Autowired
    private ZenRedisDsMapper redisDataSourceMapper;

    @Override
    public String add(ZenRedisDs redisDataSource) {
        redisDataSourceMapper.insert(redisDataSource);
        return null;
    }

    @Override
    public String delete(Long pkRedisDs) {
        redisDataSourceMapper.deleteById(pkRedisDs);
        return null;
    }

    @Override
    public String update(ZenRedisDs redisDataSource) {
        redisDataSourceMapper.updateById(redisDataSource);
        return null;
    }

    @Override
    public ZenRedisDs get(Long pkRedisDs) {
        return redisDataSourceMapper.selectById(pkRedisDs);
    }

    @Override
    public List<ZenRedisDs> listByCurrentUser() {
        LambdaQueryWrapper<ZenRedisDs> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ZenRedisDs::getPkCreatedby, UserContext.getUserId());
        wrapper.eq(ZenRedisDs::getDs, 0);
        wrapper.orderByDesc(ZenRedisDs::getDtCreated);
        return redisDataSourceMapper.selectList(wrapper);
    }

    @Override
    public String testConnection(Long pkRedisDs) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            return "数据源不存在";
        }
        return RedisClientUtil.testConnection(ds);
    }

    @Override
    public String setDefault(Long pkRedisDs) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        Long userId = UserContext.getUserId();
        if (ds.getIsDefault() != null && ds.getIsDefault() == 1) {
            // 当前已是默认，取消默认
            ds.setIsDefault((byte) 0);
            redisDataSourceMapper.updateById(ds);
        } else {
            // 先将该用户所有数据源设为非默认
            UpdateWrapper<ZenRedisDs> clearWrapper = new UpdateWrapper<>();
            clearWrapper.eq("PK_CREATEDBY", userId)
                        .eq("DS", 0)
                        .set("IS_DEFAULT", 0);
            redisDataSourceMapper.update(null, clearWrapper);
            // 再将指定数据源设为默认
            ds.setIsDefault((byte) 1);
            redisDataSourceMapper.updateById(ds);
        }
        return null;
    }
}
