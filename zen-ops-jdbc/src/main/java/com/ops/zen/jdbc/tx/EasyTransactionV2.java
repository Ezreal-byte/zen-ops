package com.ops.zen.jdbc.tx;

import com.ops.zen.utils.Context;
import com.ops.zen.utils.ex.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

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
public class EasyTransactionV2 implements Transaction {

    private static final Logger logger = LoggerFactory.getLogger(EasyTransactionV2.class);

    private static final String KEY_CONN = "conn";

    /**
     * 当前线程是否在事务中
     *
     * @return
     */
    public boolean isInTx() {
        Stack<LinkedHashMap<DataSource, Connection>> stackDsCMap = (Stack<LinkedHashMap<DataSource, Connection>>) Context.get().getAttribute(KEY_CONN);
        return stackDsCMap != null;
    }

    /**
     * 连接是否在事务中
     *
     * @param conn
     * @return
     */
    public boolean isInTx(Connection conn) {
        Stack<LinkedHashMap<DataSource, Connection>> stackDsCMap = (Stack<LinkedHashMap<DataSource, Connection>>) Context.get().getAttribute(KEY_CONN);
        if (stackDsCMap == null) {
            return false;
        }
        LinkedHashMap<DataSource, Connection> dsConnMap = stackDsCMap.size() > 0 ? stackDsCMap.peek() : null;
        if (dsConnMap == null) {
            return false;
        }
        return dsConnMap.containsValue(conn);
    }

    /**
     * 开始事务
     *
     * @param requireNew 是否开启新事务（需要新建连接，和外层事务不会互相影响）
     */
    public void begin(boolean requireNew) {
        // stack + 1
        Stack<LinkedHashMap<DataSource, Connection>> stackDsCMap = (Stack<LinkedHashMap<DataSource, Connection>>) Context.get().getAttribute(KEY_CONN);
        if (stackDsCMap == null) {
            stackDsCMap = new Stack<>();
            Context.get().setAttribute(KEY_CONN, stackDsCMap);
        }
        if (requireNew) {
            stackDsCMap.push(new LinkedHashMap<>());
        }
    }

    /**
     * 移除事务标记，移除连接，回收连接
     */
    public void close() {
        try {
            Stack<LinkedHashMap<DataSource, Connection>> stackDsCMap = (Stack<LinkedHashMap<DataSource, Connection>>) Context.get().getAttribute(KEY_CONN);
            //按connection获取顺序close
            if (stackDsCMap == null || stackDsCMap.isEmpty()) {
                Context.remove(KEY_CONN);
                logger.debug("EasyJdbc上下文中没有打开的连接");
                return;
            }
            LinkedHashMap<DataSource, Connection> dsConnMap = stackDsCMap.pop();
            for (Map.Entry<DataSource, Connection> entry : dsConnMap.entrySet()) {
                //TODO 多数据源多连接时，某个出现异常以后直接中断
                entry.getValue().close();
            }
            // 为null时移除上下文中的标记
            if (stackDsCMap.isEmpty()) {
                Context.remove(KEY_CONN);
            }
        } catch (SQLException e) {
            Exceptions.throwAsRuntimeException(e);
        }
    }

    /**
     * 提交事务
     */
    public void commit() {
        try {
            Stack<LinkedHashMap<DataSource, Connection>> stackDsCMap = (Stack<LinkedHashMap<DataSource, Connection>>) Context.get().getAttribute(KEY_CONN);
            LinkedHashMap<DataSource, Connection> dsConnMap = stackDsCMap.peek();
            //按connection获取顺序commit
            for (Map.Entry<DataSource, Connection> entry : dsConnMap.entrySet()) {
                //TODO 多数据源多连接时，某个出现异常以后直接中断
                entry.getValue().commit();
            }
        } catch (SQLException e) {
            Exceptions.throwAsRuntimeException(e);
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        try {
            Stack<LinkedHashMap<DataSource, Connection>> stackDsCMap = (Stack<LinkedHashMap<DataSource, Connection>>) Context.get().getAttribute(KEY_CONN);
            if (stackDsCMap == null || stackDsCMap.isEmpty()) {
                return;
            }
            LinkedHashMap<DataSource, Connection> dsConnMap = stackDsCMap.peek();
            //按connection获取顺序rollback
            for (Map.Entry<DataSource, Connection> entry : dsConnMap.entrySet()) {
                //TODO 多数据源多连接时，某个出现异常以后直接中断
                entry.getValue().rollback();
            }
        } catch (SQLException e) {
            Exceptions.throwAsRuntimeException(e);
        }
    }

    public void setConn(DataSource ds, Connection conn) {
        try {
            /*
            因为是在事务中使用连接，所以修改为手动提交事务
             */
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            Exceptions.throwAsRuntimeException(e);
        }
        Stack<LinkedHashMap<DataSource, Connection>> stackDsCMap = (Stack<LinkedHashMap<DataSource, Connection>>) Context.get().getAttribute(KEY_CONN);
        if (stackDsCMap == null) {
            stackDsCMap = new Stack<>();
            Context.get().setAttribute(KEY_CONN, stackDsCMap);
        }
        LinkedHashMap<DataSource, Connection> dsConnMap = stackDsCMap.size() > 0 ? stackDsCMap.peek() : null;
        if (dsConnMap == null) {
            dsConnMap = new LinkedHashMap<>();
            stackDsCMap.push(dsConnMap);
        }
        dsConnMap.put(ds, conn);
    }

    /**
     * 获取连接
     *
     * @return
     */
    public Connection getConn(DataSource ds) {
        Stack<LinkedHashMap<DataSource, Connection>> stackDsCMap = (Stack<LinkedHashMap<DataSource, Connection>>) Context.get().getAttribute(KEY_CONN);
        if (stackDsCMap == null) {
            return null;
        }
        LinkedHashMap<DataSource, Connection> dsConnMap = stackDsCMap.size() > 0 ? stackDsCMap.peek() : null;
        if (dsConnMap != null) {
            Connection connection = dsConnMap.get(ds);
            return connection;
        }
        return null;
    }
}
