package com.ops.zen.utils;


import java.io.*;
import java.util.*;

/**
 * @author Ezreal
 * @version 2020/7/30 16:59
 * <文件说明>
 **/
public class PropertiesUtils {

    /**
     * @param str
     * @return 返回LinkedHashMap
     * @throws IOException
     */
    public static Map<String, Object> strToHashMap(String str) throws IOException {
        if (StringUtils.isEmpty(str)) {
            return new LinkedHashMap<>();
        }
        // @edit on 2023/7/12 使用缓冲流BufferedReader 解决properties 中文乱码问题
        InputStream inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"));
        BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        OrderedProperties properties = new OrderedProperties();
        properties.load(bf);
        IOUtils.close(inputStream);
        Map<String, Object> map = new LinkedHashMap<>();
        Set<String> names = properties.stringPropertyNames();
        for (String key : names) {
            map.put(key, properties.get(key));
        }
        return map;
    }

    /**
     * 出错抛出运行时异常
     *
     * @param str
     * @return 返回LinkedHashMap
     */
    public static Map<String, String> strToHashMapWithRtEx(String str) {
        try {
            Map<String, String> rt = new LinkedHashMap<>();
            Map<String, Object> objectMap = strToHashMap(str);
            objectMap.entrySet().forEach(ey -> {
                rt.put(ey.getKey(), ey.getValue() != null ? ey.getValue().toString() : null);
            });
            return rt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(Map<String, String> extMap) {
        if (extMap == null) {
            return null;
        }
        // 校验
        for (Map.Entry<String, String> kv : extMap.entrySet()) {
            Assert.isTrue(kv.getKey() != null && kv.getValue() != null, "key：%s或value：%s都不能为空", kv.getKey(), kv.getValue());
        }
        try {
            OrderedProperties p = new OrderedProperties();
            p.putAll(extMap);
            StringWriter writer = new StringWriter();
            p.store(writer, null);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static class OrderedProperties extends Properties {

        private static final long serialVersionUID = -4627607243846121965L;

        private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

        public Enumeration<Object> keys() {
            return Collections.<Object>enumeration(keys);
        }

        public Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }

        public Set<Object> keySet() {
            return keys;
        }

        public Set<String> stringPropertyNames() {
            Set<String> set = new LinkedHashSet<String>();

            for (Object key : this.keys) {
                set.add((String) key);
            }

            return set;
        }
    }

}
