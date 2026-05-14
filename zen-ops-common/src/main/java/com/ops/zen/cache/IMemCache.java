package com.ops.zen.cache;

import java.util.concurrent.ConcurrentMap;

/**
 * @Author xyn
 * @Date 2025/04/12
 * @Description
 */
public interface IMemCache<K, V> extends CacheCleanPlan {

    // V load(K key) throws Exception;

    V get(K key);

    void cleanUp();

    void refresh(K key);

    /**
     * 如果有缓存的数据返回缓存数据，如果没有返回空
     * @param Key
     * @return
     */
    V getIfPresent(K Key);

    void put(K key, V value);

    void invalidate(K key);

    ConcurrentMap<K, V> asMap();
}
