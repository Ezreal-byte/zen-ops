package com.ops.zen.phy.impl;


import com.ops.zen.cache.Pair;
import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.phy.meta.EtlFieldMetaHelper;
import com.ops.zen.phy.meta.EtlFieldMeta;
import com.ops.zen.phy.vo.EtlFieldTpEn;
import com.ops.zen.phy.vo.EtlTableMeta;
import com.ops.zen.phy.vo.TableVO;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Ezreal
 * @Date 2021/11/25 16:07
 * @Description
 */
public class PhyTableToolClickHouse extends AbstractPhyTableTool {

    /**
     * 元数据中的字段类型到Oracle的字段类型映射
     */
    Map<String, String> METATP2DBTP = new HashMap<>();

    {
        METATP2DBTP.put(EtlFieldTpEn.DATETIME, "DateTime");
        METATP2DBTP.put(EtlFieldTpEn.NUMBER, "UInt64");
        METATP2DBTP.put("Float", "Float64 ");//如果前端的数字，存在精度  则使用Float64
        METATP2DBTP.put(EtlFieldTpEn.STRING, "String");
        METATP2DBTP.put(EtlFieldTpEn.CLOB, "String");
        METATP2DBTP.put(EtlFieldTpEn.UNSUPPORT, EtlFieldTpEn.UNSUPPORT);
    }

    //ALTER TABLE tutorial.IPF_INTF_SRV_MON ADD COLUMN Column1 String;
    @Override
    String column(EtlFieldMeta f) {
        // 格式："dt_created DATE default sysdate not null ";
        String format = "%s %s %s %s"; // TODO 当前时间默认值是否要特殊处理
        String typePart = buildType(f);
        String defaultPart = StringUtils.isNotEmpty(f.getDefValue()) ? "DEFAULT '" + f.getDefValue() + "'" : "";
        String nullPart = f.isNullable() ? "" : "not null";
        return String.format(format, f.getName().toUpperCase(), typePart, defaultPart, nullPart);
    }

    /**
     * 分区默认使用DT_CREATED
     *
     * @param meta
     * @return
     */
    @Override
    String createBasicTableSuffix(EtlTableMeta meta) {
        String format = "ENGINE = MergeTree \n" +
                "PARTITION BY toYYYYMM(%s) \n" +
                "PRIMARY KEY %s ";
        String defaultPartition = "DT_CREATED";
        List<EtlFieldMeta> filter = meta.getCols().stream().filter(col -> defaultPartition.equals(col.getName())).collect(Collectors.toList());
        String partition = "";
        if (filter.size() == 1) {
            partition = defaultPartition;
        } else {
            for (EtlFieldMeta col : meta.getCols()) {
                if (col.getName().startsWith("DT_")) {
                    partition = col.getName();
                    break;
                }
            }
        }
        String pkName = meta.getPkName();
        if (StringUtils.isBlank(pkName))
            throw new RuntimeException("[pkName] cannot be null; clickhouse table must have PK");
        return String.format(format, partition, pkName.toUpperCase());
    }

    private String buildType(EtlFieldMeta f) {
        String dbType = METATP2DBTP.get(f.getType());
        if (f.getType().equals(EtlFieldTpEn.STRING)) {
//            dbType += "(" + f.getPrecision() + ")";
        } else if (f.getType().equals(EtlFieldTpEn.NUMBER) && f.getScale() == 0) {//int
        } else if (f.getType().equals(EtlFieldTpEn.NUMBER) && f.getScale() != 0) { //浮点
            dbType = "Float64";
        } else if (f.getType().equals(EtlFieldTpEn.UNSUPPORT)) {
            throw new RuntimeException("不允许不支持的类型：" + EtlFieldTpEn.UNSUPPORT);
        }
        return dbType;
    }

    @Override
    String tableComment(EtlTableMeta meta) {
        return null;
    }

    @Override
    String columnComment(EtlTableMeta tbMeta, EtlFieldMeta meta, String dbName) {
        //ALTER TABLE tutorial.ETL_TEST_TB COMMENT COLUMN CODE '注释信息'
        return StringUtils.isBlank(meta.getDes()) ? null : String.format("ALTER TABLE %s.%s COMMENT COLUMN %s '%s' ", dbName, tbMeta.getName(), meta.getName(), meta.getDes());
    }

    @Override
    String pk(EtlTableMeta meta) {
        // PRIMARY KEY PK_MCM_NODE_ENGINE_MON
        return null;
    }


    @Override
    protected String index(String name, int idx, EtlTableMeta.TableIndex ti) {
        //https://clickhouse.com/docs/zh/sql-reference/statements/alter/
        //ALTER TABLE [db].name ADD INDEX name expression TYPE type GRANULARITY value [FIRST|AFTER name]
//        String tableName = name;
//        String format = "create index %s on %s (%s)";
//        String indexName = "idx_" + tableName + "_" + StringUtils.collConcate(ti.getCols(), "_");
        return null;
    }

    @Override
    String dropIndex(String name, int idx, EtlTableMeta.TableIndex ti) {
        //        String tableName = name;
//        String format = "create index %s on %s (%s)";
//        String indexName = "idx_" + tableName + "_" + StringUtils.collConcate(ti.getCols(), "_");
//        return String.format("drop index %s", indexName);
        return null;
    }

    @Override
    public String getSelectTableNameSql(String dbName) {
        Assert.notNull(dbName, "该clickhouse数据源不能缺少数据库名称");
        return "select DISTINCT table value, table label from system.parts where database = '" + dbName + "' AND table like @keyword";
    }

    /**
     * FIXME 已知的问题  如果这个表在ClickHouse中刚创建 还有没存储过数据 那么会导致检测不准确
     * @param jdbc
     * @param meta
     * @param dbName
     * @return
     */
    @Override
    public boolean hasTable(Jdbc jdbc, EtlTableMeta meta, String dbName) {
        Assert.notNull(dbName, "该clickhouse数据源不能缺少数据库名称");
//        EasyParams params = new EasyParams().put("tableName", meta.getName());
//        String sql = "select DISTINCT table from system.parts where database = '" + dbName + "' AND table = @tableName";
        String sql = "select DISTINCT table from system.parts where database = '" + dbName + "'";
        List<EasyRecord> tableList = jdbc.query(sql, null, null);
        List<String> tables = new ArrayList<>();
        tableList.forEach(easyRecord -> tables.add(easyRecord.getString("table").toUpperCase()));
//        long count = jdbc.count(sql, params, null);
        String tableName = meta.getName();
        return tables.contains(tableName.toUpperCase());
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
        //set索引
        //setpk
        return meta;
    }

    public String qualifyDDLDbName(String dbName) {
        Assert.notNull(dbName, "该clickhouse数据源不能缺少数据库名称");
        return dbName.concat(".");
    }

    //未实现
    @Override
    public Pair<String, String> getPrimaryColumNameType(Jdbc jdbc, String tableName) {
        Assert.notEmpty(tableName, "getPrimaryColumNameType: tableName is not be null");
//        String sql = "";
//        EasyParams params = new EasyParams().put("tableName", tableName);
//        List<EasyRecord> list = jdbc.query(sql, params, null);
//        if (CollectionUtils.isEmpty(list)) {
//            return new Pair<>();
//        }
//        EasyRecord record = list.get(0);
//        String filedName = record.getString("COLUMN_NAME");
//        String fieldType = record.getString("DATA_TYPE"); //TODO  类型转换
//        字段类型需要转换
//        return new Pair<>(filedName, EtlFieldTpEn.NUMBER);
        return new Pair<>();
    }

    //FIXME 未验证
    @Override
    public List<TableVO> userTables(Jdbc jdbc) {
        String sql = "SELECT table_name as table_name, table_comment as comments FROM system.tables";
        List<EasyRecord> list = jdbc.query(sql, null, null);
        return list.stream().map(r -> new TableVO(r.getString("table_name"), r.getString("comments"))).collect(Collectors.toList());
    }
}
