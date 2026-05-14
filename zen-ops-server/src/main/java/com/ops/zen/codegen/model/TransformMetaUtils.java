package com.ops.zen.codegen.model;

import com.ops.zen.meta.TableColumnInfo;
import com.ops.zen.meta.TableInfo;
import com.ops.zen.utils.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xyn
 * @date 2025/4/23 18:38
 * @description
 **/
public class TransformMetaUtils {


    public static CodeGenerateModel transform(TableInfo tableInfo) {
        CodeGenerateModel model = new CodeGenerateModel();
        TableColumnInfo pkColumn = tableInfo.getColumns().stream().filter(TableColumnInfo::isPrimaryKey).findFirst().orElseGet(null);
        model.setRemarks(tableInfo.getRemarks());
        model.setTableName(tableInfo.getTableName());
        model.setTableNameCamel(StringUtils.dbField2Camel(tableInfo.getTableName(), false));
        model.setTableNameCamelFirstUpper(StringUtils.dbField2Camel(tableInfo.getTableName(), true));
        if (Objects.nonNull(pkColumn)) {
            model.setPkName(pkColumn.getColumnName());
            model.setPkNameCamel(StringUtils.dbField2Camel(pkColumn.getColumnName(), false));
            model.setPkNameCamelFirstUpper(StringUtils.dbField2Camel(pkColumn.getColumnName(), true));
        }
        model.setControllerRequestMapping(tableInfo.getTableName());
        List<CodeGenerateFieldModel> fields = tableInfo.getColumns().stream().map(column -> {
            CodeGenerateFieldModel field = new CodeGenerateFieldModel();
            field.setPk(column.isPrimaryKey());
            field.setRemarks(column.getRemarks());
            field.setFieldName(column.getColumnName());
            field.setFieldNameCamel(StringUtils.dbField2Camel(column.getColumnName(), false));
            field.setFieldNameCamelFirstUpper(StringUtils.dbField2Camel(column.getColumnName(), true));
            field.setDataType(column.getDataType());
            field.setTypeName(column.getTypeName());
            field.setJavaType(TypeMapping.toJavaType(column));
            field.setColumnSize(column.getColumnSize());
            field.setIsNullable(column.getIsNullable());
            return field;
        }).collect(Collectors.toList());
        model.setFields(fields);
        return model;
    }

}
