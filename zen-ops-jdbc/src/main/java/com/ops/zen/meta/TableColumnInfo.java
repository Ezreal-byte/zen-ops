package com.ops.zen.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.DatabaseMetaData;

/**
 * 表示表列信息的实体类，包含了表和列的各种属性。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableColumnInfo {

    /**
     * 是否为主键
     */
    private boolean primaryKey = false;
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
     * 列名
     */
    private String columnName;
    /**
     * 来自 java.sql.Types 的 SQL 类型
     */
    private int dataType;
    /**
     * 数据源相关的类型名称，对于 UDT，类型名称是完全限定的
     */
    private String typeName;
    /**
     * 列大小
     */
    private int columnSize;
    /**
     * 小数位数。对于不适用 DECIMAL_DIGITS 的数据类型，返回 null
     */
    private int decimalDigits;
    /**
     * 基数（通常为 10 或 2）
     */
    private int numPrecRadix;
    /**
     * 是否允许为 NULL
     *  - columnNoNulls - 可能不允许 NULL 值 {@link DatabaseMetaData#columnNoNulls}
     *  - columnNullable - 肯定允许 NULL 值 {@link DatabaseMetaData#columnNullable}
     *  - columnNullableUnknown - 可空性未知 {@link DatabaseMetaData#columnNullableUnknown}
     */
    private int nullable;
    /**
     * 描述列的注释（可能为 null）
     */
    private String remarks;
    /**
     * 列的默认值，当值用单引号括起来时应解释为字符串（可能为 null）
     */
    private String columnDef;
    /**
     * 未使用
     */
    private int sqlDataType;
    /**
     * 未使用
     */
    private int sqlDatetimeSub;
    /**
     * 对于字符类型，列中的最大字节数
     */
    private int charOctetLength;
    /**
     * 列在表中的索引（从 1 开始）
     */
    private int ordinalPosition;
    /**
     * 使用 ISO 规则确定列的可空性
     *  - YES - 列可以包含 NULL -> true
     *  - NO - 列不能包含 NULL ->  false
     *  - 空字符串 - 列的可空性未知 -> null
     */
    private Boolean isNullable;
    /**
     * 引用属性作用域的表的目录（如果 DATA_TYPE 不是 REF，则为 null）
     */
    private String scopeCatalog;
    /**
     * 引用属性作用域的表的模式（如果 DATA_TYPE 不是 REF，则为 null）
     */
    private String scopeSchema;
    /**
     * 引用属性作用域的表名（如果 DATA_TYPE 不是 REF，则为 null）
     */
    private String scopeTable;
    /**
     * 不同类型或用户生成的 Ref 类型的源类型，来自 java.sql.Types 的 SQL 类型（如果 DATA_TYPE 不是 DISTINCT 或用户生成的 REF，则为 null）
     */
    private Short sourceDataType;
    /**
     * 指示此列是否为自动递增
     *  - YES - 列是自动递增的
     *  - NO - 列不是自动递增的
     *  - 空字符串 - 无法确定列是否为自动递增
     */
    private Boolean isAutoincrement;
    /**
     * 指示此列是否为生成列
     *  - YES - 这是一个生成列
     *  - NO - 这不是一个生成列
     *  - 空字符串 - 无法确定这是否为生成列
     */
    private Boolean isGeneratedColumn;
}
