package com.ops.zen.codegen.model;

import com.ops.zen.meta.TableColumnInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xyn
 * @date 2025/4/23 18:51
 * @description
 **/
public class TypeMapping {

    private static Map<Integer,  Class> map = new HashMap();

    static {
        map.put(2003, Object.class);
        map.put(-5, Double.class);
        map.put(-2, byte[].class);
        map.put(-7, Boolean.class);
        map.put(2004, byte[].class);
        map.put(16, Boolean.class);
        map.put(1, String.class);
        map.put(2005, String.class);
        map.put(70, Object.class);
        map.put(91, LocalDateTime.class);
        map.put(3, Double.class);
        map.put(2001, Object.class);
        map.put(8, Double.class);
        map.put(6, Double.class);
        map.put(4, Integer.class);
        map.put(2000, Object.class);
        map.put(-16, String.class);
        map.put(-4, byte[].class);
        map.put(-1, String.class);
        map.put(-15, String.class);
        map.put(2011, String.class);
        map.put(-9, String.class);
        map.put(0, Object.class);
        map.put(2, BigDecimal.class);
//      mapap.put(2, Long.class); // 原代码这里疑似有拼写错误，注释掉
        map.put(1111, Object.class);
        map.put(7, Float.class);
        map.put(2006, Object.class);
        map.put(5, Short.class);
        map.put(2002, Object.class);
        map.put(92, LocalDateTime.class);
        map.put(93, LocalDateTime.class);
        map.put(-6, Byte.class);
        map.put(-3, byte[].class);
        map.put(12, String.class);
    }

    public static String toJavaType(TableColumnInfo info) {
        Class clazz = map.get(info.getDataType());
        if (clazz == null) {
            return "Object";
        }
        clazz = overrideDefaultType(info, clazz);
        return clazz.getSimpleName();
    }

    private static Class overrideDefaultType(TableColumnInfo column, Class defaultType) {
        Class clazz = defaultType;
        switch (column.getDataType()) {
            case -7:
                clazz = calculateBitReplacement(column, defaultType);
                break;
            case 2:
            case 3:
                clazz = calculateBigDecimalReplacement(column, defaultType);
        }
        return clazz;
    }

    private static Class calculateBigDecimalReplacement(TableColumnInfo column, Class defaultType) {
        Class answer;
        if (column.getDecimalDigits() <= 0 && column.getColumnSize() <= 38) { //&& !this.forceBigDecimals
            if (column.getColumnSize() == 38) {
                answer = Long.class;
            } else if (column.getColumnSize() > 9) {
                answer = Double.class;
            } else if (column.getColumnSize() > 4) {
                answer = Integer.class;
            } else if (column.getColumnSize() == 1) {
                answer = Byte.class;
            } else {
                answer = Short.class;
            }
        } else {
            answer = defaultType;
        }
        return answer;
    }

    private static Class calculateBitReplacement(TableColumnInfo column, Class defaultType) {
        if (column.getColumnSize() > 1) {
            return byte[].class;
        }
        return defaultType;
    }

}
