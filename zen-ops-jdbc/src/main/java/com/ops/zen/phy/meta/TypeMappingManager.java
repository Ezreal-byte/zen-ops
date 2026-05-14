package com.ops.zen.phy.meta;


import com.ops.zen.cache.Pair;
import com.ops.zen.utils.Reflects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2021/7/12 15:56
 * @Description
 */
public class TypeMappingManager {

    /**
     * 2[NUMERIC]=fullClassName[shortName]<br>
     * key:2<br>
     * 2[NUMERIC(10,5)]=fullClassName[shortName]<br>
     * key:2(10,5)
     */
    private Map<String, Pair<String, ? extends Class<?>>> typeMap = new HashMap<>();

    public TypeMappingManager() throws IOException {
        init(Reflects.asString(TypeMappingManager.class, "sqltype.javatype.mapping.cfg"));
    }

    public void init(String cfgContent) throws IOException {
        // 读取配置文件，初始化typeMap
        BufferedReader sr = new BufferedReader(new StringReader(cfgContent));
        String line = null;
        while ((line = sr.readLine()) != null) {
            // 2[NUMERIC]=fullClassName[shortName]
            // 2[NUMERIC(10,5)]=fullClassName[shortName]
            String[] split = line.split("=");
            String key = split[0];
            String value = split[1];

            char[] chars = key.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char aChar : chars) {
                if (aChar == '【' || aChar == '】' || (aChar >= 'A' && aChar <= 'Z') || aChar == '_') {
                    continue;
                } else {
                    sb.append(aChar);
                }
            }
            String fullClassName = value.substring(0, value.indexOf("【"));
            String shortClassName = value.substring(value.indexOf("【") + 1, value.indexOf("】"));
            Class<?> aClass = loadClass(shortClassName, fullClassName);
            typeMap.put(sb.toString(), new Pair<>(shortClassName, aClass));
        }
        System.out.println(typeMap);
    }

    public Class<?> loadClass(String shortClassName, String fullClassName) {
        try {
            if ("byte[]".equals(shortClassName)) {
                return byte[].class;
            }
            return Reflects.loadClass(fullClassName);
        } catch (ClassNotFoundException e) {
            System.err.printf(String.format("类型转换，找不到类%s\r\n", fullClassName));
            return null;
        }
    }

    public Pair<String, ? extends Class<?>> typeMapping(int columnType, int scale, int precision) {
        String key1 = String.format("%d(%d,%d)", columnType, precision, scale);
        String key2 = String.format("%d", columnType);
        Pair<String, ? extends Class<?>> o = typeMap.get(key1);
        o = o == null ? typeMap.get(key2) : null;
        if (o == null) {
            throw new RuntimeException(String.format("%s和%s都没有找到类型映射", key1, key2));
        } else {
            return o;
        }
    }
}
