package com.ops.zen.utils;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private static Context instance = new Context();

    private long idMainThread = -1;

    private static final ThreadLocal<Map<Object, Object>> attributes = new InheritableThreadLocalMap<Map<Object, Object>>();

    private Context() {
    }

    /**
     * 是否是主线程，主线程主要用于系统启动
     */
    public boolean isMainThread() {
        return (Thread.currentThread().getId() == idMainThread);
    }

    public void setMainThread(Thread thread) {
        idMainThread = thread.getId();
    }

    public void setAttribute(String name, Object value) {
        // TODO old value 是否要返回
        attributes.get().put(name, value);
    }


    public <T> T getAttribute(String name, Class<T> attributeType) {
        Object o = getAttribute(name);
        if (attributeType.isInstance(o))
            return attributeType.cast(o);
        return null;
    }

    public Object getAttribute(String name) {
        return attributes.get().get(name);
    }

    public static Context get() {
        return instance;
    }

    public static Object remove(String key) {
        Object removed = attributes.get().remove(key);
        return removed;
    }

    public static void removeAll() {
        attributes.get().clear();
        attributes.remove();
    }

    private static final class InheritableThreadLocalMap<T extends Map<Object, Object>>
            extends InheritableThreadLocal<Map<Object, Object>> {
        protected Map<Object, Object> initialValue() {
            return new HashMap<Object, Object>();
        }

        @SuppressWarnings({"unchecked"})
        protected Map<Object, Object> childValue(Map<Object, Object> parentValue) {
            if (parentValue != null) {
                return (Map<Object, Object>) ((HashMap<Object, Object>) parentValue).clone();
            } else {
                return null;
            }
        }
    }


}
