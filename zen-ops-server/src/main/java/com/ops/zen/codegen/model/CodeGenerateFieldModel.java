package com.ops.zen.codegen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xyn
 * @date 2025/4/23 18:00
 * @description
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeGenerateFieldModel {

    /**
     * 是否主键
     */
    private boolean pk;

    /**
     * 字段注释
     */
    private String remarks;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段名驼峰
     */
    private String fieldNameCamel;

    /**
     * 字段名 驼峰 第一个字母大写
     */
    private String fieldNameCamelFirstUpper;


    /**
     * 来自 java.sql.Types 的 SQL 类型
     */
    private int dataType;
    /**
     * 数据源相关的类型名称，对于 UDT，类型名称是完全限定的
     */
    private String typeName;

    private String javaType;

    /**
     * 列大小
     */
    private int columnSize;

    /**
     * 使用 ISO 规则确定列的可空性
     *  - YES - 列可以包含 NULL -> true
     *  - NO - 列不能包含 NULL ->  false
     *  - 空字符串 - 列的可空性未知 -> null
     */
    private Boolean isNullable;

    /**
     * 是否作为查询条件
     */
    private boolean query;

    /**
     * 查询条件匹配规则
     *
     * LIKE EQUAL (BETWEEN IN 暂未实现)
     */
    private String queryMatch = "EQUAL";

    /**
     * 是否在表格中显示
     */
    private boolean table;

    /**
     * 是否在表单中显示
     */
    private boolean form;

    /**
     * 是否使用下拉框
     */
    private boolean select;

    /**
     * 下拉id
     */
    private String selectId;

    /**
     * 是否必填
     */
    private boolean required;

}
