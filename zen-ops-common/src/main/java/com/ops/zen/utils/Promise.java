package com.ops.zen.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @Author xiaoyingnan
 * @Date 2021/8/26 8:49
 * @Description
 */
public class Promise<R> {

    private R result;

    private Throwable throwable = null;

    public Promise() {

    }

    public Promise(Throwable throwable) {
        this.throwable = throwable;
    }

    public Promise(R r) {
        this.result = r;
    }

    public Promise then(Consumer<R> consumer) {
        if (throwable != null) {
            return this;
        }
        try {
            consumer.accept(this.result);
            return this;
        } catch (Exception e) {
            throw e;
        }
    }

    public Promise error(Consumer<Throwable> consumer) {
        if (throwable == null) {
            return this;
        }
        try {
            consumer.accept(throwable);
            return this;
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean hasError() {
        return throwable != null;
    }

    /**
     * 入口方法，參考{@link java.util.concurrent.CompletableFuture}
     *
     * @param supplier 需要返回值
     * @param <T>
     * @return
     */
    public static <T> Promise supply(Supplier<T> supplier) {
        try {
            T t = supplier.get();
            return new Promise(t);
        } catch (Exception e) {
            return new Promise(e);
        }
    }

    /**
     * 入口方法 直接调用
     *
     * @param callable 无需返回值
     * @return
     */
    public static Promise call(Callable callable) {
        try {
            callable.call();
            return new Promise(null);
        } catch (Exception e) {
            return new Promise(e);
        }
    }

    /**
     * 直接调用函数
     */
    @FunctionalInterface
    public interface Callable {
        void call() throws Exception;
    }
}
