package com.ops.zen.sqlwindow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列元数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetaVo {

    /** 列名 */
    private String name;

    /** 简化类型：STRING/NUMBER/DATETIME/BLOB/CLOB/BOOLEAN/UNSUPPORT */
    private String type;

    /** 数据库真实类型，如 mysql 的 tinyint */
    private String colType;

    /** java.sql.Types */
    private Integer sqlType;

    /** 精度/长度 */
    private Integer precision;

    /** 小数位 */
    private Integer scale;
}
