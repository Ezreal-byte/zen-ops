package com.ops.zen.jdbc;

import com.ops.zen.jdbc.annotation.EntityFieldWrapper;
import com.ops.zen.jdbc.cache.EntityField2TableFieldCache;
import com.ops.zen.jdbc.cache.EntityTypeFieldsCache;
import com.ops.zen.jdbc.dialect.DialectEn;
import com.ops.zen.jdbc.mixed.AnnotationMixed;
import com.ops.zen.utils.IOUtils;
import com.ops.zen.utils.Reflect;
import com.ops.zen.utils.StringUtils;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class EntityHelper {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(EntityHelper.class);

    /**
     * 将EasyRecord转为实体（对象），
     * 将查询出的结果中的字段名和实体字段名进行匹配，
     * 满足数据库字段（包含下划线）对应实体字段的驼峰式，或equalsIgnoreCase时可以将数据库字段赋值给实体字段
     * 直接字段对字段赋值，目前可以将实体字段权限>=私有的字段赋值（无法处理静态字段）
     *
     * @param clazz
     * @param easyRecord
     * @param <T>
     * @return
     */
    public static <T> T easyRecord2Entity(Class<? extends T> clazz, EasyRecord easyRecord) {
        //for exception detail
        String fieldName = null;
        Object value = null;
        Field field = null;
        try {
            T entity = Reflect.newInstance(clazz);
            //从缓存获取提升性能
            Map<String, Field> fields = EntityTypeFieldsCache.inst().get(clazz);
//            Map<String, Field> fields = Reflect.nameFields(clazz, true);
            Set<Map.Entry<String, Field>> entries = fields.entrySet();
            for (Map.Entry<String, Field> ey : entries) {
                fieldName = ey.getKey();
                field = ey.getValue();
                //  实体字段名映射到数据库字段名，从缓存获取提升性能
//                String tableFldName = mappingEntityFldName2TableFldName(field);
                //先用pojo的字段名称的toLowerCase来直接在easyRecord中获取值
                String tableFldName = field.getName().toLowerCase();
                value = easyRecord.get(tableFldName);
                if (value == null) {
                    // 如果没有对应的值，则将字段名从驼峰式转为下划线方式再次取值
                    tableFldName = EntityField2TableFieldCache.inst().get(field);
                    value = easyRecord.get(tableFldName);
                }

                Object convertedValue = CastUtils.valueByFieldType(field.getType(), value);
                if (convertedValue != null) {
                    Reflect.set(entity, field, convertedValue);
                }
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(String.format("%s(%s)=%s", fieldName, field != null ? field.getType() : null, value), e);
        }
    }

    /**
     * 将驼峰式的实体字段名映射为数据库的字段名
     * 返回结果全小写
     *
     * @param field
     * @return
     */
    public static String mappingEntityFldName2TableFldName(Field field) {
        //优先取注解中的字段名
        EntityFieldWrapper annotation = AnnotationMixed.getEntityField(field);
        if (annotation != null && StringUtils.isNotEmpty(annotation.name())) {
            return annotation.name();
        }
        String fieldName = field.getName();
        char[] chars = fieldName.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean uppperCharPreviousHasNoUnderline = true;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'A' && c <= 'Z') {
                c += 32;
                if (i == 0) {
                    uppperCharPreviousHasNoUnderline = false;
                }
                if (i >= 1 && chars[i - 1] == '_') {
                    uppperCharPreviousHasNoUnderline = false;
                }
                if (uppperCharPreviousHasNoUnderline) {//如果一个大写字母前一个字母不是下划线则追加下划线
                    sb.append('_');
                    sb.append(c);
                } else {
                    sb.append(c);
                }
                uppperCharPreviousHasNoUnderline = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * 将ResultSet解析为EasyRecord
     *
     * @param dialect
     * @param resultSet
     * @param blobAsInputStream
     * @return
     * @throws SQLException
     */
    public static EasyRecord toEasyRecord(DialectEn dialect, ResultSet resultSet, boolean blobAsInputStream) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        EasyRecord er = new EasyRecord();
        int i = 1;
        do {
            String columnName = metaData.getColumnName(i);
            String columnLabel = metaData.getColumnLabel(i); // 别名
            // ORACLE二者相等，MYSQL二者不等
            if (StringUtils.isNotEmpty(columnLabel)) {
                columnName = columnLabel;
            }
            int columnType = metaData.getColumnType(i);
            String columnTypeName = metaData.getColumnTypeName(i);
            if (logger.isTraceEnabled()) {
                Object debugValue = resultSet.getObject(columnName);
                logger.trace("{} - {} sqlType【{}】 --> {}（{}）", columnName, columnTypeName, columnType, debugValue != null ? debugValue.getClass() : null, debugValue);
            }
            //TODO 通过columnType来决定使用ResultSet的哪个get方法
            Object value = null;
            switch (columnType) {
                case Types.TIMESTAMP:
                    Timestamp ts = resultSet.getTimestamp(i);
                    if (ts != null) {
                        value = new java.util.Date(ts.getTime());
                    }
                    break;
                case Types.DATE:
                    Date dt = resultSet.getDate(i);
                    if (dt != null) {
                        value = new java.util.Date(dt.getTime());
                    }
                    break;
                case Types.CLOB:
                    value = resultSet.getString(i);
                    break;
                case Types.LONGVARBINARY:// mysql的longblob对应该值    https://blog.csdn.net/lxacdf/article/details/77579203
                case Types.BLOB:
                case Types.BINARY: // postgres的blob类型
                    if (blobAsInputStream && dialect == DialectEn.POSTGRE) { // 处理postgres的 bytea类型，对应jdbc的Types.BINARY
                        value = new ByteArrayInputStream(resultSet.getBytes(i));
                    } else if (blobAsInputStream) { // ORACLE、MYSQL
                        // 如果是mysql，得到的是ByteArrayInputStream（全部读进内存中）
                        Blob blob = resultSet.getBlob(i);
                        value = blob == null ? null : blob.getBinaryStream();
                    } else {
                        value = resultSet.getBytes(i);
                    }
                    break;
                case Types.BOOLEAN:
                    value = resultSet.getBoolean(i);
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                case Types.LONGVARCHAR:
                    value = resultSet.getString(i);
                    break;
                default:
                    value = resultSet.getObject(columnName);
            }
            Object put = er.put(columnName, value);
            i++;
        } while (i <= columnCount);
        return er;
    }


    /**
     * 将实体字段值转为sql使用的值，不仅仅给实体字段值转换使用，同时也给EasyParams中的参数值做转换，
     * 重要的一点对于in的支持，入参x需要是Collection类型
     *
     * @param x   如果 入参x为Collection的派生，则返回List<Object>，一般在sql in表达式的时候使用
     * @param <T>
     * @return
     */
    public static <T> Object entityFieldValue2SqlValue(Object x) {
        //TODO 其他类型转换
        Object o = x;
        if (x instanceof Date) {
            o = new Date(((Date) x).getTime());
        } else if (x instanceof Collection) {
            o = ((Collection) x).stream().collect(Collectors.toList());
        }
        return o;
    }


    /**
     * 为{@link PreparedStatement}设置参数值
     *
     * @param preparedStatement
     * @param paramsList        按顺序排列的参数值
     * @throws SQLException
     */
    public static void preparedStatementSet(PreparedStatement preparedStatement, List<Object> paramsList) throws SQLException {
        AtomicInteger index = new AtomicInteger(1);
        for (Object param : paramsList) {
            Object paramValue = EntityHelper.entityFieldValue2SqlValue(param);
            if (paramValue instanceof List) {// 集合代表表达式in的处理
                ((List) paramValue).stream().forEach(ele -> {
                    try {
                        // if (ele instanceof InputStream) { // TODO 可能没有意义，元素是List是不应出现类型是InputStream
                        //     preparedStatement.setBinaryStream(index.getAndIncrement(), (InputStream) ele);
                        // } else {
                        preparedStatement.setObject(index.getAndIncrement(), ele);
                        // }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (paramValue instanceof InputStream) {
                preparedStatement.setBinaryStream(index.getAndIncrement(), (InputStream) paramValue);
            } else if (paramValue instanceof java.util.Date) { // java.util.Date 转为java.sql.Timestamp
                preparedStatement.setTimestamp(index.getAndIncrement(), new Timestamp(((java.util.Date) paramValue).getTime()));
            } else {
                preparedStatement.setObject(index.getAndIncrement(), paramValue);
            }
        }
    }

    /**
     * 主要用来处理参数是InputStream的情况
     *
     * @param dialectEn
     * @param preparedStatement
     * @param atomicInteger
     * @param fieldValue
     * @throws SQLException
     */
    public static void preparedStatementSet(DialectEn dialectEn, PreparedStatement preparedStatement, AtomicInteger atomicInteger, Object fieldValue) throws SQLException {
        fieldValue = EntityHelper.entityFieldValue2SqlValue(fieldValue);
        if (fieldValue instanceof InputStream) {
            if (dialectEn == DialectEn.MYSQL) {
                try {
                    // mysql 需要使用字节数组来处理流（非常消耗内存）
                    preparedStatement.setBytes(atomicInteger.getAndIncrement(), IOUtils.toByteArray(((InputStream) fieldValue)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                preparedStatement.setBinaryStream(atomicInteger.getAndIncrement(), (InputStream) fieldValue);
            }
        } else if (fieldValue instanceof java.util.Date) { // java.util.Date 转为java.sql.Timestamp
            preparedStatement.setTimestamp(atomicInteger.getAndIncrement(), new Timestamp(((java.util.Date) fieldValue).getTime()));
        } else {
            preparedStatement.setObject(atomicInteger.getAndIncrement(), fieldValue);
        }
    }

}
