package com.ops.zen.utils;

import com.ops.zen.entity.ZenRedisDs;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.function.Function;

/**
 * Redis连接工具类
 * @Date 2026-05-06
 */
@Slf4j
public class RedisClientUtil {

    /**
     * 执行Redis操作（自动管理连接）
     */
    public static <T> T execute(ZenRedisDs ds, Integer dbIndex, Function<Jedis, T> action) {
        Jedis jedis = null;
        try {
            jedis = createJedis(ds);
            if (dbIndex != null) {
                jedis.select(dbIndex);
            }
            return action.apply(jedis);
        } catch (Exception e) {
            log.error("Redis操作失败", e);
            throw new RuntimeException("Redis操作失败: " + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 执行Redis操作（无返回值）
     */
    public static void executeVoid(ZenRedisDs ds, Integer dbIndex, Function<Jedis, Void> action) {
        execute(ds, dbIndex, action);
    }

    /**
     * 创建Jedis连接
     */
    private static Jedis createJedis(ZenRedisDs ds) {
        Jedis jedis = new Jedis(
            ds.getHost(),
            Integer.parseInt(ds.getPort()),
            ds.getTimeout() != null ? ds.getTimeout() : 3000
        );

        // 如果有密码，设置密码
        if (ds.getPassword() != null && !ds.getPassword().isEmpty()) {
            jedis.auth(ds.getPassword());
        }

        return jedis;
    }

    /**
     * 测试连接
     */
    public static String testConnection(ZenRedisDs ds) {
        Jedis jedis = null;
        try {
            jedis = createJedis(ds);
            jedis.select(0);
            String pong = jedis.ping();
            if ("PONG".equals(pong)) {
                return null; // 连接成功
            } else {
                return "连接失败: " + pong;
            }
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
            return "连接失败: " + e.getMessage();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
