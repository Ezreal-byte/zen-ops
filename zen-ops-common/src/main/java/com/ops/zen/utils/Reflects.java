package com.ops.zen.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 反射，列加载，实例化等工具类
 *
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
@Reflects.NULL_ANNOTATION
public class Reflects {

    private static final Logger logger = LoggerFactory.getLogger(Reflects.class);

    /**
     * 获取泛型的第一个Class
     *
     * @param clazz
     * @return
     */
    public static Class<?> getTtypeFirst(Class<?> clazz) {
        Type type = clazz.getGenericSuperclass();
        return getTtypeFirst(type);
    }

    public static Class<?> getTtypeFirst(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type claz = pType.getActualTypeArguments()[0];
            if (claz instanceof Class) {
                return (Class<?>) claz;
            }
        }
        return null;
    }

    public static List<Type> getParameterTypes(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] actualTypeArguments = pType.getActualTypeArguments();
            return Arrays.stream(actualTypeArguments).collect(Collectors.toList());
        }
        return null;
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    public static Class<?> loadClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader.loadClass(className);
    }

    public static <T> T newInstance(ClassLoader classLoader, String className) {
        try {
            if (classLoader != null) {
                Class<?> aClass = classLoader.loadClass(className);
                return (T) aClass.newInstance();
            } else {
                return (T) (Class.forName(className).newInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String asString(Class<?> resource, String name) {
        InputStream resourceAsStream = resource.getResourceAsStream(name);
        try {
            return IOUtils.toString(resourceAsStream, "UTF-8");
        } catch (IOException e) {
            logger.warn("资源{} {}读取失败", resource.getName(), name, e);
        }
        return null;
    }

    public static List<Map<String, Object>> toListMap(List list) {
        List<Map<String, Object>> rt = new ArrayList<>();
        for (Object o : list) {
            rt.add(toMap(o));
        }
        return rt;
    }

    public static Map<String, Object> toMap(Object o) {
        return Reflect.on(o).asMap();
    }

    private static Map<Method, Map<Class<? extends Annotation>, Annotation>> METHOD_ANNOTATIONS = new ConcurrentHashMap<>();
    private static Map<Field, Map<Class<? extends Annotation>, Annotation>> FIELD_ANNOTATIONS = new ConcurrentHashMap<>();
    private static Map<Class<?>, Map<Class<? extends Annotation>, Annotation>> KLASS_ANNOTATIONS = new ConcurrentHashMap<>();

    // TODO 以下还有优化空间
    /*
        通过缓存方式获取Method注解
     */
    public static <T extends Annotation> T getMtdAnnotation(Method method, Class<T> annotationClass) {
        if (!METHOD_ANNOTATIONS.containsKey(method)) {
            METHOD_ANNOTATIONS.putIfAbsent(method, new ConcurrentHashMap<>()); // putIfAbsent相当于put加锁
        }
        Map<Class<? extends Annotation>, Annotation> annotationMap = METHOD_ANNOTATIONS.get(method);
        if (!annotationMap.containsKey(annotationClass)) {
            T annotation = method.getAnnotation(annotationClass);
            if (annotation != null) {
                annotationMap.putIfAbsent(annotationClass, annotation);
            } else {
                annotationMap.putIfAbsent(annotationClass, Reflects.class.getAnnotation(NULL_ANNOTATION.class));
            }
        }
        Annotation annotation = annotationMap.get(annotationClass);
        if (annotation instanceof NULL_ANNOTATION) {
            return null;
        }
        return (T) annotation;
    }

    /*
        通过缓存方式获取Field注解
     */
    public static <T extends Annotation> T getFldAnnotation(Field fld, Class<T> annotationClass) {
        if (!FIELD_ANNOTATIONS.containsKey(fld)) {
            FIELD_ANNOTATIONS.putIfAbsent(fld, new ConcurrentHashMap<>()); // putIfAbsent相当于put加锁
        }
        Map<Class<? extends Annotation>, Annotation> annotationMap = FIELD_ANNOTATIONS.get(fld);
        if (!annotationMap.containsKey(annotationClass)) {
            T annotation = fld.getAnnotation(annotationClass);
            if (annotation != null) {
                annotationMap.putIfAbsent(annotationClass, annotation);
            } else {
                annotationMap.putIfAbsent(annotationClass, Reflects.class.getAnnotation(NULL_ANNOTATION.class));
            }
        }
        Annotation annotation = annotationMap.get(annotationClass);
        if (annotation instanceof NULL_ANNOTATION) {
            return null;
        }
        return (T) annotation;
    }

    /*
        通过缓存方式获取Class注解
     */
    public static <T extends Annotation> T getClsAnnotation(Class<?> cls, Class<T> annotationClass) {
        if (!KLASS_ANNOTATIONS.containsKey(cls)) {
            KLASS_ANNOTATIONS.putIfAbsent(cls, new ConcurrentHashMap<>()); // putIfAbsent相当于put加锁
        }
        Map<Class<? extends Annotation>, Annotation> annotationMap = KLASS_ANNOTATIONS.get(cls);
        if (!annotationMap.containsKey(annotationClass)) {
            T annotation = cls.getAnnotation(annotationClass);
            if (annotation != null) {
                annotationMap.putIfAbsent(annotationClass, annotation);
            } else {
                annotationMap.putIfAbsent(annotationClass, Reflects.class.getAnnotation(NULL_ANNOTATION.class));
            }
        }
        Annotation annotation = annotationMap.get(annotationClass);
        if (annotation instanceof NULL_ANNOTATION) {
            return null;
        }
        return (T) annotation;
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface NULL_ANNOTATION {

    }
}
