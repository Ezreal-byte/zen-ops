package com.ops.zen.service;

import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.utils.map.KvMap;
import com.ops.zen.utils.map.PageResult;

/**
 * @author xyn
 * @date 2025/4/9 20:44
 * @description
 **/
public interface DsDataSourceService {
    PageResult<ZenDbDs> queryByPage(String keyword, Integer pageNum, Integer pageSize);

    String add(ZenDbDs pixelDatasource);

    String delete(Long pkDs);

    String update(ZenDbDs pixelDatasource);

    ZenDbDs get(Long pkDs);

    /**
     * 测试是否已连通数据库
     * @param pixelDatasource
     * @return
     */
    KvMap testConnection(ZenDbDs pixelDatasource);

    /**
     * 收藏/取消收藏数据源
     */
    void toggleFavorite(Long pkDs);

    /**
     * 更新标签
     */
    void updateTags(Long pkDs, String tags);
}
