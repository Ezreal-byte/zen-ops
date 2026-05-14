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
 * @Author xyn
 * @Date 2021/11/16 10:09
 * @Description
 */
public class PhyTableToolMSsql extends AbstractPhyTableTool {

    /**
     * 元数据中的字段类型到Mssql的字段类型映射
     */
    Map<String, String> METATP2DBTP = new HashMap<>();

    {
        METATP2DBTP.put(EtlFieldTpEn.DATETIME, "datetime");
        METATP2DBTP.put(EtlFieldTpEn.NUMBER, "numeric");//等效于numeric
        METATP2DBTP.put(EtlFieldTpEn.STRING, "varchar");
        METATP2DBTP.put(EtlFieldTpEn.UNSUPPORT, EtlFieldTpEn.UNSUPPORT);
    }


    @Override
    String column(EtlFieldMeta f) {
        // `c` datetime NOT NULL DEFAULT 'AA' COMMENT 'asdf'";
        String format = "%s %s %s %s %s"; // TODO 当前时间默认值是否要特殊处理
        String nullPart = f.isNullable() ? "NULL" : "NOT NULL";
        String defaultPart = StringUtils.isNotEmpty(f.getDefValue()) ? "DEFAULT '" + f.getDefValue() + "'" : "";
        String typePart = buildType(f);
        String commentPart = ""; // StringUtils.isNotEmpty(f.getDes()) ? "COMMENT '" + f.getDes() + "'" : "";
        return String.format(format, f.getName(), typePart, nullPart, defaultPart, commentPart);
    }

    private String buildType(EtlFieldMeta f) {
        String dbType = METATP2DBTP.get(f.getType());
        if (f.getType().equals(EtlFieldTpEn.STRING)) {
            dbType += "(" + f.getPrecision() + ")";
        } else if (f.getType().equals(EtlFieldTpEn.NUMBER)) {
            dbType += "(" + f.getPrecision() + "," + f.getScale() + ")";
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
        return null;
    }

    @Override
    String columnComment(EtlTableMeta tbMeta, EtlFieldMeta meta, String dbName) {
        return null;
    }

    @Override
    String pk(EtlTableMeta meta) {
        return String.format("ALTER TABLE %s ADD CONSTRAINT PK_%S PRIMARY KEY CLUSTERED (%s) ", meta.getName(), meta.getName(), meta.getPkName());
    }

    /**
     * TODO 区分聚簇索引和非聚簇索引
     *
     * @param name
     * @param idx
     * @param ti
     * @return
     */
    @Override
    protected String index(String name, int idx, EtlTableMeta.TableIndex ti) {
        String tableName = name;
        String format = "CREATE INDEX %s ON %s (%s)";
        String indexName = "idx_" + tableName + "_" + StringUtils.collConcate(ti.getCols(), "_");
        return String.format(format, indexName, tableName, StringUtils.collConcate(ti.getCols(), ", "));
    }

    @Override
    String dropIndex(String name, int idx, EtlTableMeta.TableIndex ti) {
        String tableName = name;
//        String format = "CREATE INDEX %s ON %s (%s)";
        String indexName = "idx_" + tableName + "_" + StringUtils.collConcate(ti.getCols(), "_");
        return String.format("DROP INDEX %s ON %s", indexName, tableName);
    }

    /**
     * sqlServer 的查询当前用户表的SQL
     * xtype='U':表示所有用户表，xtype='S':表示所有系统表。
     *
     * @return
     * @param dbName
     */
    @Override
    public String getSelectTableNameSql(String dbName) {
        return "select name label, name value from sysobjects where xtype='U' and name like @keyword";
    }

    @Override
    public boolean hasTable(Jdbc jdbc, EtlTableMeta meta, String dbName) {
        EasyParams params = new EasyParams().put("tableName", meta.getName());
        String sql = "select name from sysobjects where xtype='U' and name = @tableName";
        long count = jdbc.count(sql, params, null);
        return count > 0;
    }

    /**
     * 根据表名生成元数据
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
        meta.setName(tableName);
        meta.setDes(des);
        meta.setCols(EtlFieldMetaHelper.fieldsMetaByTableName(jdbc.getDataSource(), tableName, false));
        EasyParams params = new EasyParams().put("tableName", tableName);
        // 通过SQL查询主键 begin
        String pkSql = "select COL_NAME(object_id(@tableName),c.colid) pk\n" +
                "from sysobjects a,sysindexes b,sysindexkeys c\n" +
                "where a.name=b.name and b.id=c.id and b.indid=c.indid\n" +
                "and a.xtype='PK' and a.parent_obj=object_id(@tableName)\n" +
                "and c.id=object_id(@tableName) \n";
        meta.setPkName(getPkNameBySql(jdbc, pkSql, params));
        //end
        //通过SQL查询索引 begin
        String indexSql = "SELECT  a.name indexName, d.name columnName \n" +
                "FROM  sysindexes  a \n" +
                "JOIN  sysindexkeys  b  ON  a.id=b.id  AND  a.indid=b.indid \n" +
                "JOIN  sysobjects  c  ON  b.id=c.id \n" +
                "JOIN  syscolumns  d  ON  b.id=d.id  AND  b.colid=d.colid \n" +
                "WHERE  a.indid  NOT IN(0,255) \n" +
                "AND  c.name = @tableName \n" +
                "ORDER BY  c.name,a.name,d.name";
        //结果集示例
        // indexName                |columnName|
        //-------------------------+----------+
        //idx_ETL_TEST_TB_CODE     |CODE      |
        //idx_ETL_TEST_TB_CODE_NAME|CODE      |
        //idx_ETL_TEST_TB_CODE_NAME|NAME      |
        //PK_ETL_TEST_TB           |PK_SRV    |
        List<IndexQueryVO> indexList = jdbc.query(IndexQueryVO.class, indexSql, params, null);
        // 索引名称去重
        Set<String> indexNames = new HashSet<>();
        for (IndexQueryVO vo : indexList) {
            if (!vo.getIndexName().startsWith("PK")) {
                indexNames.add(vo.getIndexName());
            }
        }
        List<EtlTableMeta.TableIndex> indexesList = new ArrayList<>();
        //遍历去重后的索引名称
        for (String indexName : indexNames) {
            EtlTableMeta.TableIndex index = new EtlTableMeta.TableIndex();
            HashSet<String> cols = new HashSet<>();
            for (IndexQueryVO vo : indexList) {
                if (indexName.equals(vo.getIndexName())) cols.add(vo.getColumnName());
            }
            index.setCols(cols);
            indexesList.add(index);
        }
        meta.setIndexs(indexesList);
        // end
        return meta;
    }

    @Override
    public Pair<String, String> getPrimaryColumNameType(Jdbc jdbc, String tableName) {
        Assert.notEmpty(tableName, "getPrimaryColumNameType: tableName is not be null");
        String sql = "SELECT COLUMN_NAME, DATA_TYPE\n" +
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE\n" +
                "WHERE TABLE_NAME = @tableName AND CONSTRAINT_NAME LIKE 'PK_%'";
        EasyParams params = new EasyParams().put("tableName", tableName);
        List<EasyRecord> list = jdbc.query(sql, params, null);
        if (CollectionUtils.isEmpty(list)) {
            return new Pair<>();
        }
        EasyRecord record = list.get(0);
        String filedName = record.getString("COLUMN_NAME");
        String fieldType = record.getString("DATA_TYPE"); //TODO  类型转换
        //字段类型需要转换
        return new Pair<>(filedName, EtlFieldTpEn.NUMBER);
    }

    //FIXME 未验证
    @Override
    public List<TableVO> userTables(Jdbc jdbc) {
        String sql = "SELECT \n" +
                "    t.name AS table_name,\n" +
                "    ep.value AS comments\n" +
                "FROM \n" +
                "    sys.tables t\n" +
                "LEFT JOIN \n" +
                "    sys.extended_properties ep ON ep.major_id = t.object_id AND ep.minor_id = 0 AND ep.class = 1";
        List<EasyRecord> list = jdbc.query(sql, null, null);
        return list.stream().map(r -> new TableVO(r.getString("table_name"), r.getString("comments"))).collect(Collectors.toList());
    }
}
