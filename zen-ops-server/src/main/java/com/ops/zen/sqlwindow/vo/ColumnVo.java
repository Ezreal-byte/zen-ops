package com.ops.zen.sqlwindow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnVo {

    /** 字段名 */
    private String columnName;

    /** 数据类型 */
    private String dataType;

    /** 字段注释 */
    private String comments;

    /** 默认值 */
    private String columnDefault;

    /** 是否可空 */
    private String isNullable;
}
