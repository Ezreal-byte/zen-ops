package com.ops.zen.jdbc.cache;

import com.ops.zen.cache.BaseMemCache;
import com.ops.zen.jdbc.annotation.EntityFieldWrapper;
import com.ops.zen.jdbc.mixed.AnnotationMixed;
import com.ops.zen.utils.Reflect;
import com.ops.zen.utils.StringUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析key中存在注解的字段为map：<br>
 * 【数据库字段1（小写），数据库字段1（小写）】<br>
 * 【实体字段1（原始），数据库字段1（小写）】<br>
 * <pre>
 *     {@literal @}EntityField(name="FLD1_TEST")
 *      private int fld1Test;
 *      上面的字段会被解析为
 *      【fld1_test，fld1_test】
 *      【fld1Test，fld1_test】
 * </pre>
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class WhereName2TableFieldCache extends BaseMemCache<Class<?>, Map<String, String>> {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(WhereName2TableFieldCache.class);

    private volatile static WhereName2TableFieldCache _inst;

    private WhereName2TableFieldCache() {
        //size无限，过期时间无限
        super(Long.MAX_VALUE, Long.MAX_VALUE);
    }

    public static WhereName2TableFieldCache inst() {
        if (_inst == null) {
            synchronized (WhereName2TableFieldCache.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new WhereName2TableFieldCache();
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

    /**
     * @param key
     * @return
     * @throws Exception
     */
    @Override
    protected Map<String, String> load(Class<?> key) throws Exception {
        Map<String, Field> fields = Reflect.nameFields(key, true);
        Map<String, String> mapping = new HashMap<>();
        fields.forEach((k, v) -> {
            // EntityField annotation = v.getAnnotation(EntityField.class);
            EntityFieldWrapper annotation = AnnotationMixed.getEntityField(v);
            if (annotation != null) {//必须有注解
                String name = annotation.name();
                if (StringUtils.isNotEmpty(name) && !name.equals(k)) {
                    mapping.put(name.toLowerCase(), name.toLowerCase());//数据库字段使用小写
                }
                mapping.put(k, name.toLowerCase());
            }
        });
        return mapping;
    }
}
