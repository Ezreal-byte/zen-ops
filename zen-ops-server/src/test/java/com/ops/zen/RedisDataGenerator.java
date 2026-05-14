package com.ops.zen;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 向Redis批量写入1万个key，用于测试分页效率
 */
public class RedisDataGenerator {

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 6379;
        String password = "123456";
        int count = 10000;

        Jedis jedis = new Jedis(host, port);
        jedis.auth(password);

        System.out.println("连接Redis成功，开始写入 " + count + " 个key...");

        long start = System.currentTimeMillis();

        Pipeline pipeline = jedis.pipelined();
        for (int i = 1; i <= count; i++) {
            String key = "zen:test:key:" + String.format("%05d", i);
            String value = "value_" + i + "_data_" + System.nanoTime();
            pipeline.set(key, value);
            // 每1000条同步一次
            if (i % 1000 == 0) {
                pipeline.sync();
                System.out.println("已写入: " + i + " / " + count);
            }
        }
        // 剩余的也同步
        pipeline.sync();

        long cost = System.currentTimeMillis() - start;
        System.out.println("写入完成! 共 " + count + " 个key，耗时: " + cost + "ms");

        // 验证
        long dbSize = jedis.dbSize();
        System.out.println("当前Redis key总数: " + dbSize);

        jedis.close();
    }
}
