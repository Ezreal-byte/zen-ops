package com.ops.zen.jdbc;

import com.ops.zen.utils.DateTimeUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class CastUtils {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(CastUtils.class);

    /**
     * 【实体字段类型，实体字段类型对应的枚举值】实体字段可定义的类型的范围为key的范围
     */
    private static Map<Class<?>, CastEnum> CAST_MAP = new HashMap<>();


    static {
        CAST_MAP.put(int.class, CastEnum.UNWRAP_INT);
        CAST_MAP.put(byte.class, CastEnum.UNWRAP_BYTE);
        CAST_MAP.put(long.class, CastEnum.UNWRAP_LONG);
        CAST_MAP.put(double.class, CastEnum.UNWRAP_DOUBLE);
        CAST_MAP.put(short.class, CastEnum.UNWRAP_SHORT);
        CAST_MAP.put(float.class, CastEnum.UNWRAP_FLOAT);

        CAST_MAP.put(byte[].class, CastEnum.UNWRAP_BYTE_ARRAY);

        CAST_MAP.put(Integer.class, CastEnum.WRAP_INT);
        CAST_MAP.put(Byte.class, CastEnum.WRAP_BYTE);
        CAST_MAP.put(Long.class, CastEnum.WRAP_LONG);
        CAST_MAP.put(Double.class, CastEnum.WRAP_DOUBLE);
        CAST_MAP.put(Short.class, CastEnum.WRAP_SHORT);
        CAST_MAP.put(Float.class, CastEnum.WRAP_FLOAT);

        CAST_MAP.put(Byte[].class, CastEnum.WRAP_BYTE_ARRAY);

        CAST_MAP.put(BigDecimal.class, CastEnum.BIGDECIMAL);
        CAST_MAP.put(BigInteger.class, CastEnum.BIGINTEGER);

        CAST_MAP.put(String.class, CastEnum.STRING);
        CAST_MAP.put(StringBuilder.class, CastEnum.STRINGBUILDER);
        CAST_MAP.put(StringBuffer.class, CastEnum.STRINGBUFFER);

        CAST_MAP.put(Date.class, CastEnum.DATE);
        CAST_MAP.put(LocalDateTime.class, CastEnum.LOCALDATETIME);

        CAST_MAP.put(boolean.class, CastEnum.UNWRAP_BOOLEAN);
        CAST_MAP.put(Boolean.class, CastEnum.WRAP_BOOLEAN);
        CAST_MAP.put(char.class, CastEnum.UNWRAP_CHAR);
        CAST_MAP.put(Character.class, CastEnum.WRAP_CHAR);
        CAST_MAP.put(InputStream.class, CastEnum.INPUTSTREAM);


    }

    /**
     * 将value转为能被type接受的值，满足type a = value;
     *
     * @param type
     * @param value
     * @return
     */
    public static Object valueByFieldType(Class<?> type, Object value) {
        if (value == null) {
            return value;
        }
        Class<?> valueType = value.getClass();
        if (type.equals(valueType)) {
            return value;
        }
        if (valueType.isAssignableFrom(type)) {
            return value;
        }
        CastEnum castEnum = CAST_MAP.get(type);
        switch (castEnum) {
            case UNWRAP_INT:
                if (value instanceof BigDecimal) {
                    value = new BigDecimal(value.toString()).intValue();
                } else if (value instanceof String) {
                    value = ((BigDecimal) value).intValue();
                } else {
                    value = (int) value;
                }
                break;
            case UNWRAP_BYTE:
                if (value instanceof BigDecimal) {
                    value = ((BigDecimal) value).byteValue();
                } else if (value instanceof String) {
                    value = new BigDecimal(value.toString()).byteValue();
                } else {
                    value = (byte) value;
                }
                break;
            case UNWRAP_LONG:
                if (value instanceof BigDecimal) {
                    value = ((BigDecimal) value).longValue();
                } else if (value instanceof String) {
                    value = new BigDecimal(value.toString()).longValue();
                } else {
                    value = (long) value;
                }
                break;
            case UNWRAP_DOUBLE:
                if (value instanceof BigDecimal) {
                    value = ((BigDecimal) value).doubleValue();
                } else if (value instanceof String) {
                    value = new BigDecimal(value.toString()).doubleValue();
                } else {
                    value = (double) value;
                }
                break;
            case UNWRAP_SHORT:
                if (value instanceof BigDecimal) {
                    value = ((BigDecimal) value).shortValue();
                } else if (value instanceof String) {
                    value = new BigDecimal(value.toString()).shortValue();
                } else {
                    value = (short) value;
                }
                break;
            case UNWRAP_FLOAT:
                if (value instanceof BigDecimal) {
                    value = ((BigDecimal) value).floatValue();
                } else if (value instanceof String) {
                    value = new BigDecimal(value.toString()).floatValue();
                } else {
                    value = (float) value;
                }
                break;
            case UNWRAP_BYTE_ARRAY:
                //nothing to do
                break;
            case WRAP_INT:
                value = Integer.valueOf(value.toString());
                break;
            case WRAP_BYTE:
                value = Byte.valueOf(value.toString());
                break;
            case WRAP_LONG:
                value = Long.valueOf(value.toString());
                break;
            case WRAP_DOUBLE:
                value = Double.valueOf(value.toString());
                break;
            case WRAP_SHORT:
                value = Short.valueOf(value.toString());
                break;
            case WRAP_FLOAT:
                value = Float.valueOf(value.toString());
                break;
            case BIGDECIMAL:
                value = new BigDecimal(value.toString());
                break;
            case BIGINTEGER:
                value = new BigInteger(value.toString());
                break;
            case STRING:
                value = value.toString();
                break;
            case STRINGBUILDER:
                value = new StringBuilder(value.toString());
                break;
            case UNWRAP_BOOLEAN:
                if (!value.getClass().equals(boolean.class))
                    castException(type, value);
                break;
            case WRAP_BOOLEAN:
                if (value.getClass().equals(boolean.class)) {
                    value = new Boolean((boolean) value);
                } else {
                    castException(type, value);
                }
                break;
            case UNWRAP_CHAR:
            case WRAP_CHAR:
                if (!value.getClass().equals(String.class)) {
                    castException(type, value);
                }
                value = ((String) value).charAt(0);
                break;
            case DATE: // com.uis.nx.soar.base.jdbc.EntityHelper.toEasyRecord方法已经将EasyRecord中的时间类型转为了java.util.Date
                // 对方必须是java.sql.Date或java.sql.Timestamp或java.util.Date --> 全部转为 java.util.Date
                if (value instanceof Date) {
                    value = new Date(((java.sql.Date) value).getTime());
                } else if (value instanceof Timestamp) {
                    value = new Date(((Timestamp) value).getTime());
                } else if (value instanceof java.sql.Date) {
                    // value = value
                } else {
                    castException(type, value);
                }
                break;
            case LOCALDATETIME: // com.uis.nx.soar.base.jdbc.EntityHelper.toEasyRecord方法已经将EasyRecord中的时间类型转为了java.util.Date
                if (value instanceof Date) {
                    value = DateTimeUtils.asLocalDateTime(((Date) value).getTime());
                } else if (value instanceof java.sql.Date) {
                    value = DateTimeUtils.asLocalDateTime(((java.sql.Date) value).getTime());
                } else if (value instanceof Timestamp) {
                    value = DateTimeUtils.asLocalDateTime(((Timestamp) value).getTime());
                } else {
                    castException(type, value);
                }
                break;
            case INPUTSTREAM:
                break;
            default:
                castException(type, value);
        }
        return value;
    }

    /**
     * 格式化的异常信息
     *
     * @param type
     * @param value
     */
    public static void castException(Class<?> type, Object value) {
        throw new RuntimeException(String.format("%s（%s） -> %s 数据库中的值无法转换为实体的字段值，类型无法匹配", value.getClass(), value, type));
    }

}
