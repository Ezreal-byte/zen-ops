package com.ops.zen.jdbc;


import com.ops.zen.utils.Assert;
import com.ops.zen.utils.map.KvMap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单记录，sql查询直接映射为该对象，
 * 字段名（key）全部小写
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class EasyRecord extends KvMap {

    public EasyRecord() {
        super();
    }

    @Override
    public KvMap put(String key, String value) {
        return super.put(key.toLowerCase(), value);
    }

    @Override
    public Object get(Object key) {
        return super.get(key.toString().toLowerCase());
    }

    @Override
    public Object put(String key, Object value) {
        return super.put(key.toLowerCase(), value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        Map<String, Object> m2 = new HashMap<>();
        m.forEach((k, v) -> {
            m2.put(k.toLowerCase(), v);
        });
        super.putAll(m2);
    }

    public Date getDate(String key) {
        Object o = get(key);
        if (o == null) {
            return null;
        }
        Assert.isTrue(o instanceof Date, "非Date类型" + o.getClass());
        return (Date) o;
    }

    public byte[] getBytes(String key) {
        Object o = get(key);
        if (o == null) {
            return null;
        }
        Assert.isTrue(o instanceof byte[], "非Date类型" + o.getClass());
        return (byte[]) o;
    }
}
