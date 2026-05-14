package com.ops.zen.jdbc;


import com.ops.zen.cache.Pair;
import com.ops.zen.jdbc.annotation.EntityFieldWrapper;
import com.ops.zen.jdbc.mixed.AnnotationMixed;
import com.ops.zen.utils.Reflect;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class PrepareHelper {

    /**
     * add方法使用，构建insert的prepare模式下的sql语句
     *
     * @param <T>
     * @param entity
     * @param fields    传入空列表，带出entity的实体字段列表
     * @param tableName 当tableName不为空时优先使用tableName，为空时解析entity的注解中表名
     * @return
     */
    public static <T> String buildPrepareInsertSql(T entity, List<Field> fields, String tableName) {
        Class<?> clazz = entity.getClass();
        if (tableName == null)
            tableName = AnnotationMixed.parseTableName(clazz);
        //支持实体继承，支持私有字段的继承，但是表名来自entity对应类的直接注解
        Map<String, Field> nameFields = Reflect.on(entity).nameFields(true);

        StringBuilder strFields = new StringBuilder();//fld1,fld2,fld3
        StringBuilder strValues = new StringBuilder();//prepareSql ?,?,?
        nameFields.values().stream().forEach(fld -> {
            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
            if (annoEntityField == null) {
                return;
            }
            fields.add(fld);
            String name = annoEntityField.name();
            if (strFields.length() != 0) {
                strFields.append(",").append(name);
                strValues.append(",").append("?");
            } else {
                strFields.append(name);
                strValues.append("?");
            }
        });

        return String.format("insert into %s(%s) values(%s)", tableName, strFields.toString(), strValues.toString());
    }

    public static <T> String buildPrepareInsertSql(T entity, List<Pair<Field, Object>> fieldValueList, String tableName, boolean ignoreNull) {
        Class<?> clazz = entity.getClass();
        if (tableName == null)
            tableName = AnnotationMixed.parseTableName(clazz);
        //支持实体继承，支持私有字段的继承，但是表名来自entity对应类的直接注解
        Map<String, Field> nameFields = Reflect.on(entity).nameFields(true);

        StringBuilder strFields = new StringBuilder();//fld1,fld2,fld3
        StringBuilder strValues = new StringBuilder();//prepareSql ?,?,?
        nameFields.values().stream().forEach(fld -> {
            Object o = null;
            try {
                o = fld.get(entity);
            } catch (IllegalAccessException e) {
            }

            if (ignoreNull && o == null) { // ignore null
                return;
            }
            EntityFieldWrapper annoEntityField = AnnotationMixed.getEntityField(fld);
            if (annoEntityField == null) {
                return;
            }
            String name = annoEntityField.name();
            if (strFields.length() != 0) {
                strFields.append(",").append(name);
                strValues.append(",").append("?");
            } else {
                strFields.append(name);
                strValues.append("?");
            }
            fieldValueList.add(new Pair<>(fld, o));
        });

        return String.format("insert into %s(%s) values(%s)", tableName, strFields.toString(), strValues.toString());
    }
}
