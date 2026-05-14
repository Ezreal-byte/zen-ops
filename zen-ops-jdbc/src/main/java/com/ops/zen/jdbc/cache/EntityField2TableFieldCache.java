package com.ops.zen.jdbc.cache;

import com.ops.zen.cache.BaseMemCache;
import com.ops.zen.jdbc.EntityHelper;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class EntityField2TableFieldCache extends BaseMemCache<Field, String> {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(EntityField2TableFieldCache.class);

    private volatile static EntityField2TableFieldCache _inst;

    private EntityField2TableFieldCache() {
        //size无限，过期时间无限
        super(Long.MAX_VALUE, Long.MAX_VALUE);
    }

    public static EntityField2TableFieldCache inst() {
        if (_inst == null) {
            synchronized (EntityField2TableFieldCache.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new EntityField2TableFieldCache();
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
    protected String load(Field key) throws Exception {
        return EntityHelper.mappingEntityFldName2TableFldName(key);
    }
}
