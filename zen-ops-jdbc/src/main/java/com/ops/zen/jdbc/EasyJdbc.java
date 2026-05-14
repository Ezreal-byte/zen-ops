package com.ops.zen.jdbc;

import com.ops.zen.cache.Pair;
import com.ops.zen.jdbc.annotation.EntityFieldWrapper;
import com.ops.zen.jdbc.cache.EntityTypeFieldsCache;
import com.ops.zen.jdbc.cache.WhereName2TableFieldCache;
import com.ops.zen.jdbc.dialect.*;
import com.ops.zen.jdbc.dialect.*;
import com.ops.zen.jdbc.mixed.AnnotationMixed;
import com.ops.zen.jdbc.sql.*;
import com.ops.zen.jdbc.sql.*;
import com.ops.zen.jdbc.tx.EasyTransactionV2;
import com.ops.zen.jdbc.tx.EasyTx;
import com.ops.zen.jdbc.tx.Transaction;
import com.ops.zen.tpl.TemplateService;
import com.ops.zen.tpl.TemplateServiceFactory;
import com.ops.zen.utils.*;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.JsonUtils;
import com.ops.zen.utils.Reflects;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.ex.Exceptions;
import com.ops.zen.utils.map.PageResult;
import com.ops.zen.jdbc.cond.Where;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 简单Jdbc操作工具
 * TODO 考虑为实体设计模型，缓存实体模型。遇到需要反射创建实例时注意和实体模型的配合
 * TODO 嵌套事务 requireNew实现
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class EasyJdbc implements Jdbc {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(EasyJdbc.class);

    private final String DEFAULT_TEMPLAE_SERVICE = "com.uis.nx.soar.base.tpl.beetl.BeetlTemplateService";

    private final SQLFormat sqlFormat = new SQLFormat();

    private DataSource dataSource;

    private DialectEn dialect;

    private TemplateService templateService;

    private Transaction transaction;

    public EasyJdbc() {
        transaction = new EasyTransactionV2();
        EasyTx.transaction = transaction;
        // nx-soar-sqltpl和nx-soar-api的依赖要在同一个bundle中
        ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(EasyJdbc.class.getClassLoader());
        try {
            this.templateService = TemplateServiceFactory.inst().getDefault();
            // cube中使用时得到空，通过反射方式实例化
            if (this.templateService == null) {
                // 上下文的classloader来自调用的bundle，如果后面使用了Thread.currentThread().getContextClassLoader()进行类加载，可能无法加载到想要的类，所以将ContextClassLoader替换为EasyJdbc的类加载器
                // 对于错误java.lang.IllegalStateException: 初始化失败 com.uisnx.cube.core.bundle.BundleClassNotFoundException: nx-soar-cube-test: org.beetl.ext.fn.DateFunction
                // 通过上面的Thread.currentThread().setContextClassLoader(EasyJdbc.class.getClassLoader());来解决
                this.templateService = Reflects.newInstance(EasyJdbc.class.getClassLoader(), DEFAULT_TEMPLAE_SERVICE);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previousContextClassLoader);
        }
    }

    public EasyJdbc(DataSource dataSource) {
        this();
        this.dataSource = dataSourceProxy(dataSource);
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSourceProxy(dataSource);
    }

    @Override
    public DialectEn dialect() {
        return this.dialect;
    }

    /**
     * //TODO 如果不做动态代理，可以使用静态代理来实现切面的功能，定义DataSource和Connection的代理类，在代理类中做拦截
     * datasource和Connection做动态代理，控制连接的获取和连接的关闭
     */
    private DataSource dataSourceProxy(DataSource originDataSource) {
        dialect = Dialect.inst().dialect(originDataSource);

        Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{DataSource.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> returnType = method.getReturnType();
                //拦截获取连接方法
                if (name.equals("getConnection") && parameterTypes.length == 0 && returnType.equals(Connection.class)) {
                    // 从datasource中获取连接还是直接在线程本地变量中获取连接

                    if (transaction.isInTx()) {
                        Connection conn = transaction.getConn(originDataSource);
                        if (conn == null) {
                            Object invoke = method.invoke(originDataSource, args);
                            invoke = connProxy(invoke);
                            transaction.setConn(originDataSource, (Connection) invoke);
                            return invoke;
                        } else {
                            return conn;
                        }
                    } else {
                        Object invoke = method.invoke(originDataSource, args);
                        /*Connection conn = (Connection) invoke;
                        boolean autoCommit = conn.getAutoCommit();
                        System.out.println(autoCommit);*/
                        invoke = connProxy(invoke);
                        return invoke;
                    }
                } else {
                    return method.invoke(originDataSource, args);
                }
            }
        });
        return (DataSource) o;
    }

    /**
     * datasource和Connection做动态代理，控制连接的获取和连接的关闭
     *
     * @param conn
     * @return
     */
    private Object connProxy(Object conn) {
        Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{Connection.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> returnType = method.getReturnType();
                //拦截close方法，事务结束时可以close
                if (name.equals("close") && parameterTypes.length == 0 && returnType.equals(void.class)) {
                    // 是否真的关闭，什么时机关闭
                    // 修改为 conn -> proxy，因为最终是将proxy绑定到线程上下文
                    if (transaction.isInTx((Connection) proxy)) { // transaction.close调用先将isInTx标记为false，这样调用Connection的close方法时该语句为false
                        return null;
                    } else {
                        Object rt = method.invoke(conn, args);
                        return rt;
                    }
                } else {
                    return method.invoke(conn, args);
                }
            }
        });
        return o;
    }

    @Override
    public <T> int add(T entity) {
        return add(entity, null);
    }

    @Override
    public <T> int add(T entity, String tableName) {
        List<Field> fields = new ArrayList<>();
        String prepareSql = PrepareHelper.buildPrepareInsertSql(entity, fields, tableName);
//        if (logger.isDebugEnabled()) {
        logger.info(prepareSql);
//        }
        List<String> list = new ArrayList<>();
        try {
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
                    AtomicInteger atomicInteger = new AtomicInteger(1);
                    fields.forEach(fld -> {
                        try {
                            Object fieldValue = fld.get(entity);
                            list.add(String.valueOf(fieldValue));
                            EntityHelper.preparedStatementSet(dialect, preparedStatement, atomicInteger, fieldValue);
                        } catch (Exception e) {
                            Exceptions.throwAsRuntimeException(e);
                        }
                    });
                    return preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            logger.error("失败参数：{}", JsonUtils.toJSONString(list));
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> int addIgnoreNull(T entity) {
        List<Pair<Field, Object>> fieldValueList = new ArrayList<>();
        String prepareSql = PrepareHelper.buildPrepareInsertSql(entity, fieldValueList, null, true);
//        if (logger.isDebugEnabled()) {
        logger.info(prepareSql);
//        }
        try {
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
                    AtomicInteger atomicInteger = new AtomicInteger(1);
                    for (Pair<Field, Object> fieldObjectPair : fieldValueList) {
                        EntityHelper.preparedStatementSet(dialect, preparedStatement, atomicInteger, fieldObjectPair.getValue());
                    }
                    return preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param entities 元素字段如果为空会插入null
     * @param <T>
     * @return
     */
    @Override
    public <T> int[] add(List<T> entities) {
        return add(entities, null);
    }

    @Override
    public <T> int[] add(List<T> entities, String tableName) {
        if (entities == null || entities.size() == 0) {
            return null;
        }
        T entity = entities.get(0);
        List<Field> fields = new ArrayList<>();
        String prepareSql = PrepareHelper.buildPrepareInsertSql(entity, fields, tableName);
        try {
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
                    entities.stream().forEach(ent -> {
                        AtomicInteger atomicInteger = new AtomicInteger(1);
                        fields.forEach(fld -> {
                            try {
                                Object fieldValue = fld.get(ent);
                                EntityHelper.preparedStatementSet(dialect, preparedStatement, atomicInteger, fieldValue);
                            } catch (Exception e) {
                                Exceptions.throwAsRuntimeException(e);
                            }
                        });
                        try {
                            preparedStatement.addBatch();
//                            if (logger.isDebugEnabled()) {
                            logger.info(prepareSql);
//                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    try {
                        connection.setAutoCommit(false);
                        int[] ints = preparedStatement.executeBatch();
                        connection.commit();
                        return ints; // DML（insert,update,delete），DDL（create，alter...）
                    } catch (Exception e) {
                        connection.rollback();
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public <T> int update(T entity) {
        Class<?> clazz = entity.getClass();
        String tableName = AnnotationMixed.parseTableName(clazz);
        return update(tableName, clazz, entity);
    }

    @Override
    public <T> int update(T entity, Where where) {
        Class<?> clazz = entity.getClass();
        String tableName = AnnotationMixed.parseTableName(clazz);
        //支持实体继承，支持私有字段的继承，但是表名来自entity对应类的直接注解
        Map<String, Field> nameFields = EntityTypeFieldsCache.inst().get(clazz);

        List<String> setPairList = new ArrayList<>();
        Pair<String, Field> pkFldNameField = null;
        List<Pair<Field, EntityFieldWrapper>> fields = new ArrayList<>();
        for (Field fld : nameFields.values()) {
            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
            if (annoEntityField == null) {
                continue;
            }
            if (annoEntityField.pk()) {
                pkFldNameField = new Pair<>(annoEntityField.name(), fld);
            } else {//fields不包括pk的字段
                fields.add(new Pair<>(fld, annoEntityField));
                setPairList.add(String.format("%s=?", annoEntityField.name()));
            }
        }
        Assert.notNull(pkFldNameField, "实体没有标记主键");

        String whereClause = where.toString(WhereName2TableFieldCache.inst().get(clazz), true);
        String originSql = String.format("update %s set %s %s", tableName, StringUtils.concate(setPairList, ","), whereClause);
        SQL sqlObj = sqlFormat.compile(dialect, originSql, null, where.params().toMap());
//        if (logger.isDebugEnabled()) {
        logger.info(sqlLog(sqlObj.getPrepareSql(), sqlObj.getExampleSql(), sqlObj.getOriginSql()));
//        }
        try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlObj.getPrepareSql())) {
                //处理set a = b的预编译参数
                AtomicInteger atomicInteger = new AtomicInteger(1);
                fields.forEach(fld -> {
                    try {
                        Object fieldValue = fld.getKey().get(entity);
                        EntityHelper.preparedStatementSet(dialect, preparedStatement, atomicInteger, fieldValue);
                    } catch (Exception e) {
                        Exceptions.throwAsRuntimeException(e);
                    }
                });
                //最后处理where的预编译参数
                List<Object> paramsList = sqlObj.getParamList();
                EntityHelper.preparedStatementSet(preparedStatement, paramsList);
                int i = preparedStatement.executeUpdate();
                return i;
            }
        } catch (Exception e) {
            Exceptions.throwAsRuntimeException(e);
        }
        return 0;
    }

    @Override
    public <T> int update(String tableName, Class<? extends T> tClazz, T entity) {
        return UpdateHelper.update(tableName, tClazz, entity, logger, dataSource, dialect);
    }

    @Override
    public <T> int updateIgnoreNull(String tableName, Class<? extends T> tClazz, T entity) {
        return UpdateHelper.updateIgnoreNull(tableName, tClazz, entity, logger, dataSource, dialect);
    }

    @Override
    public <T> int updateIgnoreNull(T entity) {
        Class<?> clazz = entity.getClass();
        String tableName = AnnotationMixed.parseTableName(clazz);
        return UpdateHelper.updateIgnoreNull(tableName, clazz, entity, logger, dataSource, dialect);
    }


    @Override
    public <T> int[] update(List<T> entities) {
        if (entities == null || entities.size() == 0) {
            return new int[0];
        }
        T object = entities.get(0);
        Assert.notNull(object, "第一个元素为null");
        Class<?> tClazz = object.getClass();
        String tableName = AnnotationMixed.parseTableName(tClazz);


        Map<String, Field> nameFields = EntityTypeFieldsCache.inst().get(tClazz);

        List<String> setPairList = new ArrayList<>();
        Pair<String, Field> pkFldNameField = null;
        List<Pair<Field, EntityFieldWrapper>> fields = new ArrayList<>();
        for (Field fld : nameFields.values()) {
            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
            if (annoEntityField == null) {
                continue;
            }
            if (annoEntityField.pk()) {
                pkFldNameField = new Pair<>(annoEntityField.name(), fld);
            } else {//fields不包括pk的字段
                fields.add(new Pair<>(fld, annoEntityField));
                setPairList.add(String.format("%s=?", annoEntityField.name()));
            }
        }
        Assert.notNull(pkFldNameField, "实体没有标记主键");

        String prepareSql = String.format("update %s set %s where 1=1 and %s=?", tableName, StringUtils.concate(setPairList, ","), pkFldNameField.getKey());
//        if (logger.isDebugEnabled()) {
        logger.info(prepareSql);
//        }

        try {
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
                    AtomicInteger atomicInteger = new AtomicInteger(1);
                    for (T entity : entities) {
                        atomicInteger.set(1);
                        fields.forEach(fld -> {
                            try {
                                Object fieldValue = fld.getKey().get(entity);
                                EntityHelper.preparedStatementSet(dialect, preparedStatement, atomicInteger, fieldValue);
                            } catch (Exception e) {
                                Exceptions.throwAsRuntimeException(e);
                            }
                        });
                        //最后一个为主键的预编译参数
                        preparedStatement.setObject(atomicInteger.getAndIncrement(), pkFldNameField.getValue().get(entity));
                        preparedStatement.addBatch();
//                        if (logger.isDebugEnabled()) {
                        logger.info(prepareSql);
//                        }
                    }
                    try {
                        connection.setAutoCommit(false);
                        int[] ints = preparedStatement.executeBatch();
                        connection.commit();
                        return ints; // DML（insert,update,delete），DDL（create，alter...）
                    } catch (Exception e) {
                        connection.rollback();
                        throw e;
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> int delete(T entity) {
        Class<?> clazz = entity.getClass();
        String tableName = AnnotationMixed.parseTableName(clazz);
        //支持实体继承，支持私有字段的继承，但是表名来自entity对应类的直接注解
        Map<String, Field> nameFields = EntityTypeFieldsCache.inst().get(clazz);

        Pair<String, Field> pkFldNameField = null;
        for (Field fld : nameFields.values()) {
            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
            if (annoEntityField == null) {
                continue;
            }
            if (annoEntityField.pk()) {
                pkFldNameField = new Pair<>(annoEntityField.name(), fld);
            }
        }
        Assert.notNull(pkFldNameField, "实体没有标记主键");

        String originSql = String.format("delete from %s where %s=?", tableName, pkFldNameField.getKey());
//        if (logger.isDebugEnabled()) {
        logger.info(originSql);
//        }
        try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
            try (PreparedStatement preparedStatement = connection.prepareStatement(originSql)) {
                //处理where的预编译参数
                Object paramValue = EntityHelper.entityFieldValue2SqlValue(pkFldNameField.getValue().get(entity));
                preparedStatement.setObject(1, paramValue);
                int i = preparedStatement.executeUpdate();
                return i;
            }
        } catch (Exception e) {
            Exceptions.throwAsRuntimeException(e);
        }
        return 0;
    }

    @Override
    public <T> int delete(T entity, Where where) {
        Class<?> clazz = entity.getClass();
        String tableName = AnnotationMixed.parseTableName(clazz);
        return delete(tableName, clazz, where);
    }

    @Override
    public <T> int delete(String tableName, Class<? extends T> entityClass, Where where) {

        // TODO BEGIN 好像没必要考虑主键
        //支持实体继承，支持私有字段的继承，但是表名来自entity对应类的直接注解
//        Map<String, Field> nameFields = Reflect.onClass(entityClass).nonStaticNameFields(true);
//
//        Pair<String, Field> pkFldNameField = null;
//        for (Field fld : nameFields.values()) {
//            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
//            if (annoEntityField == null) {
//                continue;
//            }
//            if (annoEntityField.pk()) {
//                pkFldNameField = new Pair<>(annoEntityField.name(), fld);
//            }
//        }
//        Assert.notNull(pkFldNameField, "实体没有标记主键");
        // TODO END 好像没必要考虑主键

        String whereClause = where.toString(WhereName2TableFieldCache.inst().get(entityClass), true);
        String originSql = String.format("delete from %s %s", tableName, whereClause);
        SQL sqlObj = sqlFormat.compile(dialect, originSql, null, where.params().toMap());
//        if (logger.isDebugEnabled()) {
        logger.info(sqlLog(sqlObj.getPrepareSql(), sqlObj.getExampleSql(), sqlObj.getOriginSql()));
//        }
        try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlObj.getPrepareSql())) {
                //处理where的预编译参数
                List<Object> paramsList = sqlObj.getParamList();
                EntityHelper.preparedStatementSet(preparedStatement, paramsList);
                int i = preparedStatement.executeUpdate();
                return i;
            }
        } catch (Exception e) {
            Exceptions.throwAsRuntimeException(e);
        }
        return 0;
    }

    @Override
    public <T> T get(Class<? extends T> entityClass, String pk) {
        return getInner(entityClass, pk);
    }

    @Override
    public <T> T get(Class<? extends T> entityClass, Long pk) {
        if (pk == null) {
            throw new RuntimeException("参数pk不能为空");
        }
        return getInner(entityClass, pk);
    }

    private <T> T getInner(Class<? extends T> entityClass, Object pk) {
        String tableName = AnnotationMixed.parseTableName(entityClass);
        T t = null;
        try {
            t = entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, Field> fieldsMap = EntityTypeFieldsCache.inst().get(entityClass);
        Pair<String, Field> pkFldNameField = null;
        List<String> fieldNames = new ArrayList<>();
        List<Pair<Field, EntityFieldWrapper>> fields = new ArrayList<>();
        for (Field fld : fieldsMap.values()) {
            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
            if (annoEntityField == null) {
                continue;
            }
            if (annoEntityField.pk()) {
                pkFldNameField = new Pair<>(annoEntityField.name(), fld);
            }
            fieldNames.add(annoEntityField.name());
            fields.add(new Pair<>(fld, annoEntityField));
        }
        Assert.notNull(pkFldNameField, "实体没有标记主键");
        String prepareSql = String.format("select %s from %s where %s=?", StringUtils.concate(fieldNames, ","), tableName, pkFldNameField.getKey());
//        if (logger.isDebugEnabled()) {
        logger.info(prepareSql);
//        }
        try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
            try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
                if (pk.getClass() == Long.class) {
                    preparedStatement.setLong(1, (long) pk);
                } else {
                    preparedStatement.setString(1, (String) pk);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    //只取第一条
                    if (resultSet.next()) {
                        EasyRecord er = EntityHelper.toEasyRecord(dialect, resultSet, false);
                        for (Pair<Field, EntityFieldWrapper> pair : fields) {
                            EntityFieldWrapper ef = pair.getValue();
                            String fieldName = ef.name();
                            Field field = pair.getKey();
                            Object value = CastUtils.valueByFieldType(field.getType(), er.get(fieldName));
                            if (logger.isDebugEnabled()) {
                                logger.debug("给字段{}赋值{}", fieldName, value);
                            }
                            try {
                                field.set(t, value);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            Exceptions.throwAsRuntimeException(e);
        }
        return t;
    }

    private List<EasyRecord> query(String sql) {
        try {
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//                    if (logger.isDebugEnabled()) {
                    logger.info(sql);
//                    }
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        List<EasyRecord> list = new ArrayList<>();
                        while (resultSet.next()) {
                            EasyRecord er = EntityHelper.toEasyRecord(dialect, resultSet, false);
                            list.add(er);
                        }
                        return list;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<EasyRecord> query(String sql, int pageNum, int pageSize) {
        pageSize = PageSizeLimiter.limitSize(pageSize);
        String pageSql = DialectHelper.getPagerSql(dialect, sql, pageNum, pageSize);
        return query(pageSql);
    }

    @Override
    public List<EasyRecord> query(String sql, EasyParams params, EasyVars vars) {
        return query(sql, params, vars, 1, PAGE_SIZE_LIMIT);
    }

    /**
     * 根据方言将 _db_tp设置到params中
     *
     * @param dialect
     * @param params
     */
    private void putDbTp(DialectEn dialect, EasyParams params) {
        if (params == null) {
            params = new EasyParams();
        }
        if (dialect != null) {
            String dialectName = SoarDbHelper.parseDialect(dialect);
            if (StringUtils.isNotEmpty(dialectName)) {
                params.put(DialectConst._P_DB_TP, dialectName);
            }
        }
    }

    @Override
    public List<EasyRecord> query(String sql, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        pageSize = PageSizeLimiter.limitSize(pageSize);
        try {
            putDbTp(dialect, params);
            sql = templateService.process(sql, params != null ? params.toMap() : null);

            SQL sqlObj = sqlFormat.compile(dialect, sql, vars != null ? vars.getMap() : null,
                    params != null ? params.getMap() : null);
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                String prepareSql = sqlObj.getPrepareSql();
                prepareSql = DialectHelper.getPagerSql(dialect, prepareSql, pageNum, pageSize);
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
//                    if (logger.isDebugEnabled()) {
                    String exampleSql = DialectHelper.getPagerSql(dialect, sqlObj.getExampleSql(), pageNum, pageSize);
                    String originSql = sqlObj.getOriginSql();
                    logger.info(sqlLog(prepareSql, exampleSql, originSql));
//                    }
                    // 处理预编译参数
                    List<Object> paramsList = sqlObj.getParamList();
                    EntityHelper.preparedStatementSet(preparedStatement, paramsList);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        List<EasyRecord> list = new ArrayList<>();
                        while (resultSet.next()) {
                            EasyRecord er = EntityHelper.toEasyRecord(dialect, resultSet, false);
                            list.add(er);
                        }
                        return list;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageResult<EasyRecord> queryPage(String sql, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        pageSize = PageSizeLimiter.limitSize(pageSize);
        try {
            // TODO 和query-return List<EasyRecord>合并
            String sqlOrigin = sql;
            putDbTp(dialect, params);
            sql = templateService.process(sql, params != null ? params.toMap() : null);

            SQL sqlObj = sqlFormat.compile(dialect, sql, vars != null ? vars.getMap() : null,
                    params != null ? params.getMap() : null);
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                String prepareSql = sqlObj.getPrepareSql();
                prepareSql = DialectHelper.getPagerSql(dialect, prepareSql, pageNum, pageSize);
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
//                    if (logger.isDebugEnabled()) {
                    String exampleSql = DialectHelper.getPagerSql(dialect, sqlObj.getExampleSql(), pageNum, pageSize);
                    String originSql = sqlObj.getOriginSql();
                    logger.info(sqlLog(prepareSql, exampleSql, originSql));
//                    }
                    // 处理预编译参数
                    List<Object> paramsList = sqlObj.getParamList();
                    EntityHelper.preparedStatementSet(preparedStatement, paramsList);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        List<EasyRecord> list = new ArrayList<>();
                        while (resultSet.next()) {
                            EasyRecord er = EntityHelper.toEasyRecord(dialect, resultSet, false);
                            list.add(er);
                        }
                        // total
                        String sqlCount = DialectHelper.getTotalSql(dialect, sqlOrigin);
                        Integer count = query(sqlCount, params, vars).get(0).getInteger("count");
                        return new PageResult<EasyRecord>(list, pageNum, pageSize, count.longValue());
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageResult<EasyRecord> queryPage(Class<?> fileLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        try {
            if (sqlId == null) {
                String sql = SingleSQLFileFactory.inst().getSqlFileContent(fileLoaderClass, fileName, dialect);
                return queryPage(sql, params, vars, pageNum, pageSize);
            } else {
                String sql = null;
                SQLSource sqlSource = getSqlSource(fileLoaderClass, fileName, sqlId);
                Assert.notNull(sqlSource, String.format("没有找到sql：loader【】,fileName【%s】,sqlId【%s】", fileLoaderClass.getName(), fileName, sqlId));
                sql = sqlSource.getSqlContent();
                return queryPage(sql, params, vars, pageNum, pageSize);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<EasyRecord> query(String sql, EasyParams params, EasyVars vars, int pageNum, int pageSize, boolean sqlTemplateEngine) {
        return query(sql, params, vars, pageNum, pageSize);
    }

    @Override
    public long count(String sql, EasyParams params, EasyVars vars) {
        long total = query(DialectHelper.getTotalSql(dialect, sql), params, vars).get(0).getLong("count");
        return total;
    }

    @Override
    public boolean has(String sql, EasyParams params, EasyVars vars) {
        String innerSql = DialectHelper.getPagerSql(dialect, sql, 1, 1);
        return count(innerSql, params, vars) > 0;

    }

    @Override
    public List<EasyRecord> queryByFile(String fileName, EasyParams params, EasyVars vars) {
        Class<?> resouceLoaderClass = getCallerClass();
        return queryByFileInner(fileName, null, params, vars, resouceLoaderClass, 1, PAGE_SIZE_LIMIT, false);
    }

    /**
     * 获取调用EasyJdbc对象方法的对象的类
     *
     * @return
     */
    private Class<?> getCallerClass() {
        try {
            // 服务所在包需要和nx-soar-api包在同一个bundle中可保证在cube中正确进行的类加载
            return Reflects.loadClass(EasyJdbc.class.getClassLoader(), new Throwable().getStackTrace()[2].getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> queryBeansByFile(Class<? extends T> beanClass, String fileName, EasyParams params, EasyVars vars) {
        Class<?> resouceLoaderClass = getCallerClass();
        List<EasyRecord> easyRecords = queryByFileInner(fileName, null, params, vars, resouceLoaderClass, 1, PAGE_SIZE_LIMIT, false);

        List<T> list = new ArrayList<>();
        easyRecords.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(beanClass, er));
        });
        return list;
    }

    @Override
    public <T> List<T> queryBeansByFile(Class<? extends T> beanClass, String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        Class<?> resouceLoaderClass = getCallerClass();
        List<EasyRecord> easyRecords = queryByFileInner(fileName, null, params, vars, resouceLoaderClass, pageNum, pageSize, false);

        List<T> list = new ArrayList<>();
        easyRecords.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(beanClass, er));
        });
        return list;
    }

    @Override
    public <T> PageResult<T> queryBeansByFilePages(Class<? extends T> beanClass, String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        Class<?> resouceLoaderClass = getCallerClass();
        List<EasyRecord> easyRecords = queryByFileInner(fileName, null, params, vars, resouceLoaderClass, pageNum, pageSize, false);
        List<T> list = new ArrayList<>();
        easyRecords.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(beanClass, er));
        });
        long total = countByFileInner(fileName, null, params, vars, resouceLoaderClass, false);
        return new PageResult<>(list, pageNum, pageSize, total);
    }

    @Override
    public List<EasyRecord> queryByFile(String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        Class<?> resouceLoaderClass = getCallerClass();
        return queryByFileInner(fileName, null, params, vars, resouceLoaderClass, pageNum, pageSize, false);
    }

    @Override
    public List<EasyRecord> queryByFile(String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize, boolean useSqlTemplateEngine) {
        Class<?> resouceLoaderClass = getCallerClass();
        return queryByFileInner(fileName, null, params, vars, resouceLoaderClass, pageNum, pageSize, useSqlTemplateEngine);
    }

    @Override
    public long countByFile(String fileName, EasyParams params, EasyVars vars) {
        Class<?> resouceLoaderClass = getCallerClass();
        return countByFileInner(fileName, null, params, vars, resouceLoaderClass, false);
    }

    @Override
    public List<EasyRecord> queryByFile(Class<?> fileLoaderClass, String fileName, EasyParams params, EasyVars vars) {
        return queryByFileInner(fileName, null, params, vars, fileLoaderClass, 1, PAGE_SIZE_LIMIT, false);
    }

    @Override
    public List<EasyRecord> queryByFile(Class<?> fileLoaderClass, String fileName, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        return queryByFileInner(fileName, null, params, vars, fileLoaderClass, pageNum, pageSize, false);
    }

    @Override
    public long countByFile(Class<?> fileLoaderClass, String fileName, EasyParams params, EasyVars vars) {
        return countByFileInner(fileName, null, params, vars, fileLoaderClass, false);
    }

    @Override
    public List<EasyRecord> queryByFile(String fileName, String sqlId, EasyParams params, EasyVars vars) {
        Class<?> resouceLoaderClass = getCallerClass();
        return queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, 1, PAGE_SIZE_LIMIT, false);
    }

    @Override
    public <T> List<T> queryBeansByFile(Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars) {
        Class<?> resouceLoaderClass = getCallerClass();
        List<EasyRecord> easyRecords = queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, 1, PAGE_SIZE_LIMIT, false);
        List<T> list = new ArrayList<>();
        easyRecords.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(beanClass, er));
        });
        return list;
    }

    @Override
    public <T> List<T> queryBeansByFile(Class<?> resouceLoaderClass, Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars) {
        List<EasyRecord> easyRecords = queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, 1, PAGE_SIZE_LIMIT, false);
        List<T> list = new ArrayList<>();
        easyRecords.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(beanClass, er));
        });
        return list;
    }

    @Override
    public <T> List<T> queryBeansByFile(Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        Class<?> resouceLoaderClass = getCallerClass();
        List<EasyRecord> easyRecords = queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, pageNum, pageSize, false);
        List<T> list = new ArrayList<>();
        easyRecords.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(beanClass, er));
        });
        return list;
    }

    @Override
    public <T> PageResult<T> queryBeansByFilePages(Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        Class<?> resouceLoaderClass = getCallerClass();
        List<EasyRecord> easyRecords = queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, pageNum, pageSize, false);
        List<T> list = new ArrayList<>();
        easyRecords.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(beanClass, er));
        });
        long total = countByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, false);
        return new PageResult<>(list, pageNum, pageSize, total);
    }

    @Override
    public <T> PageResult<T> queryBeansByFilePages(Class<?> resouceLoaderClass, Class<? extends T> beanClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        List<EasyRecord> easyRecords = queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, pageNum, pageSize, false);
        List<T> list = new ArrayList<>();
        easyRecords.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(beanClass, er));
        });
        long total = countByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, false);
        return new PageResult<>(list, pageNum, pageSize, total);
    }

    @Override
    public List<EasyRecord> queryByFile(String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        Class<?> resouceLoaderClass = getCallerClass();
        return queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, pageNum, pageSize, false);
    }

    @Override
    public PageResult<EasyRecord> queryByFilePage(String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        Class<?> resouceLoaderClass = getCallerClass();
        List<EasyRecord> easyRecords = queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, pageNum, pageSize, false);
        return new PageResult<>(easyRecords, pageNum, pageSize, countByFile(resouceLoaderClass, fileName, sqlId, params, vars));
    }

    @Override
    public List<EasyRecord> queryByFile(String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize, boolean useSqlTemplateEngine) {
        Class<?> resouceLoaderClass = getCallerClass();
        return queryByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, pageNum, pageSize, useSqlTemplateEngine);
    }

    @Override
    public long countByFile(String fileName, String sqlId, EasyParams params, EasyVars vars) {
        Class<?> resouceLoaderClass = getCallerClass();
        return countByFileInner(fileName, sqlId, params, vars, resouceLoaderClass, false);
    }

    @Override
    public List<EasyRecord> queryByFile(Class<?> fileLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars) {
        return queryByFileInner(fileName, sqlId, params, vars, fileLoaderClass, 1, PAGE_SIZE_LIMIT, false);
    }

    @Override
    public List<EasyRecord> queryByFile(Class<?> fileLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        return queryByFileInner(fileName, sqlId, params, vars, fileLoaderClass, pageNum, pageSize, false);
    }

    @Override
    public long countByFile(Class<?> fileLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars) {
        return countByFileInner(fileName, sqlId, params, vars, fileLoaderClass, false);
    }

    private List<EasyRecord> queryByFileInner(String fileName, String sqlId, EasyParams params, EasyVars vars, Class<?> resouceLoaderClass, int pageNum, int pageSize, boolean sqlTemplateEngine) {
        try {
            if (sqlId == null) {
                String sql = SingleSQLFileFactory.inst().getSqlFileContent(resouceLoaderClass, fileName, dialect);
                return query(sql, params, vars, pageNum, pageSize);
            } else {
                String sql = null;
                SQLSource sqlSource = getSqlSource(resouceLoaderClass, fileName, sqlId);
                Assert.notNull(sqlSource, String.format("没有找到sql：loader【】,fileName【%s】,sqlId【%s】", resouceLoaderClass.getName(), fileName, sqlId));
                sql = sqlSource.getSqlContent();
                return query(sql, params, vars, pageNum, pageSize);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SQLSource getSqlSource(Class<?> loader, String fileName, String sqlId) throws IOException {
        return SQLSourceFactory.inst().getSqlSource(loader, fileName, sqlId, dialect);
    }

    private long countByFileInner(String fileName, String sqlId, EasyParams params, EasyVars vars, Class<?> resouceLoaderClass, boolean sqlTemplateEngine) {
        String sql = null;
        try {
            if (sqlId == null) {
                sql = SingleSQLFileFactory.inst().getSqlFileContent(resouceLoaderClass, fileName, dialect);
            } else {
                SQLSource sqlSource = getSqlSource(resouceLoaderClass, fileName, sqlId);
                Assert.notNull(sqlSource, String.format("没有找到sql：loader【】,fileName【%s】,sqlId【%s】", resouceLoaderClass.getName(), fileName, sqlId));
                sql = sqlSource.getSqlContent();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sql = DialectHelper.getTotalSql(dialect, sql);
        return query(sql, params, vars).get(0).getInteger("count");
    }

    @Override
    public <T> List<T> query(Class<? extends T> clazz, String sql) {
        return query(clazz, sql, 1, PAGE_SIZE_LIMIT);
    }

    @Override
    public <T> List<T> query(Class<? extends T> clazz, String sql, int pageNum, int pageSize) {
        List<EasyRecord> query = query(sql, pageNum, pageSize);
        List<T> list = new ArrayList<>();
        query.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(clazz, er));
        });
        return list;
    }

    @Override
    public long count(Class<?> clazz, String sql) {
        long total = query(DialectHelper.getTotalSql(dialect, sql)).get(0).getLong("count");
        return total;
    }

    @Override
    public <T> List<T> query(Class<? extends T> clazz, String sql, EasyParams params, EasyVars vars) {
        return query(clazz, sql, params, vars, 1, PAGE_SIZE_LIMIT);
    }

    @Override
    public <T> List<T> query(Class<? extends T> clazz, String sql, EasyParams params, EasyVars vars, int pageNum, int pageSize) {
        List<EasyRecord> query = query(sql, params, vars, pageNum, pageSize);
        List<T> list = new ArrayList<>();
        query.forEach(er -> {
            list.add(EntityHelper.easyRecord2Entity(clazz, er));
        });
        return list;
    }

    @Override
    public long count(Class<?> clazz, String sql, EasyParams params, EasyVars vars) {
        long total = query(DialectHelper.getTotalSql(dialect, sql), params, vars).get(0).getLong("count");
        return total;
    }

    @Override
    public <T> List<T> queryEntities(Class<? extends T> entityClass, Where where) {
        return queryEntities(entityClass, where, 1, PAGE_SIZE_LIMIT);
    }

    @Override
    public <T> List<T> queryEntities(Class<? extends T> entityClass, Where where, int pageNum, int pageSize) {
        return queryEntities(entityClass, null, where, false, pageNum, pageSize);
    }

    @Override
    public <T> PageResult<T> queryPageEntities(Class<? extends T> clazz, Where where, int pageNum, int pageSize) {
        Long total = countEntities(clazz, where);
        List<T> ts = queryEntities(clazz, where, pageNum, pageSize);
        return new PageResult<T>(ts, pageNum, pageSize, total);
    }

    @Override
    public <T> List<T> queryEntities(Class<? extends T> entityClass, String tableName, Where where, boolean blobAsInputStream, int pageNum, int pageSize) {
        return queryEntities(entityClass, tableName, where, null, blobAsInputStream, null, pageNum, pageSize);
    }

    @Override
    public <T> List<T> queryEntities(Class<? extends T> entityClass, String tableName, Where where, String[] filterFields, boolean blobAsInputStream, Consumer<List<T>> consumerBeforeConnClosed, int pageNum, int pageSize) {
        pageSize = PageSizeLimiter.limitSize(pageSize);
        Assert.notNull(where, "where 不能为空");
        if (tableName == null)
            tableName = AnnotationMixed.parseTableName(entityClass);
        T t = null;
        try {
            t = entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, Field> fieldsMap = EntityTypeFieldsCache.inst().get(entityClass);
        Pair<String, Field> pkFldNameField = null;
        List<String> fieldNames = new ArrayList<>();
        List<Pair<Field, EntityFieldWrapper>> fields = new ArrayList<>();
        for (Field fld : fieldsMap.values()) {
            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
            if (annoEntityField == null) {
                continue;
            }
            if (annoEntityField.pk()) {
                pkFldNameField = new Pair<>(annoEntityField.name(), fld);
            }
            fieldNames.add(annoEntityField.name());
            fields.add(new Pair<>(fld, annoEntityField));
        }
        // 使用过滤字段，和数据库字段一致
        if (filterFields != null && filterFields.length > 0) {
            fieldNames = Arrays.asList(filterFields);
        }
        Assert.notNull(pkFldNameField, "实体没有标记主键");
        String whereClause = where.toString(WhereName2TableFieldCache.inst().get(entityClass), true);
        String originSql = String.format("select %s from %s %s", StringUtils.concate(fieldNames, ","), tableName, whereClause);
        originSql = DialectHelper.getPagerSql(dialect, originSql, pageNum, pageSize);
        SQL sqlObj = sqlFormat.compile(dialect, originSql, null, where.params().toMap());
//        if (logger.isDebugEnabled()) {
        logger.info(sqlLog(sqlObj.getPrepareSql(), sqlObj.getExampleSql(), sqlObj.getOriginSql()));
//        }
        List<T> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlObj.getPrepareSql())) {
                // 处理预编译参数
                List<Object> paramsList = sqlObj.getParamList();
                EntityHelper.preparedStatementSet(preparedStatement, paramsList);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        EasyRecord er = EntityHelper.toEasyRecord(dialect, resultSet, blobAsInputStream);
                        for (Pair<Field, EntityFieldWrapper> pair : fields) {
                            EntityFieldWrapper ef = pair.getValue();
                            String fieldName = ef.name();
                            Field field = pair.getKey();
                            Object value = CastUtils.valueByFieldType(field.getType(), er.get(fieldName));
                            if (logger.isTraceEnabled()) {
                                logger.trace("给字段{}赋值{}", fieldName, value);
                            }
                            try {
                                field.set(t, value);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        list.add(EntityHelper.easyRecord2Entity(entityClass, er));
                        if (consumerBeforeConnClosed != null) {
                            consumerBeforeConnClosed.accept(list);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Exceptions.throwAsRuntimeException(e);
        }
        return list;
    }

    @Override
    public long countEntities(Class<?> entityClass, Where where) {
        Assert.notNull(where, "where 不能为空");
        String tableName = AnnotationMixed.parseTableName(entityClass);
        String whereClause = where.toString(WhereName2TableFieldCache.inst().get(entityClass), false);
        String originSql = String.format("select count(1) count from %s %s", tableName, whereClause);
        SQL sqlObj = sqlFormat.compile(dialect, originSql, null, where.params().toMap());
//        if (logger.isDebugEnabled()) {
        logger.info(sqlLog(sqlObj.getPrepareSql(), sqlObj.getExampleSql(), sqlObj.getOriginSql()));
//        }
        try {
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlObj.getPrepareSql())) {
                    // 处理预编译参数
                    List<Object> paramsList = sqlObj.getParamList();
                    EntityHelper.preparedStatementSet(preparedStatement, paramsList);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        resultSet.next();
                        return resultSet.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int execute(String sql, EasyParams params, EasyVars vars) {
        try {
            putDbTp(dialect, params);
            sql = templateService.process(sql, params != null ? params.toMap() : null);
            SQL sqlObj = sqlFormat.compile(dialect, sql, vars != null ? vars.getMap() : null,
                    params != null ? params.getMap() : null);
            try (Connection connection = dataSource.getConnection()) {
                String prepareSql = sqlObj.getPrepareSql();
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
//                    if (logger.isDebugEnabled()) {
                    String exampleSql = sqlObj.getExampleSql();
                    String originSql = sqlObj.getOriginSql();
                    logger.info(sqlLog(prepareSql, exampleSql, originSql));
//                    }
                    // 处理预编译参数
                    List<Object> paramsList = sqlObj.getParamList();
                    EntityHelper.preparedStatementSet(preparedStatement, paramsList);
                    return preparedStatement.executeUpdate(); // DML（insert,update,delete），DDL（create，alter...）
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int execute(String sql) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//                    if (logger.isDebugEnabled()) {
                    logger.info(sql);
//                    }
                    // 处理预编译参数
                    return preparedStatement.executeUpdate(); // DML（insert,update,delete），DDL（create，alter...）
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int execute(String fileName, String sqlId, EasyParams params, EasyVars vars) {
        Class<?> resouceLoaderClass = getCallerClass();
        return execute(resouceLoaderClass, fileName, sqlId, params, vars);
    }

    @Override
    public int execute(Class<?> resouceLoaderClass, String fileName, String sqlId, EasyParams params, EasyVars vars) {

        try {
            if (sqlId == null) {
                String sql = SingleSQLFileFactory.inst().getSqlFileContent(resouceLoaderClass, fileName, dialect);
                return this.execute(sql, params, vars);
            } else {
                String sql = null;
                SQLSource sqlSource = getSqlSource(resouceLoaderClass, fileName, sqlId);
                Assert.notNull(sqlSource, String.format("没有找到sql：loader【】,fileName【%s】,sqlId【%s】", resouceLoaderClass.getName(), fileName, sqlId));
                sql = sqlSource.getSqlContent();
                return this.execute(sql, params, vars);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int[] executeBatch(String sql, List<EasyParams> paramsList, EasyVars vars) {
        try {
            EasyParams easyParams = paramsList.get(0);
            putDbTp(dialect, easyParams);
            sql = templateService.process(sql, easyParams != null ? easyParams.toMap() : null);
            // 做一次编译，主要目的是拿到paramSortedKey
            SQL sqlObj = sqlFormat.compile(dialect, sql, vars != null ? vars.getMap() : null,
                    easyParams != null ? easyParams.getMap() : null);
            List<String> paramSortedKey = sqlObj.getParamSortedKey();

            try (Connection connection = dataSource.getConnection()) {
                String prepareSql = sqlObj.getPrepareSql();
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
//                    if (logger.isDebugEnabled()) {
                    String exampleSql = sqlObj.getExampleSql();
                    String originSql = sqlObj.getOriginSql();
                    logger.info(sqlLog(prepareSql, "批量执行：batch sql", originSql));
//                    }
                    paramsList.stream().forEach(ep -> {
                        // 处理预编译参数（批量）
                        List<Object> preparedParams = new ArrayList<>();
                        // 根据参数排序列表paramSortedKey使用EasyParams组装preparedParams
                        paramSortedKey.stream().forEach(key -> preparedParams.add(ep.get(key)));
                        try {
                            EntityHelper.preparedStatementSet(preparedStatement, preparedParams);
                            preparedStatement.addBatch();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    try {
                        /*
                            批量时开启事务，否则：
                            批量操作时前面成功后面失败，抛异常后
                            在连接池的场景下，连接不会释放，事务会挂起（锁表）
                            开启事务，异常时回滚，防止事务挂起锁表
                         */
                        connection.setAutoCommit(false);
                        int[] ints = preparedStatement.executeBatch();
                        connection.commit();
                        return ints; // DML（insert,update,delete），DDL（create，alter...）
                    } catch (Exception e) {
                        connection.rollback();
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SQL compile(String sql, EasyParams easyParams, EasyVars vars) {
        // 方言支持
        putDbTp(dialect, easyParams);
        // 表达式支持
        sql = templateService.process(sql, easyParams != null ? easyParams.toMap() : null);
        SQL sqlObj = sqlFormat.compile(dialect, sql, vars != null ? vars.getMap() : null,
                easyParams != null ? easyParams.getMap() : null);
        return sqlObj;
    }

    /**
     * 输出sql日志
     *
     * @param prepareSql
     * @param exampleSql
     * @param originSql
     * @return
     */
    public static String sqlLog(String prepareSql, String exampleSql, String originSql) {
        return String.format("\r\n compiled：%s\r\nexample：%s\r\norigin：%s\r\n", prepareSql, exampleSql, originSql);
    }

}
