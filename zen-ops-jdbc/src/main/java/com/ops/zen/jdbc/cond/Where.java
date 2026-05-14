package com.ops.zen.jdbc.cond;


import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class Where {

    private List<Criteria> listCriterias = new ArrayList<>();

    private List<String> listOrder = new ArrayList<>();

    private EasyParams params = new EasyParams();

    public Where() {
    }

    /**
     * @param name     name建议优先使用实体的字段名（原值），其次使用数据库的真实字段名（不区分大小写）
     * @param operator
     * @param value
     */
    public Where(String name, String operator, Object value) {
        and(name, operator, value);
    }

    public Where and(String name, String operator, Object value) {
        listCriterias.add(new Criteria(name, operator, value, true));
        params.put(name, value);
        return this;
    }

    public Where or(String name, String operator, Object value) {
        listCriterias.add(new Criteria(name, operator, value, false));
        params.put(name, value);
        return this;
    }

    public Where orderBy(String name, boolean isAsc) {
        listOrder.add(String.format("%s %s", name, isAsc ? "asc" : "desc"));
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Criteria c : listCriterias) {
            String logicOp = c.getLogicOp();
            String exp;
            if (logicOp.equalsIgnoreCase("is") || logicOp.equalsIgnoreCase("is not")) {
                exp = String.format("%s %s %s", c.getName(), logicOp, c.getValue());
            } else {
                exp = String.format("%s %s @%s", c.getName(), logicOp, c.getName());
            }

            if (sb.length() == 0) {
                sb.append(exp);
            } else {
                sb.append(c.isAnd() ? " and " : " or ").append(exp);
            }
        }
        if (sb.length() > 0)
            sb.insert(0, " where ");
        if (listOrder.size() > 0) {
            sb.append(" order by ").append(StringUtils.concate(listOrder, ","));
        }
        return sb.toString();
    }

    /**
     * 将Criteria转为真正的where条件时，将name转为数据库对应的字段，
     * name建议优先使用实体的字段名（原值），其次使用数据库的真实字段名（不区分大小写）
     *
     * @param mapping        see {@link com.uis.nx.soar.base.jdbc.cache.WhereName2TableFieldCache}
     * @param includeOrderBy 当用作count时，不需要order by，如果存在在postgres中会报错
     * @return
     */
    public String toString(Map<String, String> mapping, boolean includeOrderBy) {
        StringBuilder sb = new StringBuilder();
        for (Criteria c : listCriterias) {
            String name = c.getName();
            String dbFieldName = mapping.get(name);
            if (dbFieldName == null) {
                dbFieldName = mapping.get(name.toLowerCase());
                if (dbFieldName == null) {
                    System.out.println(String.format("字段【%s】没有对应的数据库表字段，使用【%s】作为运算符左侧部分", name, name));
                }
            }

            String logicOp = c.getLogicOp();
            String exp;
            if (logicOp.equalsIgnoreCase("is") || logicOp.equalsIgnoreCase("is not")) {
                exp = String.format("%s %s %s", dbFieldName == null ? name : dbFieldName, c.getLogicOp(), c.getValue());
            } else {
                exp = String.format("%s %s @%s", dbFieldName == null ? name : dbFieldName, c.getLogicOp(), c.getName());
            }


            if (sb.length() == 0) {
                sb.append(exp);
            } else {
                sb.append(c.isAnd() ? " and " : " or ").append(exp);
            }
        }
        if (sb.length() > 0)
            sb.insert(0, " where ");
        if (listOrder.size() > 0 && includeOrderBy) {
            sb.append(" order by ").append(StringUtils.concate(listOrder, ","));
        }
        return sb.toString();
    }

    public EasyParams params() {
        return this.params;
    }

}
