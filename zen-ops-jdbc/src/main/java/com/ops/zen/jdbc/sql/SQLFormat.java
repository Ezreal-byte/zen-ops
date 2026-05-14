package com.ops.zen.jdbc.sql;

import com.ops.zen.jdbc.dialect.DialectEn;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class SQLFormat {

    // TODO exampleSql是否在输出日志时再解析？

    /**
     * 将sql编译为SQL对象：将sql转为预编译sql，替换vars中的变量，params转为预编译参数列表构造SQL对象
     * <pre>
     * IN:
     *     vars【a，tb1】
     *     params【b，'abc'】
     *     select * from $a where fld1 = @b or fld2 = @b
     * OUT:
     *     select * from tb1 where fld1 = ? or fld2 = ?
     *     paramList【'abc','abc'】
     *
     *     in @list  in('1','2')
     * </pre>
     * <p>
     * sql中的@转义@@
     * sql中的$转义$$
     *
     * @param dialect 可为null
     * @param sql
     * @param vars
     * @param params
     * @return
     */
    public SQL compile(DialectEn dialect, String sql, Map<String, Object> vars, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        List<Object> paramsList = new ArrayList<>();
        List<String> paramSortedKey = new ArrayList<>();
        char[] chars = sql.toCharArray();
        StringBuilder prepareSql = new StringBuilder();
        StringBuilder exampleSql = new StringBuilder();
        /*
            引入isLastCharVarOrParam相关逻辑为了解决
            以@name)结尾
            或以@name结尾的sql
            insert into V_PATIENT_CARD(code,name) values(@code,@name) 修改前解析为 insert into V_PATIENT_CARD(code,name) values(?,?
            少最后一个字符
            select count(1) count from OPS_C_NODE_LOG  where pk_cluster_info = @pkClusterInfo and app_tp = @appTp and script_tp = @scriptTp and node_uid = @nodeUid
            修改前解析为
            select count(1) count from OPS_C_NODE_LOG  where pk_cluster_info = ? and app_tp = ? and script_tp = ? and node_uid = ?d
            多最后一个字符
         */
        // 最后一个字是否是@参数或$变量的一部分
        boolean isLastCharVarOrParam = false;        // 用于@参数和$变量的解析，决定i是否回拨一位
        for (int i = 0; i < chars.length; i++) {
            int len = chars.length;
            char c = chars[i];
            if (c == '$' && chars[i + 1] == '$') {                 // $转义处理
                i++; // 循环中还会再++
                prepareSql.append("$");
                exampleSql.append("$");
            } else if (c == '$') {
                StringBuilder inner = new StringBuilder();
                char innerC = chars[++i];
                while (i < len && ((innerC >= 65 && innerC <= 90) || (innerC >= 97 && innerC <= 122) || innerC == '_' || (innerC >= 48 && innerC <= 57))) {
                    inner.append(innerC);
                    if (i < len - 1) {
                        innerC = chars[++i];
                    } else {
                        isLastCharVarOrParam = true;
                        break;
                    }
                }
                String key = inner.toString();
                Assert.notNull(vars, "需要变量值, 但变量不存在");
                Object obj = vars.get(key);
                Assert.notNull(obj, "%s变量值不能为空", key);
                prepareSql.append(obj);
                exampleSql.append(obj);
                if (i <= len - 1 && !isLastCharVarOrParam)                                 /* --i是为了当上面while中条件不满足时i（游标）的值停留在+1索引，这样最外层的for再+1就会丢一个字符  */
                    --i;
            } else if (c == '@' && chars[i + 1] == '@') {           // @转义处理
                i++; // 循环中还会再++
                prepareSql.append("@");
                exampleSql.append("@");
            } else if (c == '@') {
                StringBuilder inner = new StringBuilder();
                char innerC = chars[++i];
                while (i < len && ((innerC >= 65 && innerC <= 90) || (innerC >= 97 && innerC <= 122) || innerC == '_' || (innerC >= 48 && innerC <= 57))) {
                    inner.append(innerC);
                    if (i < len - 1) {
                        innerC = chars[++i];
                    } else {
                        isLastCharVarOrParam = true;
                        break;
                    }
                }
                String key = inner.toString();
                Object obj = params.get(key);
                // Assert.notNull(obj, "%s预编译参数值值不能为空", key); // TODO
                paramsList.add(obj);
                paramSortedKey.add(key);
                if (obj instanceof Collection) { // 当参数是集合时，断言它是in的使用【in(?,?,?)】，生成的sql将自动增加括号，表现为 (?,?,?)
                    int size = ((Collection) obj).size();
                    prepareSql.append("(");
                    for (int inIdx = 0; inIdx < size; inIdx++) {
                        if (inIdx == 0) {
                            prepareSql.append("?");
                        } else {
                            prepareSql.append(",?");
                        }
                    }
                    prepareSql.append(")");
                } else {
                    prepareSql.append('?');
                }

                exampleSql.append(translate(dialect, obj));
                if (i <= len - 1 && !isLastCharVarOrParam)                                 /* --i是为了当上面while中条件不满足时i（游标）的值停留在+1索引，这样最外层的for再+1就会丢一个字符  */
                    --i;
            } else {
                prepareSql.append(c);
                exampleSql.append(c);
            }
        }
        return new SQL(paramsList, paramSortedKey, prepareSql.toString(), exampleSql.toString(), sql, vars, params);
    }

    /**
     * 占位参数?替换为具体值
     *
     * @param dialect
     * @param obj     参数值
     * @return
     */
    private String translate(DialectEn dialect, Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof String) {
            return "'" + obj + "'";
        }
        if (obj instanceof Number) {
            return obj + "";
        }
        if (dialect == DialectEn.ORACLE) {
            if (obj instanceof Date) {
                return String.format("to_date('%s','yyyy-MM-dd HH24:mi:ss')", DateTimeUtils.normalFormatDate((Date) obj));
            } else if (obj instanceof LocalDateTime) {
                return String.format("to_date('%s','yyyy-MM-dd HH24:mi:ss')", DateTimeUtils.format((LocalDateTime) obj));
            } else if (obj instanceof LocalDate) {
                return String.format("to_date('%s','yyyy-MM-dd')", DateTimeUtils.format((LocalDate) obj, "yyyy-MM-dd"));
            }
        } else if (dialect == DialectEn.POSTGRE) {
            if (obj instanceof Date) {
                return String.format("to_timestamp('%s','YYYY-MM-DD HH24:MI:SS')", DateTimeUtils.normalFormatDate((Date) obj));
            } else if (obj instanceof LocalDateTime) {
                return String.format("to_timestamp('%s','YYYY-MM-DD HH24:MI:SS')", DateTimeUtils.format((LocalDateTime) obj));
            } else if (obj instanceof LocalDate) {
                return String.format("to_date('%s','YYYY-MM-DD')", DateTimeUtils.format((LocalDate) obj, "yyyy-MM-dd"));
            }
        } else if (dialect == DialectEn.MYSQL) {
            if (obj instanceof Date) {
                return String.format("str_to_date('%s','%%Y-%%m-%%d %%k:%%i:%%s')", DateTimeUtils.normalFormatDate((Date) obj));
            } else if (obj instanceof LocalDateTime) {
                return String.format("str_to_date('%s','%%Y-%%m-%%d %%k:%%i:%%s')", DateTimeUtils.format((LocalDateTime) obj));
            } else if (obj instanceof LocalDate) {
                return String.format("str_to_date('%s','%%Y-%%m-%%d')", DateTimeUtils.format((LocalDate) obj, "yyyy-MM-dd"));
            }
        } else { // TODO 其他方言
            return obj.toString();
        }

        // 处理in的情况，组装为（'1','2'）或(1,2)或(null,null,1,2) // TODO null是否要忽略？使用com.uis.nx.soar.base.jdbc.EntityHelper.entityFieldValue2SqlValue的地方也同样处理？
        if (obj instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            ((Collection) obj).stream().forEach(e -> {
                if (sb.length() > 1) {
                    sb.append(",");
                }
                if (e != null && e.getClass().isPrimitive()) {
                    sb.append(e.toString());
                } else if (e instanceof String) {
                    sb.append("'" + e + "'");
                } else {
                    sb.append(e);
                }
            });
            sb.append(")");
            obj = sb;
        }
        return obj.toString();
    }

}
