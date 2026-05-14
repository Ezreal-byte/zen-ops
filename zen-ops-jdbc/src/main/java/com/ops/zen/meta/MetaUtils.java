package com.ops.zen.meta;

import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.utils.JsonUtils;
import com.ops.zen.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author xyn
 * @date 2025/4/23 16:57
 * @description
 **/
@Slf4j
public class MetaUtils {


    /**
     * 获取表字段信息
     * @param jdbc
     * @param schema
     * @param tableName
     * @return
     */
    public static List<TableColumnInfo> columnInfos(Jdbc jdbc, String schema, String tableName) {
        try (Connection connection = jdbc.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            List<TableColumnInfo> columnInfos = new ArrayList<>();
            ResultSet columns = metaData.getColumns(schema, schema, tableName, null);
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, schema, tableName);
            Set<String> primaryKeySet = new java.util.HashSet<>();
            while (primaryKeys.next()) {
                primaryKeySet.add(primaryKeys.getString("COLUMN_NAME"));
            }
            while (columns.next()) {
                columnInfos.add(build2ColumnInfo(columns, primaryKeySet));
            }
           return columnInfos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取表信息
     * @param jdbc
     * @param schema
     * @param tableName
     * @return
     */
    public static TableInfo tableInfo(Jdbc jdbc, String schema, String tableName) {
        try (Connection connection = jdbc.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(schema, schema, tableName, null);
            List<TableInfo> tableInfos = new ArrayList<>();
            while (tables.next()) {
                String remarks = tables.getString("REMARKS");
                tableInfos.add(TableInfo.builder()
                                .tableCat(tables.getString("TABLE_CAT"))
                                .tableSchem(schema)
                                .tableType(tables.getString("TABLE_TYPE"))
                                .remarks(remarks)
                                .tableName(tables.getString("TABLE_NAME"))
                                .typeCat(tables.getString("TYPE_CAT"))
                                .typeSchem(tables.getString("TYPE_SCHEM"))
                                .typeName(tables.getString("TYPE_NAME"))
                                .selfReferencingColName(tables.getString("SELF_REFERENCING_COL_NAME"))
                                .refGeneration(tables.getString("REF_GENERATION"))
                                .columns(columnInfos(jdbc, schema, tableName))
                                .indexes(indexInfos(jdbc, schema, tableName))
                                .build());
            }
            if (CollectionUtils.isEmpty(tableInfos)) {
                return null;
            } else if (tableInfos.size() == 1) {
                return tableInfos.get(0);
            } else {
                log.error("schema: {}, tableName:{} has more than one tableInfo, tableInfos ===> {}", schema, tableName, JsonUtils.toJSONString(tableInfos));
                throw new RuntimeException(String.format("schema: %s, tableName:%s has more than one tableInfo", schema, tableName));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<TableIndexInfo> indexInfos(Jdbc jdbc, String schema, String tableName) {
        try (Connection connection = jdbc.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet indexInfos = metaData.getIndexInfo(schema, schema, tableName, false, false);
            List<TableIndexInfo> tableIndexInfos = new ArrayList<>();
            while (indexInfos.next()) {
                tableIndexInfos.add(TableIndexInfo.builder()
                        .tableCat(indexInfos.getString("TABLE_CAT"))
                        .tableSchem(indexInfos.getString("TABLE_SCHEM"))
                        .tableName(indexInfos.getString("TABLE_NAME"))
                        .nonUnique(indexInfos.getBoolean("NON_UNIQUE"))
                        .indexQualifier(indexInfos.getString("INDEX_QUALIFIER"))
                        .indexName(indexInfos.getString("INDEX_NAME"))
                        .type(indexInfos.getShort("TYPE"))
                        .ordinalPosition(indexInfos.getShort("ORDINAL_POSITION"))
                        .columnName(indexInfos.getString("COLUMN_NAME"))
                        .ascOrDesc(indexInfos.getString("ASC_OR_DESC"))
                        .cardinality(indexInfos.getLong("CARDINALITY"))
                        .pages(indexInfos.getLong("PAGES"))
                        .filterCondition(indexInfos.getString("FILTER_CONDITION"))
                        .build()
                );
            }
            return tableIndexInfos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static TableColumnInfo build2ColumnInfo(ResultSet columns, Set<String> primaryKeySet) throws SQLException {
        String columnName = columns.getString("COLUMN_NAME");
        String isNullable = columns.getString("IS_NULLABLE");
        String isAutoincrement = columns.getString("IS_AUTOINCREMENT");
        String isGeneratedcolumn = columns.getString("IS_GENERATEDCOLUMN");
        return TableColumnInfo.builder()
                .primaryKey(primaryKeySet.contains(columnName))
                .tableCat(columns.getString("TABLE_CAT"))
                .tableSchem(columns.getString("TABLE_SCHEM"))
                .tableName(columns.getString("TABLE_NAME"))
                .columnName(columnName)
                .dataType(columns.getInt("DATA_TYPE"))
                .typeName(columns.getString("TYPE_NAME"))
                .columnSize(columns.getInt("COLUMN_SIZE"))
                .decimalDigits(columns.getInt("DECIMAL_DIGITS"))
                .numPrecRadix(columns.getInt("NUM_PREC_RADIX"))
                .nullable(columns.getInt("NULLABLE"))
                .remarks(columns.getString("REMARKS"))
                .columnDef(columns.getString("COLUMN_DEF"))
                .sqlDataType(columns.getInt("SQL_DATA_TYPE"))
                .sqlDatetimeSub(columns.getInt("SQL_DATETIME_SUB"))
                .charOctetLength(columns.getInt("CHAR_OCTET_LENGTH"))
                .ordinalPosition(columns.getInt("ORDINAL_POSITION"))
                .isNullable(StringUtils.isEmpty(isNullable) ? null : "YES".equals(isNullable))
                .scopeCatalog(columns.getString("SCOPE_CATALOG"))
                .scopeSchema(columns.getString("SCOPE_SCHEMA"))
                .scopeTable(columns.getString("SCOPE_TABLE"))
                .sourceDataType(columns.getShort("SOURCE_DATA_TYPE"))
                .isAutoincrement(StringUtils.isEmpty(isAutoincrement) ? null : "YES".equals(isAutoincrement))
                .isGeneratedColumn(StringUtils.isEmpty(isGeneratedcolumn) ? null : "YES".equals(isGeneratedcolumn))
                .build();
    }


}
