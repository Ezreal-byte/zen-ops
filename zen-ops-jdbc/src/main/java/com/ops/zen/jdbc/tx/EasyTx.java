package com.ops.zen.jdbc.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于存储于ThreadLocal中的Connection，配合DataSource和Connection的动态代理来实现事务提交和回滚
 * 用法
 * <pre>
 *     try {
 *         EasyTx.begin();//开始
 *         。。。。
 *         EasyTx.commit();//提交
 *     } catch (Exception ex) {
 *         EasyTx.rollback();//回滚
 *     } finally {
 *         EasyTx.close();//关闭
 *     }
 * </pre>
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class EasyTx {

    private static final Logger logger = LoggerFactory.getLogger(EasyTx.class);

    public static Transaction transaction = null;

    /**
     * 将事务使用封装到该方法
     *
     * @param handler
     * @param t
     * @param <T>
     */
    public static <T, R> R execAtomic(TxExecutor<T, R> handler, T t) throws Exception {
        try {
            transaction.begin(false);
            R r = handler.apply(t);
            transaction.commit();
            return r;
        } catch (Exception e) {
            logger.error("", e);
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }
    }


    public static <T, R> R requireNew(TxExecutor<T, R> handler, T t) throws Exception {
        try {
            transaction.begin(true);
            R r = handler.apply(t);
            transaction.commit();
            return r;
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }
    }
}
