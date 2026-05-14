package com.ops.zen.codegen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xyn
 * @date 2025/4/23 17:53
 * @description
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeGenerateModel {

    private String packageName = "com.ops.zen";

    private String moduleName;

    private String urlContext;

    private boolean useEasyJdbc;

    private String remarks;

    private String tableName;

    /**
     * 表名_驼峰
     */
    private String tableNameCamel;

    /**
     * 表名 驼峰 第一个字母大写
     */
    private String tableNameCamelFirstUpper;

    /**
     * 主键名
     */
    private String pkName;

    /**
     * 主键名_驼峰
     */
    private String pkNameCamel;


    private String pkNameCamelFirstUpper;



    private String controllerRequestMapping;


    private List<CodeGenerateFieldModel> fields;

}
