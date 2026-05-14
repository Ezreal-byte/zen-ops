package com.ops.zen.service.impl;

import com.ops.zen.dto.RedisKeyDTO;
import com.ops.zen.dto.RedisKeyListResponse;
import com.ops.zen.entity.ZenRedisDs;
import com.ops.zen.mapper.ZenRedisDsMapper;
import com.ops.zen.util.FileSizeUtil;
import com.ops.zen.utils.RedisClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Redis Key操作Service
 * @Date 2026-05-06
 */
@Service
@Slf4j
public class RedisKeyService {

    @Autowired
    private ZenRedisDsMapper redisDataSourceMapper;

    /**
     * 获取数据库列表(动态获取数据库数量)
     */
    public List<Map<String, Object>> listDatabases(Long pkRedisDs) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        return RedisClientUtil.execute(ds, null, jedis -> {
            List<Map<String, Object>> dbList = new ArrayList<>();
            String info = jedis.info("keyspace");
            int dbCount = 0;

            if (info != null && !info.isEmpty()) {
                String[] lines = info.split("\n");
                for (String line : lines) {
                    if (line.startsWith("db")) {
                        dbCount++;
                    }
                }
            }

            if (dbCount == 0) {
                dbCount = 16;
            }

            for (int i = 0; i < dbCount; i++) {
                Map<String, Object> db = new HashMap<>();
                db.put("index", i);
                db.put("name", "db" + i);
                jedis.select(i);
                long dbSize = jedis.dbSize();
                db.put("size", dbSize);
                dbList.add(db);
            }

            return dbList;
        });
    }

    /**
     * 获取指定数据库的key列表（带分页支持）
     * 使用 SCAN 命令替代 KEYS，避免阻塞 Redis
     */
    public RedisKeyListResponse listKeys(Long pkRedisDs, Integer dbIndex, String pattern,
                                          String type, Boolean flat, 
                                          Integer pageNum, Integer pageSize) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        // 处理默认分页参数（必须在 lambda 外部，保证是 effectively final）
        final int finalPageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        final int finalPageSize = (pageSize == null || pageSize < 1) ? 100 : 
                                  (pageSize > 1000) ? 1000 : pageSize;

        return RedisClientUtil.execute(ds, dbIndex, jedis -> {
            // 使用 SCAN 命令分批获取 key
            Set<String> allKeySet = new HashSet<>();
            String cursor = "0";
            int count = 1000; // SCAN 每次扫描的数量
            
            do {
                ScanParams scanParams = new ScanParams();
                scanParams.match(pattern != null ? pattern : "*");
                scanParams.count(count);
                
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                List<String> keys = scanResult.getResult();
                allKeySet.addAll(keys);
                
                cursor = scanResult.getCursor();
            } while (!cursor.equals("0"));

            // 获取每个 key 的类型并过滤
            List<RedisKeyDTO> allKeys = new ArrayList<>();
            for (String key : allKeySet) {
                String keyType = jedis.type(key);

                if (type != null && !type.isEmpty() && !type.equals(keyType)) {
                    continue;
                }

                RedisKeyDTO keyInfo = buildKeyInfo(jedis, key, keyType);
                allKeys.add(keyInfo);
            }

            // 排序
            allKeys.sort(Comparator.comparing(RedisKeyDTO::getKey));

            // 构建树形或平铺结构
            List<RedisKeyDTO> structuredKeys;
            if (flat) {
                structuredKeys = allKeys;
            } else {
                structuredKeys = buildHierarchy(allKeys);
            }

            // 分页处理
            int total = structuredKeys.size();
            int fromIndex = (finalPageNum - 1) * finalPageSize;
            int toIndex = Math.min(fromIndex + finalPageSize, total);
            
            List<RedisKeyDTO> pageList;
            if (fromIndex >= total) {
                pageList = new ArrayList<>();
            } else {
                pageList = structuredKeys.subList(fromIndex, toIndex);
            }

            RedisKeyListResponse response = new RedisKeyListResponse();
            response.setList(pageList);
            response.setTotal(total);
            response.setPageNum(finalPageNum);
            response.setPageSize(finalPageSize);
            response.setPages((total + finalPageSize - 1) / finalPageSize);
            response.setRefreshTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return response;
        });
    }

    /**
     * 构建key信息
     */
    private RedisKeyDTO buildKeyInfo(redis.clients.jedis.Jedis jedis, String key, String keyType) {
        RedisKeyDTO keyInfo = new RedisKeyDTO();
        keyInfo.setKey(key);
        keyInfo.setType(keyType);
        keyInfo.setTtl(jedis.ttl(key));
        keyInfo.setIsParent(false);
        keyInfo.setExpanded(false);
        keyInfo.setTtlFormatFromSeconds();

        long keySize = 0;

        if ("string".equals(keyType)) {
            String value = jedis.get(key);
            keyInfo.setPreview(value != null && value.length() > 100 ? value.substring(0, 100) + "..." : value);
            keySize = value != null ? value.getBytes().length : 0;
        } else if ("hash".equals(keyType)) {
            long size = jedis.hlen(key);
            keyInfo.setPreview("Hash(" + size + " fields)");
            Map<String, String> hashValue = jedis.hgetAll(key);
            keySize = hashValue.entrySet().stream()
                .mapToLong(e -> e.getKey().getBytes().length + (e.getValue() != null ? e.getValue().getBytes().length : 0))
                .sum();
        } else if ("list".equals(keyType)) {
            long size = jedis.llen(key);
            keyInfo.setPreview("List(" + size + " items)");
            List<String> listValue = jedis.lrange(key, 0, -1);
            keySize = listValue.stream()
                .mapToLong(v -> v != null ? v.getBytes().length : 0)
                .sum();
        } else if ("set".equals(keyType)) {
            long size = jedis.scard(key);
            keyInfo.setPreview("Set(" + size + " members)");
            Set<String> setValue = jedis.smembers(key);
            keySize = setValue.stream()
                .mapToLong(v -> v != null ? v.getBytes().length : 0)
                .sum();
        } else if ("zset".equals(keyType)) {
            long size = jedis.zcard(key);
            keyInfo.setPreview("ZSet(" + size + " members)");
            Set<redis.clients.jedis.Tuple> zsetValue = jedis.zrangeWithScores(key, 0, -1);
            keySize = zsetValue.stream()
                .mapToLong(t -> t.getElement().getBytes().length)
                .sum();
        }

        keyInfo.setSize(keySize);
        keyInfo.setSizeText(FileSizeUtil.format(keySize));

        String[] parts = key.split(":");
        if (parts.length > 1) {
            keyInfo.setParent(parts[0]);
            keyInfo.setChild(String.join(":", Arrays.copyOfRange(parts, 1, parts.length)));
            keyInfo.setHasParent(true);
        } else {
            keyInfo.setParent(null);
            keyInfo.setChild(key);
            keyInfo.setHasParent(false);
        }

        return keyInfo;
    }

    /**
     * 构建层级结构
     */
    public List<RedisKeyDTO> buildHierarchy(List<RedisKeyDTO> allKeys) {
        Map<String, List<RedisKeyDTO>> parentMap = new LinkedHashMap<>();
        List<RedisKeyDTO> noParentKeys = new ArrayList<>();

        for (RedisKeyDTO key : allKeys) {
            if (key.getHasParent()) {
                parentMap.computeIfAbsent(key.getParent(), k -> new ArrayList<>()).add(key);
            } else {
                noParentKeys.add(key);
            }
        }

        List<RedisKeyDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<RedisKeyDTO>> entry : parentMap.entrySet()) {
            String parent = entry.getKey();
            List<RedisKeyDTO> children = entry.getValue();

            RedisKeyDTO parentNode = new RedisKeyDTO();
            parentNode.setKey(parent);
            parentNode.setParent(parent);
            parentNode.setIsParent(true);
            parentNode.setExpanded(false);
            parentNode.setChildCount(children.size());
            parentNode.setType(children.get(0).getType());
            parentNode.setPreview(children.size() + " 个子 key");

            long totalSize = children.stream().mapToLong(k -> k.getSize() != null ? k.getSize() : 0).sum();
            parentNode.setSize(totalSize);
            parentNode.setSizeText(FileSizeUtil.format(totalSize));
            parentNode.setTtl(-1L);
            parentNode.setTtlFormatFromSeconds();
            parentNode.setChildren(children);

            result.add(parentNode);
        }

        result.addAll(noParentKeys);

        return result;
    }

    /**
     * 获取key的详细信息
     */
    public Map<String, Object> getKeyDetail(Long pkRedisDs, Integer dbIndex, String key) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        return RedisClientUtil.execute(ds, dbIndex, jedis -> {
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("type", jedis.type(key));
            result.put("ttl", jedis.ttl(key));

            String type = jedis.type(key);
            final long SIZE_LIMIT = 500 * 1024;

            fillKeyValue(jedis, result, type, key, SIZE_LIMIT);

            return result;
        });
    }

    private void fillKeyValue(redis.clients.jedis.Jedis jedis, Map<String, Object> result,
                              String type, String key, long sizeLimit) {
        if ("string".equals(type)) {
            String value = jedis.get(key);
            if (value != null) {
                long byteSize = value.getBytes().length;
                if (byteSize > sizeLimit) {
                    result.put("value", null);
                    result.put("tooLarge", true);
                    result.put("size", byteSize);
                    result.put("sizeText", FileSizeUtil.format(byteSize));
                } else {
                    result.put("value", value);
                    result.put("tooLarge", false);
                }
            } else {
                result.put("value", null);
                result.put("tooLarge", false);
            }
        } else if ("hash".equals(type)) {
            Map<String, String> hashValue = jedis.hgetAll(key);
            long estimatedSize = hashValue.entrySet().stream()
                .mapToLong(e -> e.getKey().getBytes().length + (e.getValue() != null ? e.getValue().getBytes().length : 0))
                .sum();

            if (estimatedSize > sizeLimit) {
                result.put("value", null);
                result.put("tooLarge", true);
                result.put("size", estimatedSize);
                result.put("sizeText", FileSizeUtil.format(estimatedSize));
                result.put("fieldCount", hashValue.size());
            } else {
                result.put("value", hashValue);
                result.put("tooLarge", false);
            }
        } else if ("list".equals(type)) {
            List<String> listValue = jedis.lrange(key, 0, -1);
            long estimatedSize = listValue.stream()
                .mapToLong(v -> v != null ? v.getBytes().length : 0)
                .sum();

            if (estimatedSize > sizeLimit) {
                result.put("value", null);
                result.put("tooLarge", true);
                result.put("size", estimatedSize);
                result.put("sizeText", FileSizeUtil.format(estimatedSize));
                result.put("itemCount", listValue.size());
            } else {
                result.put("value", listValue);
                result.put("tooLarge", false);
            }
        } else if ("set".equals(type)) {
            Set<String> setValue = jedis.smembers(key);
            long estimatedSize = setValue.stream()
                .mapToLong(v -> v != null ? v.getBytes().length : 0)
                .sum();

            if (estimatedSize > sizeLimit) {
                result.put("value", null);
                result.put("tooLarge", true);
                result.put("size", estimatedSize);
                result.put("sizeText", FileSizeUtil.format(estimatedSize));
                result.put("memberCount", setValue.size());
            } else {
                result.put("value", setValue);
                result.put("tooLarge", false);
            }
        } else if ("zset".equals(type)) {
            Set<redis.clients.jedis.Tuple> zsetValue = jedis.zrangeWithScores(key, 0, -1);
            long estimatedSize = zsetValue.stream()
                .mapToLong(t -> t.getElement().getBytes().length)
                .sum();

            if (estimatedSize > sizeLimit) {
                result.put("value", null);
                result.put("tooLarge", true);
                result.put("size", estimatedSize);
                result.put("sizeText", FileSizeUtil.format(estimatedSize));
                result.put("memberCount", zsetValue.size());
            } else {
                result.put("value", zsetValue);
                result.put("tooLarge", false);
            }
        }
    }

    /**
     * 删除key
     */
    public void deleteKey(Long pkRedisDs, Integer dbIndex, String key) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        RedisClientUtil.executeVoid(ds, dbIndex, jedis -> {
            jedis.del(key);
            return null;
        });
    }

    /**
     * 编辑String类型的key值
     */
    public void editKey(Long pkRedisDs, Integer dbIndex, String key, String value, Long ttl, String expiryTime) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        RedisClientUtil.executeVoid(ds, dbIndex, jedis -> {
            String type = jedis.type(key);
            if (!"string".equals(type)) {
                throw new RuntimeException("只能编辑String类型的key");
            }
            jedis.set(key, value);
            setExpiryTime(jedis, key, ttl, expiryTime);
            return null;
        });
    }

    /**
     * 新增Key（仅支持String类型）
     */
    public void addKey(Long pkRedisDs, Integer dbIndex, String key, String value, Long ttl, String expiryTime) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        RedisClientUtil.executeVoid(ds, dbIndex, jedis -> {
            jedis.set(key, value);
            setExpiryTime(jedis, key, ttl, expiryTime);
            return null;
        });
    }

    /**
     * 修改key的过期时间
     */
    public void setKeyExpiry(Long pkRedisDs, Integer dbIndex, String key, Long ttl, String expiryTime) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        RedisClientUtil.executeVoid(ds, dbIndex, jedis -> {
            if (ttl != null && ttl == -1) {
                jedis.persist(key);
            } else {
                setExpiryTime(jedis, key, ttl, expiryTime);
            }
            return null;
        });
    }

    /**
     * 设置过期时间
     */
    private void setExpiryTime(redis.clients.jedis.Jedis jedis, String key, Long ttl, String expiryTime) {
        if (expiryTime != null && !expiryTime.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date expiryDate = sdf.parse(expiryTime);
                Date now = new Date();
                if (expiryDate.after(now)) {
                    long seconds = (expiryDate.getTime() - now.getTime()) / 1000;
                    jedis.expire(key, (int) seconds);
                }
            } catch (Exception e) {
                throw new RuntimeException("日期格式错误，请使用yyyy-MM-dd HH:mm:ss格式");
            }
        } else if (ttl != null && ttl > 0) {
            jedis.expire(key, ttl.intValue());
        }
    }

    /**
     * 获取key的下载内容
     */
    public String getDownloadContent(Long pkRedisDs, Integer dbIndex, String key) {
        ZenRedisDs ds = redisDataSourceMapper.selectById(pkRedisDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }

        return RedisClientUtil.execute(ds, dbIndex, jedis -> {
            String type = jedis.type(key);
            String content = "";

            if ("string".equals(type)) {
                content = jedis.get(key);
            } else if ("hash".equals(type)) {
                Map<String, String> hashValue = jedis.hgetAll(key);
                StringBuilder sb = new StringBuilder();
                sb.append("Hash Key: ").append(key).append("\n");
                sb.append("Fields: ").append(hashValue.size()).append("\n\n");
                for (Map.Entry<String, String> entry : hashValue.entrySet()) {
                    sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
                }
                content = sb.toString();
            } else if ("list".equals(type)) {
                List<String> listValue = jedis.lrange(key, 0, -1);
                StringBuilder sb = new StringBuilder();
                sb.append("List Key: ").append(key).append("\n");
                sb.append("Items: ").append(listValue.size()).append("\n\n");
                for (int i = 0; i < listValue.size(); i++) {
                    sb.append("[").append(i).append("] ").append(listValue.get(i)).append("\n");
                }
                content = sb.toString();
            } else if ("set".equals(type)) {
                Set<String> setValue = jedis.smembers(key);
                StringBuilder sb = new StringBuilder();
                sb.append("Set Key: ").append(key).append("\n");
                sb.append("Members: ").append(setValue.size()).append("\n\n");
                int index = 0;
                for (String value : setValue) {
                    sb.append("[").append(index++).append("] ").append(value).append("\n");
                }
                content = sb.toString();
            } else if ("zset".equals(type)) {
                Set<redis.clients.jedis.Tuple> zsetValue = jedis.zrangeWithScores(key, 0, -1);
                StringBuilder sb = new StringBuilder();
                sb.append("ZSet Key: ").append(key).append("\n");
                sb.append("Members: ").append(zsetValue.size()).append("\n\n");
                int index = 0;
                for (redis.clients.jedis.Tuple tuple : zsetValue) {
                    sb.append("[").append(index++).append("] ").append(tuple.getElement())
                      .append(" (score: ").append(tuple.getScore()).append(")\n");
                }
                content = sb.toString();
            }

            return content;
        });
    }
}
