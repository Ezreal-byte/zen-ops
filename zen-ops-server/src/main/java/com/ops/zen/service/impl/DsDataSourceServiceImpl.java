package com.ops.zen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.mapper.ZenDbDsMapper;
import com.ops.zen.service.DsDataSourceService;
import com.ops.zen.utils.DataSourceConnectionUtils;
import com.ops.zen.utils.Jdbcs;
import com.ops.zen.utils.UserContext;
import com.ops.zen.utils.map.KvMap;
import com.ops.zen.utils.map.PageResult;
import com.ops.zen.utils.pk.SnowPkGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author xyn
 * @date 2025/4/9 20:44
 * @description
 **/
@Service
@Slf4j
public class DsDataSourceServiceImpl implements DsDataSourceService {

    @Autowired
    private ZenDbDsMapper mapper;

    @Override
    public String add(ZenDbDs pixelDatasource) {
        pixelDatasource.setPkDs(SnowPkGenerator.generateSnow());
        pixelDatasource.setDtCreated(LocalDateTime.now());
        pixelDatasource.setPkCreatedby(UserContext.getUserId());

        mapper.insert(pixelDatasource);
        return null;
    }

    @Override
    public String delete(Long pkDs) {
        mapper.deleteById(pkDs);
        return null;
    }

    @Override
    public String update(ZenDbDs pixelDatasource) {
        pixelDatasource.setDtModified(LocalDateTime.now());
        pixelDatasource.setPkModifiedby(UserContext.getUserId());
        mapper.updateById(pixelDatasource);
        return null;
    }

    @Override
    public ZenDbDs get(Long pkDs) {
        return mapper.selectById(pkDs);
    }

    @Override
    public PageResult<ZenDbDs> queryByPage(String keyword, Integer pageNum, Integer pageSize) {
        QueryWrapper<ZenDbDs> wrapper = new QueryWrapper<>();
        wrapper.eq("PK_CREATEDBY", UserContext.getUserId()).eq("DS", 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("NAME", keyword).or().like("HOST", keyword).or().like("DB_SCHEMA", keyword));
        }
        // 排序：收藏的排在前面（按收藏时间降序），未收藏的按创建时间降序
        wrapper.orderByDesc("DT_FAVORITE").orderByDesc("DT_CREATED");
        PageHelper.startPage(pageNum, pageSize);
        List<ZenDbDs> result = mapper.selectList(wrapper);
        PageInfo<ZenDbDs> pageInfo = new PageInfo<>(result);
        return PageResult.of(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    @Override
    public KvMap testConnection(ZenDbDs pixelDatasource) {
        return DataSourceConnectionUtils.testConnection(Jdbcs.buildJdbcUrl(pixelDatasource),
                pixelDatasource.getUserName(), pixelDatasource.getUserPwd());
    }

    @Override
    public void toggleFavorite(Long pkDs) {
        ZenDbDs ds = mapper.selectById(pkDs);
        if (ds == null || !ds.getPkCreatedby().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权操作该数据源");
        }
        
        // 使用 UpdateWrapper 明确设置 null 值
        UpdateWrapper<ZenDbDs> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("PK_DS", pkDs).eq("DS", 0);
        
        // 如果已收藏则取消，否则设置为收藏
        if (ds.getDtFavorite() != null) {
            updateWrapper.set("DT_FAVORITE", null);
        } else {
            updateWrapper.set("DT_FAVORITE", LocalDateTime.now());
        }
        updateWrapper.set("PK_MODIFIEDBY", UserContext.getUserId());
        updateWrapper.set("DT_MODIFIED", LocalDateTime.now());
        
        mapper.update(null, updateWrapper);
    }

    @Override
    public void updateTags(Long pkDs, String tags) {
        ZenDbDs ds = mapper.selectById(pkDs);
        if (ds == null || !ds.getPkCreatedby().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权操作该数据源");
        }
        ds.setTags(tags);
        ds.setPkModifiedby(UserContext.getUserId());
        ds.setDtModified(LocalDateTime.now());
        mapper.updateById(ds);
    }
}
