package com.ops.zen.sqlwindow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseVo {

    /** 数据库名称 */
    private String databaseName;
}
