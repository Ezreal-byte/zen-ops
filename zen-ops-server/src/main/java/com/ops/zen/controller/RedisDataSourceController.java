package com.ops.zen.controller;

import com.ops.zen.entity.ZenRedisDs;
import com.ops.zen.mapper.ZenRedisDsMapper;
import com.ops.zen.service.impl.RedisKeyService;
import com.ops.zen.service.RedisDataSourceService;
import com.ops.zen.utils.RedisClientUtil;
import com.ops.zen.utils.UserContext;
import com.ops.zen.utils.map.KvMap;
import com.ops.zen.utils.pk.SnowPkGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Redis数据源管理
 * @Date 2026-05-06
 */
@RestController
@RequestMapping("/redis")
@Slf4j
public class RedisDataSourceController {

    @Autowired
    private RedisDataSourceService redisDataSourceService;

    @Autowired
    private ZenRedisDsMapper redisDataSourceMapper;

    @Autowired
    private RedisKeyService redisKeyService;

    @PostMapping("datasource/add")
    public String add(@RequestBody ZenRedisDs redisDataSource) {
        redisDataSource.setPkRedisDs(SnowPkGenerator.generateSnow());
        redisDataSource.setPkCreatedby(UserContext.getUserId());
        redisDataSource.setDtCreated(LocalDateTime.now());
        return redisDataSourceService.add(redisDataSource);
    }

    @GetMapping("datasource/delete/{pkRedisDs}")
    public String delete(@PathVariable Long pkRedisDs) {
        return redisDataSourceService.delete(pkRedisDs);
    }

    @PostMapping("datasource/update")
    public String update(@RequestBody ZenRedisDs redisDataSource) {
        redisDataSource.setPkModifiedby(UserContext.getUserId());
        redisDataSource.setDtModified(LocalDateTime.now());
        return redisDataSourceService.update(redisDataSource);
    }

    @GetMapping("datasource/get/{pkRedisDs}")
    public ZenRedisDs get(@PathVariable Long pkRedisDs) {
        return redisDataSourceService.get(pkRedisDs);
    }

    @GetMapping("datasource/list")
    public List<ZenRedisDs> list() {
        return redisDataSourceService.listByCurrentUser();
    }

    @GetMapping("datasource/test/{pkRedisDs}")
    public KvMap testConnection(@PathVariable Long pkRedisDs) {
        String error = redisDataSourceService.testConnection(pkRedisDs);
        KvMap result = new KvMap();
        if (error == null) {
            result.put("success", true);
            result.put("message", "连接成功");
        } else {
            result.put("success", false);
            result.put("message", error);
        }
        return result;
    }

    @PostMapping("datasource/test")
    public KvMap testConnectionWithTempData(@RequestBody ZenRedisDs ds) {
        String error = RedisClientUtil.testConnection(ds);
        KvMap result = new KvMap();
        if (error == null) {
            result.put("success", true);
            result.put("message", "连接成功");
        } else {
            result.put("success", false);
            result.put("message", error);
        }
        return result;
    }

    /**
     * 设置默认数据源（同时取消该用户其他数据源的默认状态）
     */
    @PostMapping("datasource/set-default")
    public String setDefault(@RequestParam Long pkRedisDs) {
        redisDataSourceService.setDefault(pkRedisDs);
        return null;
    }

    @GetMapping("db/list")
    public List<Map<String, Object>> listDatabases(@RequestParam Long pkRedisDs) {
        return redisKeyService.listDatabases(pkRedisDs);
    }

    @GetMapping("keys/list")
    public com.ops.zen.dto.RedisKeyListResponse listKeys(@RequestParam Long pkRedisDs,
                          @RequestParam Integer dbIndex,
                          @RequestParam(required = false, defaultValue = "*") String pattern,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false, defaultValue = "false") Boolean flat,
                          @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                          @RequestParam(required = false, defaultValue = "100") Integer pageSize) {
        return redisKeyService.listKeys(pkRedisDs, dbIndex, pattern, type, flat, pageNum, pageSize);
    }

    @GetMapping("key/detail")
    public KvMap getKeyDetail(@RequestParam Long pkRedisDs,
                              @RequestParam Integer dbIndex,
                              @RequestParam String key) {
        Map<String, Object> detail = redisKeyService.getKeyDetail(pkRedisDs, dbIndex, key);
        return new KvMap(detail);
    }

    @GetMapping("key/download")
    public void downloadKey(@RequestParam Long pkRedisDs,
                           @RequestParam Integer dbIndex,
                           @RequestParam String key,
                           HttpServletResponse response) throws Exception {
        String content = redisKeyService.getDownloadContent(pkRedisDs, dbIndex, key);
        String filename = "redis_" + key + ".txt";

        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        String encodedFilename = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
        response.setContentLength(content.getBytes("UTF-8").length);
        response.getWriter().write(content);
        response.getWriter().flush();
    }

    @GetMapping("key/delete")
    public String deleteKey(@RequestParam Long pkRedisDs,
                           @RequestParam Integer dbIndex,
                           @RequestParam String key) {
        redisKeyService.deleteKey(pkRedisDs, dbIndex, key);
        return null;
    }

    @PostMapping("key/edit")
    public String editKey(@RequestParam Long pkRedisDs,
                         @RequestParam Integer dbIndex,
                         @RequestParam String key,
                         @RequestParam String value,
                         @RequestParam(required = false, defaultValue = "0") Long ttl,
                         @RequestParam(required = false) String expiryTime) {
        redisKeyService.editKey(pkRedisDs, dbIndex, key, value, ttl, expiryTime);
        return null;
    }

    @PostMapping("key/add")
    public String addKey(@RequestParam Long pkRedisDs,
                        @RequestParam Integer dbIndex,
                        @RequestParam String key,
                        @RequestParam String value,
                        @RequestParam(required = false, defaultValue = "0") Long ttl,
                        @RequestParam(required = false) String expiryTime) {
        redisKeyService.addKey(pkRedisDs, dbIndex, key, value, ttl, expiryTime);
        return null;
    }

    @GetMapping("key/expiry")
    public String setKeyExpiry(@RequestParam Long pkRedisDs,
                              @RequestParam Integer dbIndex,
                              @RequestParam String key,
                              @RequestParam(required = false, defaultValue = "0") Long ttl,
                              @RequestParam(required = false) String expiryTime) {
        redisKeyService.setKeyExpiry(pkRedisDs, dbIndex, key, ttl, expiryTime);
        return null;
    }
}
