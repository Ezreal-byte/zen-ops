package com.ops.zen.jdbc;


import com.ops.zen.jdbc.dialect.DialectEn;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.jdbc.sql.EasyVars;
import com.ops.zen.jdbc.sql.SQL;
import com.ops.zen.utils.map.PageResult;
import com.ops.zen.jdbc.cond.Where;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public interface Jdbc {

    /*
     * pageSize限制条数
     */
    int PAGE_SIZE_LIMIT = 2000;

    /**
     * pageSize限制条数，5000条，特殊用途
     */
    int PAGE_SIZE_5000 = 5000;

    int PAGE_SIZE_10000 = 10000;
    int PAGE_SIZE_50000 = 50000;

    /**
     * 最大值，慎用！可能会导致OOM
     */
    int PAGE_SIZE_MAX = Integer.MAX_VALUE - 1;

    DataSource getDataSource();

    void setDataSource(DataSource dataSource);

    DialectEn dialect();

    <T> int add(T entity);

    <T> int add(T entity, String tableName);

    <T> int addIgnoreNull(T entity);

    <T> int[] add(List<T> entities);

    <T> int[] add(List<T> entities, String tableName);

    <T> int update(T entity);

    <T> int updateIgnoreNull(String tableName, Class<? extends T> tClazz, T entity);

    <T> int updateIgnoreNull(T entity);

    <T> int[] update(List<T> entities);

    <T> int update(T entity, Where where);

    <T> int update(String tableName, Class<? extends T> tClazz, T entity);

    <T> int delete(T entity);

    <T> int delete(T entity, Where where);

    /**
     * @param tableName   删除数据的表以tableName为准，不使用entityClass注解中的表名
     * @param entityClass
     * @param where
     * @param <T>
     * @return
     */
    <T> int delete(String tableName, Class<? extends T> entityClass, Where where);

    <T> T get(Class<? extends T> entityClass, String pk);

    <T> T get(Class<? extends T> entityClass, Long pk);

    <T> List<T> queryEntities(Class<? extends T> clazz, Where where);

    <T> List<T> queryEntities(Class<? extends T> clazz, Where where, int pageNum, int pageSize);

    <T> PageResult<T> queryPageEntities(Class<? extends T> clazz, Where where, int pageNum, int pageSize);

    /**
     * 优先使用表名tableName，当tableName为null时，解析entityClass注解中的表名
     *
     * @param <T>
     * @param entityClass
     * @param tableName
     * @param where
     * @param blobAsInputStream
     * @param pageNum
     * @param pageSize
     * @return
     */
    <T> List<T> queryEntities(Class<? extends T> entityClass, String tableName, Where where, boolean blobAsInputStream, int pageNum, int pageSize);

    <T> List<T> queryEntities(Class<? extends T> entityClass, String tableName, Where where, String[] filterFields, boolean blobAsInputStream, Consumer<List<T>> consumerBeforeConnClosed, int pageNum, int pageSize);

    long countEntities(Class<?> clazz, Where where);

    /**
     * 返回DML影响的行数或DDL时返回0
     *
     * @param sql
     * @param params
     * @param vars
     * @return
     */
    int execute(String sql, EasyParams params, EasyVars vars);

    /**
     * 不使用SQLFormat.compile编译，编译有些问题，例如'abc@cc.com'会被编译为'abc?com'
     * 这时候pg会报：org.postgresql.util.PSQLException: 栏位索引超过许可范围：1，栏位数：0。
     *
     * @param sql
     * @return
     */
    int execute(String sql);

    int execute(String fileName, String sqlId, EasyParams params, EasyVars vars);

    int execute(Class<?> loaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars);


    // --- begin各种自定义sql查询

    /**
     * 直接sql查询，无法使用预编译参数，尽量不要直接使用
     *
     * @param sql
     * @return
     */
    // List<EasyRecord> query(String sql);

    /**
     * 直接sql分页查询，无法使用预编译参数
     *
     * @param sql
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<EasyRecord> query(String sql, int pageNum, int pageSize);

    List<EasyRecord> query(String sql, EasyParams params, EasyVars vars);

    List<EasyRecord> query(String sql, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    PageResult<EasyRecord> queryPage(String sql, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    /**
     * @param fileLoaderClass
     * @param fileName
     * @param sqlId           可选
     * @param params
     * @param vars
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult<EasyRecord> queryPage(Class<?> fileLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    List<EasyRecord> query(String sql, EasyParams params, EasyVars vars, int pageNum, int pageSize, boolean sqlTemplateEngine);

    long count(String sql, EasyParams params, EasyVars vars);

    boolean has(String sql, EasyParams params, EasyVars vars);

    // ---- begin 文件sql
    List<EasyRecord> queryByFile(String fileName, EasyParams params, EasyVars vars);

    <T> List<T> queryBeansByFile(Class<? extends T> beanClass, String fileName, EasyParams params, EasyVars vars);

    <T> List<T> queryBeansByFile(Class<? extends T> beanClass, String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    <T> PageResult<T> queryBeansByFilePages(Class<? extends T> beanClass, String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    List<EasyRecord> queryByFile(String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    List<EasyRecord> queryByFile(String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize, boolean useSqlTemplateEngine);

    long countByFile(String fileName, EasyParams params, EasyVars vars);

    List<EasyRecord> queryByFile(Class<?> fileLoaderClass, String fileName, EasyParams params, EasyVars vars);

    List<EasyRecord> queryByFile(Class<?> fileLoaderClass, String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    long countByFile(Class<?> fileLoaderClass, String fileName, EasyParams params, EasyVars vars);
    // ---- end 文件sql

    // ---- begin 文件sql
    List<EasyRecord> queryByFile(String fileName, String sqlId, EasyParams params, EasyVars vars);

    <T> List<T> queryBeansByFile(Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars);

    <T> List<T> queryBeansByFile(Class<?> resouceLoaderClass, Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars);

    <T> List<T> queryBeansByFile(Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    <T> PageResult<T> queryBeansByFilePages(Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    <T> PageResult<T> queryBeansByFilePages(Class<?> loader, Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    List<EasyRecord> queryByFile(String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    PageResult<EasyRecord> queryByFilePage(String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    List<EasyRecord> queryByFile(String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize, boolean useSqlTemplateEngine);

    long countByFile(String fileName, String sqlId, EasyParams params, EasyVars vars);

    List<EasyRecord> queryByFile(Class<?> fileLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars);

    List<EasyRecord> queryByFile(Class<?> fileLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    long countByFile(Class<?> fileLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars);
    // ---- end 文件sql

    // ---- begin 对象映射查询
    <T> List<T> query(Class<? extends T> clazz, String sql);

    <T> List<T> query(Class<? extends T> clazz, String sql, int pageNum, int pageSize);

    long count(Class<?> clazz, String sql);

    <T> List<T> query(Class<? extends T> clazz, String sql, EasyParams params, EasyVars vars);

    <T> List<T> query(Class<? extends T> clazz, String sql, EasyParams params, EasyVars vars, int pageNum, int pageSize);

    long count(Class<?> clazz, String sql, EasyParams params, EasyVars vars);

    int[] executeBatch(String sql, List<EasyParams> paramsList, EasyVars vars);

    SQL compile(String sql, EasyParams easyParams, EasyVars easyVars);

    // ---- end 对象映射查询
    // --- end各种自定义sql查询


}
