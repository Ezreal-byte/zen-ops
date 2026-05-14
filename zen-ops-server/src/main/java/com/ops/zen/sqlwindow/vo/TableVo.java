package com.ops.zen.sqlwindow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableVo {

    /** 表名 */
    private String tableName;

    /** 表注释 */
    private String comments;
}
