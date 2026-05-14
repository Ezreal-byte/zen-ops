package com.ops.zen.jdbc;
import com.ops.zen.cache.Pair;
import com.ops.zen.jdbc.annotation.EntityFieldWrapper;
import com.ops.zen.jdbc.cache.EntityTypeFieldsCache;
import com.ops.zen.jdbc.dialect.DialectEn;
import com.ops.zen.jdbc.mixed.AnnotationMixed;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.ex.Exceptions;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class UpdateHelper {

    public static <T> int update(String tableName, Class<? extends T> tClazz, T entity, Logger logger, DataSource dataSource, DialectEn dialect) {

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
        if (logger.isDebugEnabled()) {
            logger.debug(prepareSql);
        }

        try {
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
                    AtomicInteger atomicInteger = new AtomicInteger(1);
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
                    int i = preparedStatement.executeUpdate();
                    return i;
                }
            } catch (IllegalAccessException e) {
                Exceptions.throwAsRuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public static <T> int updateIgnoreNull(String tableName, Class<? extends T> tClazz, T entity, Logger logger, DataSource dataSource, DialectEn dialect) {

        Map<String, Field> nameFields = EntityTypeFieldsCache.inst().get(tClazz);

        List<String> setPairList = new ArrayList<>();
        Pair<String, EntityFieldWrapper> pkFldNameField = null;
        List<Pair<Field, EntityFieldWrapper>> fields = new ArrayList<>();
        for (Field fld : nameFields.values()) {
            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
            if (annoEntityField == null) {
                continue;
            }
            Object o = null;
            try {
                o = fld.get(entity);
            } catch (IllegalAccessException e) {
            }
            if (o == null) { // ignore null
                continue;
            }
            annoEntityField.setValue(o);
            if (annoEntityField.pk()) {
                pkFldNameField = new Pair<>(annoEntityField.name(), annoEntityField);
            } else {//fields不包括pk的字段
                fields.add(new Pair<>(fld, annoEntityField));
                setPairList.add(String.format("%s=?", annoEntityField.name()));
            }
        }
        Assert.notNull(pkFldNameField, "实体没有标记主键");

        String prepareSql = String.format("update %s set %s where 1=1 and %s=?", tableName, StringUtils.concate(setPairList, ","), pkFldNameField.getKey());
        if (logger.isDebugEnabled()) {
            logger.debug(prepareSql);
        }

        try {
            try (Connection connection = dataSource.getConnection()) {//代码有异常也会调用Connection的close方法，方法重写，throws异常可以覆盖接口的throws
                try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
                    AtomicInteger atomicInteger = new AtomicInteger(1);
                    for (Pair<Field, EntityFieldWrapper> field : fields) {
                        EntityHelper.preparedStatementSet(dialect, preparedStatement, atomicInteger, field.getValue().getValue());
                    }
                    //最后一个为主键的预编译参数
                    preparedStatement.setObject(atomicInteger.getAndIncrement(), pkFldNameField.getValue().getValue());
                    int i = preparedStatement.executeUpdate();
                    return i;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
