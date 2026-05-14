package com.ops.zen.jdbc.tx;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
@FunctionalInterface
public interface TxExecutor<T, R> {

    R apply(T t) throws Exception;

}
