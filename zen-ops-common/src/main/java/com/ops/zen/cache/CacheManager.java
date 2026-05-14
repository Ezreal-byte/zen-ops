package com.ops.zen.cache;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author xyn
 * @Date 2025/04/12
 * @Description 缓存管理，和缓存清理计划管理
 */
public class CacheManager {

    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CacheManager.class);

    private Set<IMemCache<?, ?>> caches = new HashSet<>();

    /**
     * 有些缓存为一组，使用相同的CacheId，单独清理其中的一个或一部分会导致不可预知的错误（所以作为一组来清理）
     */
    private Multimap<String, CacheCleanPlan> cacheCleanPlans = HashMultimap.create();

    private volatile static CacheManager _inst;

    public static CacheManager inst() {
        if (_inst == null) {
            synchronized (CacheManager.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new CacheManager();
            }
        }
        return _inst;
    }

    public Set<IMemCache<?, ?>> getCaches() {
        return caches;
    }

    public void setCaches(Set<IMemCache<?, ?>> caches) {
        this.caches = caches;
    }

    public Multimap<String, CacheCleanPlan> getCacheCleanPlans() {
        return cacheCleanPlans;
    }

    public void setCacheCleanPlans(Multimap<String, CacheCleanPlan> cacheCleanPlans) {
        this.cacheCleanPlans = cacheCleanPlans;
    }

    /**
     * 接管缓存
     *
     * @param cache
     */
    public void push(IMemCache<?, ?> cache) {
        caches.add(cache);
    }

    /**
     * 接管缓存清理计划
     *
     * @param cache
     */
    public void pushCacheCleanPlan(CacheCleanPlan cache) {
        cacheCleanPlans.put(cache.getCacheID(), cache);
        if (cache instanceof IMemCache) {
            caches.add((IMemCache<?, ?>) cache);
        }

    }

    /**
     * 将所有由CacheManager管理下的缓存都清空
     */
    public void invalidateAll() {
        //for each v为非Collection，不会丢数据
        this.cacheCleanPlans.forEach((k, v) -> {
            if (v instanceof CacheCleanPlan) {
                if (!v.isInCleanAllCachePlan()) {
                    logger.warn("缓存【{} - {}】设置为清理全部时不清理该缓存", v.getCacheID(), v.toString());
                    return;
                }
            }
            logger.warn("缓存【{} - {}】将被清理（更新）", v.getCacheID(), v.toString());
            v.invalidateAll();
        });
    }

    /**
     * 通过cacheID精确的控制某个缓存的清理
     *
     * @param cacheId
     */
    public void invalidate(String cacheId) {
        //get结果不会为null
        this.cacheCleanPlans.get(cacheId).forEach(ccp -> {
            logger.warn("缓存【{} - {}】将被清理（更新）", cacheId, ccp.toString());
            ccp.invalidateAll();
        });
    }

    public void invalidate(String cacheId, String key) {
        //get结果不会为null
        this.cacheCleanPlans.get(cacheId).forEach(ccp -> {
            if (ccp instanceof BaseMemCache) {
                logger.warn("缓存【{} - {} - {}】将被清理（更新）", cacheId, ccp.toString(), key);
                ((BaseMemCache) ccp).invalidate(key);
            }
        });
    }
}
