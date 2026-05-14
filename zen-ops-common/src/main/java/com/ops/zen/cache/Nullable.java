/**
 *
 */
package com.ops.zen.cache;


/**
 * @Author xyn
 * @Date 2025/04/12
 * @Description 缓存如果可能返回空值，使用该包装类
 */
public class Nullable<T> {

    private T t;

    public Nullable() {
    }

    public Nullable(T t) {
        super();
        this.t = t;
    }

    public boolean isNull() {
        return this.t == null;
    }

    public T value() {
        return this.t;
    }

    public T getValue() {
        return this.t;
    }

    public void setValue(T t) {
        this.t = t;
    }

    public static <T> Nullable<T> fromNullable(T t) {
        return new Nullable<T>(t);
    }


}
