package com.ops.zen.phy.meta;


import com.ops.zen.cache.Pair;
import com.ops.zen.jdbc.EntityHelper;
import com.ops.zen.jdbc.sql.SQL;
import com.ops.zen.utils.DateTimeUtils;
import com.ops.zen.utils.IOUtils;
import com.ops.zen.utils.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * @Author xyn
 * @Date 2021/6/16 9:38
 * @Description
 */
public class QueryMetaHelper {

    private static TypeMappingManager mappingManager;

    static {
        try {
            mappingManager = new TypeMappingManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO 配置不同数据库字段数据类型和JAVA数据类型的映射关系，生成DTO时使用配置的映射关系
     * 通过查询结果的元数据生成DTO的字段类型
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    public static List<DBFieldMeta> toNameTypes(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<DBFieldMeta> rt = new ArrayList<>();
        int columnCount = metaData.getColumnCount();
        int i = 1;
        do {
            String columnName = metaData.getColumnName(i); // 数据库字段名称（实际表现为，如果有别名则为别名）
            String columnLabel = metaData.getColumnLabel(i);// 字段别名使用as给定的列名
            if(StringUtils.isNotEmpty(columnLabel)) {
                columnName = columnLabel;
            }
            int columnType = metaData.getColumnType(i); // java.sql.Types
            int scale = metaData.getScale(i);// NUMBER(5,4)中的4是小数点右边的位数
            int precision = metaData.getPrecision(i);// NUMBER(5,4)中的5是数字的精度，最大长度5。如果为字符串则为字符串的长度，如为其他有长度的类型则为其长度值
            String columnTypeName = metaData.getColumnTypeName(i); // 数据库的真实字段类型，例如Oracle的varchar2，Mysql的tinyint等

            // 获取Java对应的字段类型和类型名称
            Pair<String, ? extends Class<?>> convert = convert(columnType, scale, precision);
            Class<?> fieldType = convert.getValue();// 字段类型
            String shortFieldTypeName = convert.getKey();// 字段类型名称
            rt.add(new DBFieldMeta(columnType, precision, scale, columnName, columnTypeName, fieldType, shortFieldTypeName,
                    StringUtils.dbField2Camel(columnName, false)));
            i++;
        } while (i <= columnCount);
        return rt;
    }

    /**
     * 获取Java对应的字段类型和类型名称
     *
     * @param columnType java.sql.Types
     * @param scale      NUMBER(5,4)中的4是小数点右边的位数
     * @param precision  NUMBER(5,4)中的5是数字的精度，最大长度5
     * @return
     */
    public static Pair<String, ? extends Class<?>> convert(int columnType, int scale, int precision) {
        // TODO 是否走配置方式生成DTO，配置文件中没有配置sqltype对应的java类型，目前使用无法生成实体
        if (false) {
            return mappingManager.typeMapping(columnType, scale, precision);
        }
        Class<?> fieldType = null;// 字段类型
        String shortFieldName = null;// 字段类型名称
        switch (columnType) {
            case Types.TIMESTAMP:
            case Types.DATE:
                fieldType = Date.class;
                break;
            case Types.BOOLEAN:
                fieldType = Boolean.class;
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                fieldType = String.class;
                break;
            case Types.FLOAT:
                fieldType = Float.class;
                break;
            case Types.DOUBLE:
            case Types.NUMERIC:
                if (scale > 0) {
                    fieldType = Double.class;
                } else if (precision == 38) {
                    fieldType = Long.class;
                } else {
                    fieldType = Integer.class;
                }
                break;
            case Types.DECIMAL:
                fieldType = BigDecimal.class;
                break;
            case Types.BIGINT:
                fieldType = BigInteger.class;
                break;
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                fieldType = Integer.class;
                break;
            case Types.BLOB:
            case Types.BINARY:
                fieldType = byte[].class;
                shortFieldName = "byte[]";
                break;
            default:
                fieldType = Object.class;
        }
        if (shortFieldName == null) {
            shortFieldName = fieldType.getSimpleName();
        }
        return new Pair<>(shortFieldName, fieldType);
    }

    /**
     * 通过sql和数据源获取sql查询出的数据库列的元数据集合
     *
     * @param sds
     * @param sql
     * @return
     */
    public static List<DBFieldMeta> fieldsMeta(DataSource sds, String sql) {
        try (Connection con = sds.getConnection()) {
            // 不用查出结果，使用meta
            ResultSet resultSet = con.createStatement().executeQuery(String.format("select * from (%s) t_alias where 1 = 2", sql));
            List<DBFieldMeta> mts = QueryMetaHelper.toNameTypes(resultSet);
            return mts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<DBFieldMeta> fieldsMeta(DataSource sds, SQL sql) {
        try (Connection con = sds.getConnection()) {
            PreparedStatement preparedStatement = con.prepareStatement(String.format("select * from (%s\n) t_alias where 1 = 2", sql.getPrepareSql()));
            // 处理预编译参数
            List<Object> paramsList = sql.getParamList();
            EntityHelper.preparedStatementSet(preparedStatement, paramsList);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<DBFieldMeta> mts = QueryMetaHelper.toNameTypes(resultSet);
            return mts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static String generateDTO(DataSource sds, String sql, String packageName, String className) throws Exception {
        // 成员变量区域
        StringBuilder fieldsDomain = new StringBuilder();
        // get set 方法
        StringBuilder fieldsGetSetDomain = new StringBuilder();
        // import 区域
        StringBuilder importDomain = new StringBuilder();
        // import 实际类型去重
        Set<Class<?>> imports = new HashSet<>();
        try (Connection con = sds.getConnection()) {
            // 不用查出结果，使用meta
            ResultSet resultSet = con.createStatement().executeQuery(String.format("select * from (%s) t_alias where 1 = 2", sql));
            List<DBFieldMeta> mts = QueryMetaHelper.toNameTypes(resultSet);
            mts.forEach(m -> {
                String def = String.format("\tprivate %s %s;\r\n\r\n", m.getJavaTypeShortName(), m.getColNameCamel());
                // 方法名首字母大写
                String methodName = StringUtils.firstCharUppercase(m.getColNameCamel());
                String get = String.format("\tpublic %s get%s() {\r\n\t\treturn this.%s;\r\n\t}\r\n\r\n",
                        m.getJavaTypeShortName(), methodName, m.getColNameCamel());
                String set = String.format("\tpublic void set%s(%s %s) {\r\n\t\tthis.%s = %s;\r\n\t}\r\n\r\n",
                        methodName, m.getJavaTypeShortName(), m.getColNameCamel(), m.getColNameCamel(), m.getColNameCamel());
                // java.lang下的核心类部分无需显示import
                String typeShortName = m.getJavaType().getName();
                if (!typeShortName.startsWith("java.lang.")) {
                    imports.add(m.getJavaType());
                }
                fieldsDomain.append(def);
                fieldsGetSetDomain.append(get);
                fieldsGetSetDomain.append(set);
            });
            imports.forEach(c -> {
                // byte[]字节数组特殊处理，不需要import，字节数组的byte[].class.getName === [B
                if (c.equals(byte[].class)) {
                    return;
                }
                importDomain.append(String.format("import %s;\r\n", c.getName()));
            });
        }
        String date = DateTimeUtils.format(new Date(), "yyyy/MM/dd HH:mm");
        // 占位符顺序：1）包名，2）import区域，3）日期，4）类名，5）成员变量区域，6）get set方法区域
        InputStream resourceAsStream = QueryMetaHelper.class.getResourceAsStream("jdbc.dto.txt");
        String tpl = IOUtils.toString(resourceAsStream, "UTF-8");
        String rt = String.format(tpl, packageName, importDomain.toString(), date, className, fieldsDomain.toString(), fieldsGetSetDomain.toString());
        return rt;
    }


}
