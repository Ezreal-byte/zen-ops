package com.ops.zen.utils.map;




import com.ops.zen.utils.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/16 15:01
 * @Description
 */
public class KvMap implements Map<String, Object> {

    private Map<String, Object> innerMap = null;

    public KvMap(Map<String, Object> innerMap) {
        this.innerMap = innerMap;
    }

    public KvMap() {
        this.innerMap = new HashMap<>();
    }

    public KvMap put(String key, String value) {
        put(key, (Object) value);//强转，避免死循环 StackOverFlow
        return this;
    }

    public String getString(String key) {
        Object o = get(key);
        return o != null ? o.toString() : null;
    }

    public Long getLong(String key) {
        String v = getString(key);
        if (StringUtils.isNotEmpty(v)) {
            return Long.parseLong(v);
        } else {
            return null;
        }
    }

    public Boolean getBoolean(String key) {
        String v = getString(key);
        if (StringUtils.isNotEmpty(v)) {
            return Boolean.parseBoolean(v);
        } else {
            return null;
        }
    }

    public Integer getInteger(String key) {
        Object o = get(key);
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o != null) {
            return Integer.parseInt(o.toString());
        } else {
            return null;
        }
    }

    public BigDecimal getBigDecimal(String key) {
        Object o = get(key);
        if (o instanceof BigDecimal) {
            return (BigDecimal) o;
        } else if (o != null) {
            return new BigDecimal(o.toString());
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return innerMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return innerMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return innerMap.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return innerMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return innerMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        innerMap.putAll(m);
    }

    @Override
    public void clear() {
        innerMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return innerMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return innerMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return innerMap.entrySet();
    }
}
