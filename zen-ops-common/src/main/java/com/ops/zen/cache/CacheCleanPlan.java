package com.ops.zen.cache;

/**
 * @Author xyn
 * @Date 2025/04/12
 * @Description 实现接口，加入缓存清理计划
 */
public interface CacheCleanPlan {

    String getCacheID();

    void invalidateAll();

    /**
     * 当【清理所有缓存】接收到以后是否要清理掉缓存
     *
     * @return
     */
    default boolean isInCleanAllCachePlan() {
        return true;
    }

    /**
     * 是否纳入缓存清理计划，子类可以覆盖该方法
     *
     * @return
     */
    default boolean cacheClearPlan() {
        return true;
    }


}
