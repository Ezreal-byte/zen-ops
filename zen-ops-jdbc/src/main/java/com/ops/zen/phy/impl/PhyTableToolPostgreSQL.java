package com.ops.zen.phy.impl;

import com.ops.zen.cache.Pair;
import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.phy.meta.EtlFieldMeta;
import com.ops.zen.phy.meta.EtlFieldMetaHelper;
import com.ops.zen.phy.vo.EtlFieldTpEn;
import com.ops.zen.phy.vo.EtlTableMeta;
import com.ops.zen.phy.vo.IndexQueryVO;
import com.ops.zen.phy.vo.TableVO;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.CollectionUtils;
import com.ops.zen.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Ezreal
 * @Date 2022/07/28 16:07
 * @Description PG方言适配
 */
public class PhyTableToolPostgreSQL extends AbstractPhyTableTool {

    /**
     * 元数据中的字段类型到Oracle的字段类型映射
     */
    Map<String, String> METATP2DBTP = new HashMap<>();

    {
        /*
            timestamptz有时区类型的时间戳：
            在东8区写入时间2024-44-11 14:34:30
            在东9区读出时间2024-44-11 15:34:30

            timestamp没有时区类型的时间戳，存入的时间取出时在服务器时区变化后不会变化：
            在任何时区写入时间2024-44-11 14:34:30
            在任何时区读出时间2024-44-11 14:34:30
         */
        METATP2DBTP.put(EtlFieldTpEn.DATETIME, "timestamp");
        METATP2DBTP.put(EtlFieldTpEn.NUMBER, "numeric");
        METATP2DBTP.put(EtlFieldTpEn.STRING, "varchar");
        METATP2DBTP.put(EtlFieldTpEn.UNSUPPORT, EtlFieldTpEn.UNSUPPORT);
        METATP2DBTP.put(EtlFieldTpEn.CLOB, "text");
    }

    @Override
    String column(EtlFieldMeta f) {
        // 格式："dt_created DATE default sysdate not null ";
        String format = "%s %s %s %s";
        String typePart = buildType(f);
        String defValue = EtlFieldTpEn.DATETIME.equals(f.getType()) ? f.getDefValue() : "'" + f.getDefValue() + "'";
        String defaultPart = StringUtils.isNotEmpty(f.getDefValue()) ? "default " + defValue : "";
        String nullPart = f.isNullable() ? "" : "not null";
        return String.format(format, f.getName(), typePart, defaultPart, nullPart);
    }

    private String buildType(EtlFieldMeta f) {
        String dbType = METATP2DBTP.get(f.getType());
        if (f.getType().equals(EtlFieldTpEn.STRING)) {
            dbType += "(" + f.getPrecision() + ")";
        } else if (f.getType().equals(EtlFieldTpEn.NUMBER)) {
            dbType += "(" + f.getPrecision() + "," + f.getScale() + ")";
        } else if (f.getType().equals(EtlFieldTpEn.DATETIME)) {
            dbType += "(6)";//TODO  pg的日期类型精度默认是6 ?
        } else if (f.getType().equals(EtlFieldTpEn.UNSUPPORT)) {
            throw new RuntimeException("不允许不支持的类型：" + EtlFieldTpEn.UNSUPPORT);
        }
        return dbType;
    }

    @Override
    String createBasicTableSuffix(EtlTableMeta meta) {
        return null;
    }

    @Override
    String tableComment(EtlTableMeta meta) {
        return StringUtils.isNotBlank(meta.getDes()) ? String.format("comment on table %s is '%s'", meta.getName(), meta.getDes()) : "";
    }

    @Override
    String columnComment(EtlTableMeta tbMeta, EtlFieldMeta meta, String dbName) {
        return StringUtils.isBlank(meta.getDes()) ? null : String.format("comment on column %s.%s is '%s'", tbMeta.getName(), meta.getName(), meta.getDes());
    }

    @Override
    String pk(EtlTableMeta meta) {
        return String.format("alter table %s add constraint PK_%s primary key (%s)", meta.getName(), meta.getName(), meta.getPkName());
    }

    @Override
    protected String index(String name, int idx, EtlTableMeta.TableIndex ti) {
        String tableName = name;
        String format = "create %sindex %s on %s (%s) %s";
        String indexName = (ti.isUnique() ? "UNIQUE_" : "IDX_") + tableName + "_" + StringUtils.collConcate(ti.getCols(), "_");
        String tableSpace = ti.getTableSpace();
        if (StringUtils.isEmpty(tableSpace)) {
            tableSpace = "";
        } else {
            tableSpace = "tablespace " + tableSpace;
        }
        // 唯一约束语法比创建普通index多一个unique限制
        return String.format(format, ti.isUnique() ? "unique " : "", indexName, tableName, StringUtils.collConcate(ti.getCols(), ", "), tableSpace);
    }

    @Override
    String dropIndex(String name, int idx, EtlTableMeta.TableIndex ti) {
        String tableName = name;
        String indexName = (ti.isUnique() ? "UNIQUE_" : "IDX_") + tableName + "_" + StringUtils.collConcate(ti.getCols(), "_");
        return String.format("drop index %s", indexName);
    }

    @Override
    public String getSelectTableNameSql(String dbName) {
        return "select table_name \"value\", table_name \"label\" from information_schema.tables WHERE table_name like lower( @keyword )";
    }

    @Override
    public boolean hasTable(Jdbc jdbc, EtlTableMeta meta, String dbName) {
        EasyParams params = new EasyParams()
                .put("tableSchema", "public")
                .put("tableType", "BASE TABLE")
                .put("tableName", meta.getName().toLowerCase());
        String sql = "select table_name from information_schema.tables where table_schema = @tableSchema and table_type = @tableType and table_name = @tableName";
        long count = jdbc.count(sql, params, null);
        return count > 0;
    }


    /**
     * 根据表名生成元数据
     * oracle和sqlserver基本一致  只是SQL不同
     *
     * @param jdbc
     * @param dbName
     * @param tableName
     * @param des
     * @return
     */
    @Override
    public EtlTableMeta getMetaByDsTableName(Jdbc jdbc, String dbName, String tableName, String des) {
        EtlTableMeta meta = new EtlTableMeta();
        //是否强制转大写
        boolean colNameUpperCase = true;
        meta.setCols(EtlFieldMetaHelper.fieldsMetaByTableName(jdbc.getDataSource(), tableName, colNameUpperCase));
        meta.setName(tableName);
        meta.setDes(des);
        //set索引
        EasyParams params = new EasyParams().put("tableName", tableName).put("schemaName", "public");
        // 通过SQL查询主键 begin
        String pkSql = "SELECT\n" +
                "    pg_constraint.conname AS pk_name,\n" +
                "    pg_attribute.attname AS pk,\n" +
                "    pg_type.typname AS typename\n" +
                "FROM\n" +
                "    pg_constraint\n" +
                "INNER JOIN pg_class ON pg_constraint.conrelid = pg_class.oid\n" +
                "INNER JOIN pg_attribute ON pg_attribute.attrelid = pg_class.oid\n" +
                "AND pg_attribute.attnum = pg_constraint.conkey [ 1 ]\n" +
                "INNER JOIN pg_type ON pg_type.oid = pg_attribute.atttypid\n" +
                "WHERE\n" +
                "    pg_class.relname = @tableName\n" +
                "AND pg_constraint.contype = 'p';\n";
        String pkName = getPkNameBySql(jdbc, pkSql, params);
        meta.setPkName(StringUtils.isEmpty(pkName) ? pkName : colNameUpperCase ? pkName.toUpperCase() : pkName);
        //end
        //通过SQL查询索引 begin
        String indexSql = "SELECT \n" +
                "--//   A.SCHEMANAME,\n" +
                "--// \tA.TABLENAME,\n" +
                "\tA.INDEXNAME indexName,\n" +
                "--// \tA.TABLESPACE,\n" +
                "--// \tA.INDEXDEF,\n" +
                "--// \tB.AMNAME,\n" +
                "--// \tC.INDEXRELID,\n" +
                "--// \tC.INDNATTS,\n" +
                "\tC.INDISUNIQUE uniqueness,\n" +
                "--// \tC.INDISPRIMARY,\n" +
                "--// \tC.INDISCLUSTERED,\n" +
                "--// \tD.DESCRIPTION,\n" +
                "\tbute.attname columnName\n" +
                "FROM\n" +
                "\tPG_AM B\n" +
                "\tLEFT JOIN PG_CLASS F ON B.OID = F.RELAM\n" +
                "\tINNER JOIN pg_attribute bute ON bute.attrelid = F.oid\n" +
                "\tLEFT JOIN PG_STAT_ALL_INDEXES E ON F.OID = E.INDEXRELID\n" +
                "\tLEFT JOIN PG_INDEX C ON E.INDEXRELID = C.INDEXRELID\n" +
                "\tLEFT OUTER JOIN PG_DESCRIPTION D ON C.INDEXRELID = D.OBJOID,\n" +
                "\tPG_INDEXES A \n" +
                "WHERE\n" +
                "\tA.SCHEMANAME = E.SCHEMANAME \n" +
                "\tAND A.TABLENAME = E.RELNAME \n" +
                "\tAND A.INDEXNAME = E.INDEXRELNAME \n" +
                "\tAND C.INDISPRIMARY = 'f'\n" +
                "\tAND E.SCHEMANAME = @schemaName \n" +
                "\tAND E.RELNAME = @tableName";
        //结果集示例
        //|indexname                   |uniqueness|columnname|
        //+----------------------------+----------+----------+
        //|idx_ipf_srv0118_code_en_type|false     |code      |
        //|idx_ipf_srv0118_code_en_type|false     |en_type   |
        //|idx_ipf_srv0118_code        |false     |code      |
        //|idx_ipf_srv0118_pk_srv      |false     |pk_srv    |
        List<IndexQueryVO> indexList = jdbc.query(IndexQueryVO.class, indexSql, params, null);
        // 索引名称去重
        Set<String> indexNames = indexList.stream().map(IndexQueryVO::getIndexName).collect(Collectors.toSet());
        List<EtlTableMeta.TableIndex> indexesList = new ArrayList<>();
        //遍历去重后的索引名称
        for (String indexName : indexNames) {
            EtlTableMeta.TableIndex index = new EtlTableMeta.TableIndex();
            HashSet<String> cols = new HashSet<>();
            boolean unique = false;
            for (IndexQueryVO vo : indexList) {
                if (indexName.equals(vo.getIndexName())) {
                    cols.add(colNameUpperCase ? vo.getColumnName().toUpperCase() : vo.getColumnName());
                    unique = "true".equals(vo.getUniqueness());
                }
            }
            index.setIdxName(indexName);
            index.setCols(cols);
            index.setUnique(unique);
            indexesList.add(index);
        }
        meta.setIndexs(indexesList);
        //end
        return meta;
    }

    @Override
    public Pair<String, String> getPrimaryColumNameType(Jdbc jdbc, String tableName) {
        Assert.notEmpty(tableName, "getPrimaryColumNameType: tableName is not be null");
        // TODO  SQL 未验证
        String sql = "SELECT a.attname AS column_name, format_type(a.atttypid, a.atttypmod) AS data_type\n" +
                "FROM pg_index i\n" +
                "JOIN pg_attribute a ON a.attrelid = i.indrelid\n" +
                "AND a.attnum = ANY(i.indkey)\n" +
                "WHERE i.indrelid = @tableName::regclass\n" +
                "AND i.indisprimary";
        EasyParams params = new EasyParams().put("tableName", tableName);
        List<EasyRecord> list = jdbc.query(sql, params, null);
        if (CollectionUtils.isEmpty(list)) {
            return new Pair<>();
        }
        EasyRecord record = list.get(0);
        String filedName = record.getString("column_name");
        String fieldType = record.getString("data_type"); //TODO  类型转换
        //字段类型需要转换
        return new Pair<>(filedName, EtlFieldTpEn.NUMBER);
    }

    @Override
    public List<TableVO> userTables(Jdbc jdbc) {
        String sql = "select tablename table_name from pg_tables where schemaname = @schemaname order by tablename";
        List<EasyRecord> list = jdbc.query(sql, new EasyParams().put("schemaname", "soar"), null);
        return list.stream().map(r -> new TableVO(r.getString("table_name"), r.getString("comments"))).collect(Collectors.toList());
    }
}
