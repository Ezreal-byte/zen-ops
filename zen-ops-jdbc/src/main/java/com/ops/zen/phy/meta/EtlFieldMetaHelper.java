package com.ops.zen.phy.meta;

import com.ops.zen.jdbc.sql.SQL;
import com.ops.zen.phy.vo.EtlFieldTpEn;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author xyn
 * @Date 2021/11/11 9:25
 * @Description
 */
public class EtlFieldMetaHelper {

    //    public static List<EtlFieldMeta> fieldsMeta(DataSource sds, String sql) {
    public static List<EtlFieldMeta> fieldsMeta(DataSource sds, SQL sql, boolean colNameUpperCase) {
        List<DBFieldMeta> dbFieldMetas = QueryMetaHelper.fieldsMeta(sds, sql);
        return toEtlFieldMetas(dbFieldMetas, colNameUpperCase);
    }

    /**
     * 根据 tableName 去数据源 sds 中查询表的字段 转换为 List<EtlFieldMeta>
     *
     * @param sds
     * @param tableName
     * @param colNameUpperCase 字段是否强制转大写
     * @return
     */
    public static List<EtlFieldMeta> fieldsMetaByTableName(DataSource sds, String tableName, boolean colNameUpperCase) {
        String sql = String.format("SELECT * FROM %s", tableName);
        List<DBFieldMeta> dbFieldMetas = QueryMetaHelper.fieldsMeta(sds, sql);
        return toEtlFieldMetas(dbFieldMetas, colNameUpperCase);
    }

    /**
     * 将数据库字段元数据转为etl使用的字段元数据
     *
     * @param metas
     * @param colNameUpperCase
     * @return
     */
    public static List<EtlFieldMeta> toEtlFieldMetas(List<DBFieldMeta> metas, boolean colNameUpperCase) {
        List<EtlFieldMeta> etls = new ArrayList<>();
        metas.forEach(dm -> {
            EtlFieldMeta efm = new EtlFieldMeta();
            efm.name = colNameUpperCase ? dm.getColName().toUpperCase() : dm.getColName();
            efm.precision = dm.getPrecision();
            efm.scale = dm.getScale();
            efm.type = toEtlFieldTp(dm);
            efm.colType = dm.getColType();
            efm.sqlType = dm.getSqlType();
            efm.biAnalyTp = "DIMENSION"; // com.uis.nx.soar.bi2.en.BIFldAnalyTpEn.DIMENSION
            efm.cnName = "维度" + (etls.size() + 1);
            etls.add(efm);
        });
        return etls;
    }

    /**
     * 将数据库元数据字段类型转为{@link EtlFieldTpEn}
     *
     * @param dm
     * @return
     */
    private static String toEtlFieldTp(DBFieldMeta dm) {
        int columnType = dm.getSqlType();
        String etlFieldType = EtlFieldTpEn.UNSUPPORT;
        switch (columnType) {
            case Types.TIMESTAMP:
            case Types.DATE:
                etlFieldType = EtlFieldTpEn.DATETIME;
                break;
            case Types.BOOLEAN:
                // TODO
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                etlFieldType = EtlFieldTpEn.STRING;
                break;
            case Types.CLOB:
                etlFieldType = EtlFieldTpEn.CLOB;
                break;
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                etlFieldType = EtlFieldTpEn.NUMBER;
                break;
            case Types.BLOB:
            case Types.BINARY:
                etlFieldType = EtlFieldTpEn.BLOB;
                break;
            default:
                // TODO
        }
        return etlFieldType;
    }

}
