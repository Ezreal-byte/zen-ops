package com.ops.zen.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xyn
 * @date 2025/4/23 16:50
 * @description
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableInfo {

    /**
     * 表目录（可能为 null）
     */
    private String tableCat;
    /**
     * 表模式（可能为 null）
     */
    private String tableSchem;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 表类型。典型类型有 "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"
     */
    private String tableType;
    /**
     * 表的解释性注释
     */
    private String remarks;
    /**
     * 类型目录（可能为 null）
     */
    private String typeCat;
    /**
     * 类型模式（可能为 null）
     */
    private String typeSchem;
    /**
     * 类型名称（可能为 null）
     */
    private String typeName;
    /**
     * 类型化表的指定 "标识符" 列的名称（可能为 null）
     */
    private String selfReferencingColName;
    /**
     * 指定 SELF_REFERENCING_COL_NAME 中的值是如何创建的。值为 "SYSTEM", "USER", "DERIVED"（可能为 null）
     */
    private String refGeneration;

    /**
     * 表列信息
     */
    private List<TableColumnInfo> columns;

    /**
     * 表索引信息
     */
    private List<TableIndexInfo> indexes;
}
