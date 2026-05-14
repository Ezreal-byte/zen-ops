package com.ops.zen.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ops.zen.entity.ZenFsoDs;
import com.ops.zen.fso.FsoConfig;
import com.ops.zen.fso.FsoFactory;
import com.ops.zen.fso.FsoService;
import com.ops.zen.mapper.ZenFsoDsMapper;
import com.ops.zen.service.FsoDataSourceService;
import com.ops.zen.utils.UserContext;
import com.ops.zen.utils.pk.SnowPkGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 对象存储数据源服务层实现
 */
@Service
@Slf4j
public class FsoDataSourceServiceImpl implements FsoDataSourceService {

    @Autowired
    private ZenFsoDsMapper mapper;

    @Override
    public String add(ZenFsoDs fsoDataSource) {
        fsoDataSource.setPkFsoDs(SnowPkGenerator.generateSnow());
        fsoDataSource.setPkCreatedby(UserContext.getUserId());
        fsoDataSource.setDtCreated(LocalDateTime.now());
        mapper.insert(fsoDataSource);
        return null;
    }

    @Override
    public String delete(Long pkFsoDs) {
        mapper.deleteById(pkFsoDs);
        FsoFactory.removeCache(pkFsoDs.toString());
        return null;
    }

    @Override
    public String update(ZenFsoDs fsoDataSource) {
        fsoDataSource.setPkModifiedby(UserContext.getUserId());
        fsoDataSource.setDtModified(LocalDateTime.now());
        mapper.updateById(fsoDataSource);
        // 清除缓存，下次使用时重新初始化
        FsoFactory.removeCache(fsoDataSource.getPkFsoDs().toString());
        return null;
    }

    @Override
    public ZenFsoDs get(Long pkFsoDs) {
        return mapper.selectById(pkFsoDs);
    }

    @Override
    public List<ZenFsoDs> listByCurrentUser() {
        Long userId = UserContext.getUserId();
        QueryWrapper<ZenFsoDs> wrapper = new QueryWrapper<>();
        wrapper.eq("PK_CREATEDBY", userId)
               .eq("DS", 0)
               .orderByDesc("DT_CREATED");
        return mapper.selectList(wrapper);
    }

    @Override
    public String testConnection(Long pkFsoDs) {
        ZenFsoDs fsoDataSource = mapper.selectById(pkFsoDs);
        if (fsoDataSource == null) {
            return "数据源不存在";
        }
        try {
            FsoConfig config = parseConfig(fsoDataSource);
            FsoService fsoService = FsoFactory.createFsoService(fsoDataSource.getType(), config);
            fsoService.listBuckets();
            return null;
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return "连接失败: " + e.getMessage();
        }
    }

    @Override
    public String testConnectionWithConfig(ZenFsoDs fsoDataSource) {
        try {
            FsoConfig config = parseConfig(fsoDataSource);
            FsoService fsoService = FsoFactory.createFsoService(fsoDataSource.getType(), config);
            fsoService.listBuckets();
            return null;
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return "连接失败: " + e.getMessage();
        }
    }

    @Override
    public String setDefault(Long pkFsoDs) {
        ZenFsoDs ds = mapper.selectById(pkFsoDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        Long userId = UserContext.getUserId();
        if (ds.getIsDefault() != null && ds.getIsDefault() == 1) {
            // 当前已是默认，取消默认
            ds.setIsDefault((byte) 0);
            mapper.updateById(ds);
        } else {
            // 先将该用户所有数据源设为非默认
            UpdateWrapper<ZenFsoDs> clearWrapper = new UpdateWrapper<>();
            clearWrapper.eq("PK_CREATEDBY", userId)
                        .eq("DS", 0)
                        .set("IS_DEFAULT", 0);
            mapper.update(null, clearWrapper);
            // 再将指定数据源设为默认
            ds.setIsDefault((byte) 1);
            mapper.updateById(ds);
        }
        return null;
    }

    /**
     * 从FsoDataSource解析配置，根据type动态反序列化到对应的Config类
     */
    public static FsoConfig parseConfig(ZenFsoDs ds) {
        if (ds.getClobConfig() != null && ds.getType() != null) {
            Class<? extends FsoConfig> configClass = FsoFactory.getConfigClass(ds.getType());
            return JSON.parseObject(ds.getClobConfig(), configClass);
        }
        return null;
    }
}
