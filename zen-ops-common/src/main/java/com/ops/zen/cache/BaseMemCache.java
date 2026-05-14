package com.ops.zen.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @Author xyn
 * @Date 2025/04/12
 * @Description
 */
public abstract class BaseMemCache<K, V> implements IMemCache<K, V> {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(BaseMemCache.class);

    private LoadingCache<K, V> loadingCache;

    public BaseMemCache() {
        this(10000, 1 * 60);
    }

    /**
     * @param maxSize
     * @param expireTime 单位分钟
     */
    public BaseMemCache(long maxSize, long expireTime) {
        this(maxSize, expireTime, TimeUnit.MINUTES);
    }

    /**
     * @param maxSize
     * @param expireTime
     * @param tu
     * @param removalListener 缓存过期以后清理时触发的监听
     */
    public BaseMemCache(long maxSize, long expireTime, TimeUnit tu, BiConsumer<K, V> removalListener) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().expireAfterAccess(expireTime, tu).maximumSize(maxSize);
        if (removalListener != null) {
            builder.removalListener(t -> {
                removalListener.accept((K) t.getKey(), (V) t.getValue());
            });
        }
        this.loadingCache = builder.build(new CacheLoader<K, V>() {

            @Override
            public V load(K key) throws Exception {
                if (key != null) {
                    // 同步-加锁，保证load方法的线程安全性
                    Object lock = null;
                    if (key instanceof String) {
                        lock = ((String) key).intern();
                    } else if (key instanceof Number) {
                        lock = key.toString().intern();
                    } else {
                        lock = key;
                    }
                    synchronized (lock) {
                        return BaseMemCache.this.load(key);
                    }
                } else {
                    return BaseMemCache.this.load(key);
                }
            }
        });
        CacheManager.inst().push(this);
        if (cacheClearPlan()) {
            CacheManager.inst().pushCacheCleanPlan(this);
        }
    }

    public BaseMemCache(long maxSize, long expireTime, TimeUnit tu) {
        this(maxSize, expireTime, tu, null);
    }


    /**
     * 唯一标识该cache，用来准确的清空某个cache
     *
     * @return
     */
    public String getCacheID() {
        return this.getClass().getName();
    }

    protected abstract V load(K key) throws Exception;

    @Override
    public V get(K key) {
        try {
            return loadingCache.get(key);
        } catch (Exception e) {
            logger.error(String.format("获取缓存异常className【%s】-cacheId【%s】-key【%s】", getClass().getName(), getCacheID(), key), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 清空失效的缓存
     */
    @Override
    public void cleanUp() {
        loadingCache.cleanUp();
    }

    /**
     * 使缓存失效，all
     */
    @Override
    public void invalidateAll() {
        loadingCache.invalidateAll();
    }

    /**
     * 使缓存失效，all
     */
    public void invalidateAllInner() {
        loadingCache.invalidateAll();
    }

    /**
     * 更新key对应的缓存
     */
    @Override
    public void refresh(K key) {
        loadingCache.refresh(key);
    }

    @Override
    public V getIfPresent(K key) {
        return loadingCache.getIfPresent(key);
    }

    @Override
    public void put(K key, V value) {
        loadingCache.put(key, value);
    }

    @Override
    public void invalidate(K key) {
        loadingCache.invalidate(key);
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return loadingCache.asMap();
    }

}
