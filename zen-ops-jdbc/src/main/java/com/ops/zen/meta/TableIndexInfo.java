package com.ops.zen.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.DatabaseMetaData;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableIndexInfo {
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
     * 索引值是否可以非唯一。当 TYPE 是 tableIndexStatistic 时为 false
     */
    private boolean nonUnique;
    /**
     * 索引目录（可能为 null）；当 TYPE 是 tableIndexStatistic 时为 null
     */
    private String indexQualifier;
    /**
     * 索引名；当 TYPE 是 tableIndexStatistic 时为 null
     */
    private String indexName;
    /**
     * 索引类型：
     * tableIndexStatistic - 标识与表的索引描述一起返回的表统计信息 {@link DatabaseMetaData#tableIndexStatistic}
     * tableIndexClustered - 这是一个聚集索引 {@link DatabaseMetaData#tableIndexClustered}
     * tableIndexHashed - 这是一个哈希索引 {@link DatabaseMetaData#tableIndexHashed}
     * tableIndexOther - 这是其他类型的索引 {@link DatabaseMetaData#tableIndexOther}
     */
    private short type;
    /**
     * 索引内列的序号；当 TYPE 是 tableIndexStatistic 时为零
     */
    private short ordinalPosition;
    /**
     * 列名；当 TYPE 是 tableIndexStatistic 时为 null
     */
    private String columnName;
    /**
     * 列排序顺序，"A" 表示升序，"D" 表示降序，如果不支持排序顺序则可能为 null；当 TYPE 是 tableIndexStatistic 时为 null
     */
    private String ascOrDesc;
    /**
     * 当 TYPE 是 tableIndexStatistic 时，这是表中的行数；否则，这是索引中的唯一值的数量。
     */
    private long cardinality;
    /**
     * 当 TYPE 是 tableIndexStatistic 时，这是表使用的页数；否则，这是当前索引使用的页数。
     */
    private long pages;
    /**
     * 过滤条件（如果有）。（可能为 null）
     */
    private String filterCondition;
}
