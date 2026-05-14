package com.ops.zen.utils.en;

import com.ops.zen.utils.Reflect;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.ex.Exceptions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 枚举的工具类，要求枚举使用interface实现，枚举值使用@EnumDescription注解
 *
 * @Author xiaoyingnan
 * @Date 2020/7/27 15:24
 * @Description
 */
public class EnUtils {

    /**
     * SelectModel模型缓存
     */
    private static Map<Class<?>, List<SelectModel>> selectModelsCache = new ConcurrentHashMap<>();

    /**
     * 缓存枚举类的value->des
     */
    private static Map<Class<?>, Map<String, SelectModel>> enValueDes = new ConcurrentHashMap<>();

    /**
     * TODO 缓存？
     *
     * @param enClazz
     * @return 【en value，SelectModel】
     */
    public static Map<String, SelectModel> toSelectModelsMap(Class<?> enClazz) {
        Map<String, SelectModel> mmap = new HashMap<>();
        toSelectModels(enClazz).forEach(m -> {
            mmap.put(m.getValue(), m);
        });
        return mmap;
    }

    /**
     * 返回枚举类中的所有枚举只model列表，按名称排序
     *
     * @param enClazz
     * @return
     */
    public static List<SelectModel> toSelectModels(Class<?> enClazz) {
        List<SelectModel> selectModels = selectModelsCache.get(enClazz);
        if (selectModels != null) {
            return selectModels;
        }
        synchronized (enClazz) {
            selectModels = selectModelsCache.get(enClazz);
            if (selectModels != null) {
                return selectModels;
            }
            Map<String, Field> fields = Reflect.onClass(enClazz).nameFields();
            selectModels = new ArrayList<>();
            List<SelectModel> finalSelectModels = selectModels; //for lamda inner final
            fields.forEach((key, field) -> {
                try {
                    int modifiers = field.getModifiers();
                    //非public和static的过滤掉
                    if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))) {
                        return;
                    }
                    Object fieldValue = field.get(enClazz);//得到static final的值
                    EnumDescription annotation = field.getAnnotation(EnumDescription.class);
                    SelectModel sm = new SelectModel(
                            annotation != null ? annotation.remark() : null,
                            fieldValue != null ? fieldValue.toString() : null,
                            annotation != null ? annotation.group() : null,
                            annotation != null ? annotation.extType() : null,
                            annotation != null ? annotation.name() : null,
                            annotation != null ? annotation.extDefaultValue() : null);

                    finalSelectModels.add(sm);
                } catch (IllegalAccessException e) {
                    Exceptions.throwAsRuntimeException(e);
                }
            });
            // 在枚举类中字段顺序，按名称排序
            Collections.sort(selectModels, Comparator.comparing(SelectModel::getLabel));
            selectModelsCache.put(enClazz, selectModels);
            return selectModels;
        }
    }

    /**
     * 根据关键字模糊查询值和描述，返回满足条件的枚举列表
     *
     * @param enClazz
     * @param group
     * @return
     */
    public static List<SelectModel> toSelectModelsFuzzy(Class<?> enClazz, String keyword, String group) {
        List<SelectModel> selectModels = toSelectModels(enClazz);
        return selectModels.stream().filter(sm -> {
            if (StringUtils.isNotEmpty(group)) {
                if (group.equals(sm.getGroup())) {
                    return sm.getLabel().indexOf(keyword) >= 0 || sm.getValue().indexOf(keyword) >= 0;
                } else {
                    return false;
                }
            } else {
                return sm.getLabel().indexOf(keyword) >= 0 || sm.getValue().indexOf(keyword) >= 0;
            }
        }).collect(Collectors.toList());
    }

    /**
     * 根据枚举值值取描述
     *
     * @param enClazz
     * @param key
     * @return
     */
    public static SelectModel enDes(Class<?> enClazz, Object key) {
        if (key == null || StringUtils.isEmpty(key.toString())) {
            return null;
        }
        Map<String, SelectModel> modelMap = enValueDes.get(enClazz);
        if (modelMap != null) {
            return modelMap.get(key.toString());
        }
        synchronized (enClazz) {
            modelMap = enValueDes.get(enClazz);
            if (modelMap != null) {
                return modelMap.get(key.toString());
            }
            List<SelectModel> selectModels = toSelectModels(enClazz);
            HashMap<String, SelectModel> finalModelMap = new HashMap<>();
            selectModels.forEach(m -> {
                finalModelMap.put(m.getValue(), m);
            });
            //20210319 put时机放在finalModelMap数据装载完成以后，否则锁外面key可能会得到空值
            enValueDes.put(enClazz, finalModelMap);
            return finalModelMap.get(key.toString());//20210319 xiaoyingnan key的类型不确定，必须转为字符串，如果没转的话key类型未Byte，你们这里get出来为null
        }
    }

    /**
     * 根据枚举值值取描述
     *
     * @param enClazz
     * @param key
     * @return
     */
    public static String getLabel(Class<?> enClazz, Object key) {
        SelectModel selectModel = enDes(enClazz, key);
        return selectModel == null ? null : selectModel.getLabel();
    }

    /**
     * 返回分组group下的枚举的值列表
     *
     * @param enClazz
     * @param group
     * @return
     */
    public static List<String> getValuesGroup(Class<?> enClazz, String group) {
        List<String> list = new ArrayList<>();
        toSelectModels(enClazz).stream().filter(sm -> group.equals(sm.getGroup())).forEach(sm -> {
            list.add(sm.getValue());
        });
        return list;
    }

}
