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
 * @Date 2021/11/16 9:41
 * @Description
 */
public class PhyTableToolMysql extends AbstractPhyTableTool {

    /**
     * 元数据中的字段类型到Mysql的字段类型映射
     */
    Map<String, String> METATP2DBTP = new HashMap<>();

    {
        METATP2DBTP.put(EtlFieldTpEn.DATETIME, "datetime");
        METATP2DBTP.put(EtlFieldTpEn.NUMBER, "decimal");//等效于numeric
        METATP2DBTP.put(EtlFieldTpEn.STRING, "varchar");
        METATP2DBTP.put(EtlFieldTpEn.BLOB, "longblob");
        METATP2DBTP.put(EtlFieldTpEn.CLOB, "longtext");
        METATP2DBTP.put(EtlFieldTpEn.UNSUPPORT, EtlFieldTpEn.UNSUPPORT);
    }


    @Override
    String column(EtlFieldMeta f) {
        // `c` datetime NOT NULL DEFAULT 'AA' COMMENT 'asdf'";
        String format = "%s %s %s %s %s"; // TODO 当前时间默认值是否要特殊处理
        String nullPart = f.isNullable() ? "NULL" : "NOT NULL";
        String defaultPart = buildDefaultPart(f);
        String typePart = buildType(f);
        String commentPart = StringUtils.isNotEmpty(f.getDes()) ? "COMMENT '" + f.getDes() + "'" : "";
        return String.format(format, f.getName(), typePart, nullPart, defaultPart, commentPart);
    }

    private String buildDefaultPart(EtlFieldMeta f) {
        String type = f.getType();
        // 如果类型是String或者Clob  默认值需要+单引号
        if (type.equals(EtlFieldTpEn.STRING) || type.equals(EtlFieldTpEn.CLOB)) {
            return StringUtils.isNotEmpty(f.getDefValue()) ? "DEFAULT '" + f.getDefValue() + "'" : "";
        }
        return StringUtils.isNotEmpty(f.getDefValue()) ? "DEFAULT " + f.getDefValue() : "";
    }

    private String buildType(EtlFieldMeta f) {
        String dbType = METATP2DBTP.get(f.getType());
        if (f.getType().equals(EtlFieldTpEn.STRING)) {
            dbType += "(" + f.getPrecision() + ")";
        } else if (f.getType().equals(EtlFieldTpEn.NUMBER)) {
            dbType += "(" + f.getPrecision() + "," + f.getScale() + ")";
        } else if (f.getType().equals(EtlFieldTpEn.BLOB)) {
        } else if (f.getType().equals(EtlFieldTpEn.CLOB)) {
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
        return StringUtils.isBlank(meta.getDes()) ? null : String.format("ALTER TABLE %s COMMENT='%s';\n", meta.getName(), meta.getDes());
    }

    @Override
    String columnComment(EtlTableMeta tbMeta, EtlFieldMeta meta, String dbName) {
        return null;
    }

    @Override
    String pk(EtlTableMeta meta) {
        return String.format("ALTER TABLE %s ADD CONSTRAINT PK_%S PRIMARY KEY (%s) ", meta.getName(), meta.getName(), meta.getPkName());
    }

    @Override
    protected String index(String name, int idx, EtlTableMeta.TableIndex ti) {
        String tableName = name;
        String format = "ALTER TABLE %s ADD INDEX %s (%s)";
        String indexName = "idx_" + tableName + "_" + StringUtils.collConcate(ti.getCols(), "_");
        return String.format(format, tableName, indexName, StringUtils.collConcate(ti.getCols(), ", "));
    }

    @Override
    String dropIndex(String name, int idx, EtlTableMeta.TableIndex ti) {
        String tableName = name;
//        String format = "ALTER TABLE %s ADD INDEX %s (%s)";
        String indexName = "idx_" + tableName + "_" + StringUtils.collConcate(ti.getCols(), "_");
        return String.format("DROP INDEX %s ON %s", indexName, tableName);
    }

    /**
     * mysql 的查询当前用户表的SQL
     *
     * @param dbName
     * @return
     */
    @Override
    public String getSelectTableNameSql(String dbName) {
        Assert.notNull(dbName, "该mysql数据源不能缺少数据库名称");
        return "select TABLE_NAME as value, TABLE_NAME as label from information_schema.tables where table_schema='" + dbName + "' and table_type='base table' and TABLE_NAME like @keyword";
    }

    @Override
    public boolean hasTable(Jdbc jdbc, EtlTableMeta meta, String dbName) {
        Assert.notNull(dbName, "该mysql数据源不能缺少数据库名称");
        EasyParams params = new EasyParams().put("tableName", meta.getName().toLowerCase());
        String sql = "select TABLE_NAME from information_schema.tables where table_schema='" + dbName + "' and table_type='base table' and TABLE_NAME = @tableName";
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
        meta.setCols(EtlFieldMetaHelper.fieldsMetaByTableName(jdbc.getDataSource(), tableName, false));
        meta.setName(tableName);
        meta.setDes(des);
        EasyParams params = new EasyParams().put("tableName", tableName).put("tableSchema", dbName);
        // 通过SQL查询主键 begin
        String pkSql = "SELECT column_name pk FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE table_name=@tableName AND table_schema=@tableSchema AND constraint_name='PRIMARY'";
        meta.setPkName(getPkNameBySql(jdbc, pkSql, params));
        //end
        //通过SQL查询索引 begin
        String indexSql = "select INDEX_NAME indexName , GROUP_CONCAT(COLUMN_NAME) columnName \n" +
                "from \n" +
                "information_schema.statistics \n" +
                "where \n" +
                "table_schema=@tableSchema and TABLE_NAME = @tableName \n" +
                "GROUP BY INDEX_NAME ";
        //结果集示例
        //indexName              |columnName|
        //-----------------------+----------+
        //idx_TEST_DDDD_CODE_NAME|CODE,NAME |
        //idx_TEST_DDDD_PK_SRV   |PK_SRV    |
        //PRIMARY                |PK_SRV    |
        List<IndexQueryVO> indexList = jdbc.query(IndexQueryVO.class, indexSql, params, null);
        List<EtlTableMeta.TableIndex> tableIndices = new ArrayList<>();
        indexList.stream().filter(vo -> !"PRIMARY".equals(vo.getIndexName())).forEach(vo -> {
            String[] split = vo.getColumnName().split(",");
            EtlTableMeta.TableIndex index = new EtlTableMeta.TableIndex();
            Set<String> set = new HashSet<>();
            Collections.addAll(set, split);
            index.setCols(set);
            tableIndices.add(index);
        });
        meta.setIndexs(tableIndices);
        //end
        return meta;
    }

    @Override
    public Pair<String, String> getPrimaryColumNameType(Jdbc jdbc, String tableName) {
        Assert.notEmpty(tableName, "getPrimaryColumNameType: tableName is not be null");
        // TODO  SQL 未验证
        String sql = "SELECT COLUMN_NAME, DATA_TYPE\n" +
                "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                "WHERE TABLE_NAME = @tableName AND COLUMN_KEY = 'PRI'";
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
        String sql = "SELECT table_name AS table_name, table_comment comments FROM information_schema.tables";
        List<EasyRecord> list = jdbc.query(sql, null, null);
        return list.stream().map(r -> new TableVO(r.getString("table_name"), r.getString("comments"))).collect(Collectors.toList());
    }
}
