package com.ops.zen.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.ops.zen.utils.map.KvMap;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;

/**
 * @Author xiaoyingnan
 * @Date 2020/9/27 13:49
 * @Description
 */
public class JsonUtils {

    /*

 避免long作为数字类型传到浏览器中后导致js表达的数字型长度溢出
 提成静态成员变量-避免发生FULL GC - https://blog.csdn.net/qq_58680364/article/details/122436289
                                 https://zhuanlan.zhihu.com/p/365292971
 */
    static SerializeConfig avoidLongOverflowSerializeConfig = new SerializeConfig();

    static {
        avoidLongOverflowSerializeConfig.put(BigInteger.class, ToStringSerializer.instance);
        avoidLongOverflowSerializeConfig.put(Long.class, ToStringSerializer.instance);
        avoidLongOverflowSerializeConfig.put(Long.TYPE, ToStringSerializer.instance);
    }

    public static String toJSONStringWithNullValue(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
    }
    public static String toJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj);
    }

    public static String toJSONString(Object obj, boolean beauty) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj, beauty);
    }

    public static String toJSONStringBeauty(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj, true);
    }

    public static <T> List<T> toJavaList(Object o, Class<T> clazz) {
        if (o instanceof String) {
            o = JSONArray.parseArray(o.toString());
        }
        if (o instanceof JSONArray) {
            return ((JSONArray) o).toJavaList(clazz);
        } else {
            throw new RuntimeException("类型错误");
        }
    }

    public static <T> T toObject(Class<? extends T> targetClass, String body) {
        JSONObject json = JSON.parseObject(body);
        if (targetClass.equals(KvMap.class)) {
            return (T) new KvMap(json);
        }
        return JSON.toJavaObject(json, targetClass);
    }

    public static  <T> T toJavaObject(Type type, String body) {
        JSONObject json = JSON.parseObject(body);
        return json.toJavaObject(type);
    }

    /**
     * body 支持类型JSONObject或String
     *
     * @param targetClass
     * @param body
     * @param <T>
     * @return
     */
    public static <T> T toObjectFrom(Class<? extends T> targetClass, Object body) {
        JSONObject json = null;
        if (body == null) {
            return null;
        }
        if (body instanceof JSONObject) {
            json = (JSONObject) body;
        } else if (body instanceof String) {
            json = JSON.parseObject((String) body);
            if (targetClass.equals(KvMap.class)) {
                return (T) new KvMap(json);
            }
        } else {
            throw new RuntimeException();
        }
        return JSON.toJavaObject(json, targetClass);
    }

    public static String beautify(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        JSONObject jsonObject = JSON.parseObject(str);
        return jsonObject.toString(SerializerFeature.PrettyFormat);
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        return JSONArray.parseArray(text, clazz);
    }

    public static Object[] toArray(String strValue) {
        return JSON.parseArray(strValue).toArray();
    }

//    public static PfObjectArray parsePfObjectArray(String text) {
//        if (StringUtils.isEmpty(text)) {
//            return null;
//        }
//        List<PfObject> pfObjects = JSONArray.parseArray(text, PfObject.class);
//        return new PfObjectArray(pfObjects);
//    }
//
//    public static PfObject parsePfObject(String str) {
//        return new PfObject(str);
//    }

    public static String toJsonStringAvoidLongOverflow(String jsonstr) {
        JSONObject jsonObject = JSON.parseObject(jsonstr);
        String jsonRt = JSON.toJSONString(jsonObject, avoidLongOverflowSerializeConfig, SerializerFeature.WriteMapNullValue);
        return jsonRt;
    }

}
