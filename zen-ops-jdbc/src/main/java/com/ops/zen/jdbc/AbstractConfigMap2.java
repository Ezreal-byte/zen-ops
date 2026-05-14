package com.ops.zen.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * key,value配置类
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public abstract class AbstractConfigMap2<K, V> {

    protected Map<K, V> map = null;

    public AbstractConfigMap2(Map<K, V> map) {
        if (map == null) {
            this.map = new HashMap<>();
        } else {
            this.map = map;
        }
    }

    public AbstractConfigMap2() {
        this.map = new HashMap<>();
    }

    public AbstractConfigMap2 put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Object get(K key) {
        return map.get(key);
    }

    /**
     * @param key key全部小写
     * @return
     */
    public String getString(K key) {
        Object o = getValue(key);
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    public String getString(K key, String defaultValue) {
        Object o = getValue(key);
        if (o != null) {
            return o.toString();
        } else {
            return defaultValue;
        }
    }

    public abstract V getValue(K key);

    public Long getLong(K key) {
        Object o = getValue(key);
        if (o instanceof Long) {
            return (Long) o;
        } else if (o != null) {
            return Long.parseLong(o.toString());
        } else {
            return null;
        }
    }

    public Double getDouble(K key) {
        Object o = getValue(key);
        if (o instanceof Double) {
            return (Double) o;
        } else if (o != null) {
            return Double.parseDouble(o.toString());
        } else {
            return null;
        }
    }

    public Float getFloat(K key) {
        Object o = getValue(key);
        if (o instanceof Float) {
            return (Float) o;
        } else if (o != null) {
            return Float.parseFloat(o.toString());
        } else {
            return null;
        }
    }

    public Float getFloat(K key, Float defaultValue) {
        Float o = getFloat(key);
        if (o != null) {
            return o;
        } else {
            return defaultValue;
        }
    }

    public Boolean getBoolean(K key, Boolean defaultValue) {
        String o = getString(key);
        if (o != null) {
            return Boolean.valueOf(o);
        } else {
            return defaultValue;
        }
    }

    public Integer getInt(K key) {
        Object o = getValue(key);
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o != null) {
            return Integer.parseInt(o.toString());
        } else {
            return null;
        }
    }

    public Integer getInt(K key, Integer defaultValue) {
        Integer o = getInt(key);
        if (o != null) {
            return o;
        } else {
            return defaultValue;
        }
    }

    public String toString() {
        return this.map.toString();
    }

    public Map<K, V> toMap() {
        return map;
    }

    /**
     * map可以被序列化
     *
     * @return
     */
    public Map<K, V> getMap() {
        return map;
    }
}
