package com.ops.zen.controller;

import com.ops.zen.entity.ZenDbDs;
import com.ops.zen.service.DsDataSourceService;
import com.ops.zen.utils.map.KvMap;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
@RestController
@RequestMapping("/ds/datasource")
@Slf4j
public class DsDataSourceController {


    @Autowired
    private DsDataSourceService service;


    @PostMapping("add")
    public String add(@RequestBody ZenDbDs pixelDatasource) {
        return service.add(pixelDatasource);
    }

    @GetMapping("delete/{pkDs}")
    public String delete(@PathVariable Long pkDs) {
        return service.delete(pkDs);
    }

    @PostMapping("update")
    public String update(@RequestBody ZenDbDs pixelDatasource) {
        return service.update(pixelDatasource);
    }


    @GetMapping("get/{pkDs}")
    public ZenDbDs get(@PathVariable Long pkDs) {
        return service.get(pkDs);
    }


    @GetMapping("queryByPage")
    public PageResult<ZenDbDs> queryByPage(String keyword, Integer pageNum, Integer pageSize) {
        return service.queryByPage(keyword, pageNum, pageSize);
    }

    /**
     * 测试是否已连通数据库
     */
    @PostMapping("test/connection")
    public KvMap testConnection(@RequestBody ZenDbDs pixelDatasource) {
        return service.testConnection(pixelDatasource);
    }



    @PostMapping("test/connection/{pkDs}")
    public KvMap testConnection(@PathVariable Long pkDs) {
        ZenDbDs pixelDatasource = service.get(pkDs);
        return service.testConnection(pixelDatasource);
    }

    /**
     * 收藏/取消收藏数据源
     */
    @PostMapping("favorite/{pkDs}")
    public String toggleFavorite(@PathVariable Long pkDs) {
        service.toggleFavorite(pkDs);
        return null;
    }

    /**
     * 更新数据源标签
     */
    @PostMapping("tags/{pkDs}")
    public String updateTags(@PathVariable Long pkDs, @RequestParam String tags) {
        service.updateTags(pkDs, tags);
        return null;
    }

}
