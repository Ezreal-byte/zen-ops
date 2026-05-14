package com.ops.zen.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/12
 * @Description
 */
public class FIFOMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    /**
     * 最大容量
     */
    private int maxCapacity;

    public FIFOMap(int maxCapacity) {
        super((int) Math.ceil(maxCapacity / 0.75) + 1, 0.75f, false);//目的是不扩容
        this.maxCapacity = maxCapacity;
    }


    /**
     * 容量超过限制（maxCapacity），删除最老的entry
     *
     * @param eldest
     * @return
     */
    @Override
    public boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }
}
