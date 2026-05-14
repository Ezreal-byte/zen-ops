package com.ops.zen.utils.xlsx;

import com.ops.zen.utils.map.KvMap;

import java.util.LinkedHashMap;

/**
 * @Author xiaoyingnan
 * @Date 2021/3/30 17:15
 * @Description
 */
public class XRow extends LinkedHashMap<String, XCell> {

    public KvMap toMap(){
        KvMap map = new KvMap();
        this.forEach((k, v) -> {
            map.put(k, v == null ? v : v.getValue());
        });
        return map;
    }
}
