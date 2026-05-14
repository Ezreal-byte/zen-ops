package com.ops.zen.jdbc.cache;

import com.ops.zen.cache.BaseMemCache;
import com.ops.zen.utils.Reflect;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class EntityTypeFieldsCache extends BaseMemCache<Class<?>, Map<String, Field>> {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(EntityTypeFieldsCache.class);

    private volatile static EntityTypeFieldsCache _inst;

    private EntityTypeFieldsCache() {
        //size无限，过期时间无限
        super(Long.MAX_VALUE, Long.MAX_VALUE);
    }

    public static EntityTypeFieldsCache inst() {
        if (_inst == null) {
            synchronized (EntityTypeFieldsCache.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new EntityTypeFieldsCache();
            }
        }
        return _inst;
    }

    /**
     * 不加入缓存计划
     *
     * @return
     */
    @Override
    public boolean cacheClearPlan() {
        return false;
    }

    @Override
    protected Map<String, Field> load(Class<?> key) throws Exception {
        return Reflect.nameFields(key, true);
    }
}
