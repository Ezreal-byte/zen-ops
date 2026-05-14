package com.ops.zen.jdbc.tx;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 事务接口
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public interface Transaction {
    /**
     * 当前线程是否在事务中
     *
     * @return
     */
    boolean isInTx();

    /**
     * 连接是否在事务中
     *
     * @param conn
     * @return
     */
    boolean isInTx(Connection conn);

    /**
     * 开始事务
     *
     * @param requireNew 是否开启新事务（需要新建连接，和外层事务不会互相影响）
     */
    void begin(boolean requireNew);

    /**
     * 移除事务标记，移除连接，回收连接
     */
    void close();

    /**
     * 提交事务
     */
    void commit();

    /**
     * 回滚事务
     */
    void rollback();

    /**
     * 将连接绑定到线程上下文
     *
     * @param ds
     * @param conn
     */
    void setConn(DataSource ds, Connection conn);

    /**
     * 获取连接
     *
     * @return
     */
    Connection getConn(DataSource ds);
}
