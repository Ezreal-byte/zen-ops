package com.ops.zen.jdbc.sql;

import com.ops.zen.jdbc.AbstractConfigMap2;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class EasyVars extends AbstractConfigMap2<String, Object> {

    public EasyVars() {
        super(new HashMap<>());
    }

    public EasyVars(Map<String, Object> paramsMap) {
        super(paramsMap);
    }


    @Override
    public EasyVars put(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    @Override
    public Object getValue(String key) {
        return get(key);
    }
}
