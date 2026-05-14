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
 * @Date 2021/11/15 16:07
 * @Description
 */
public class PhyTableToolOracle extends AbstractPhyTableTool {

    /**
     * 元数据中的字段类型到Oracle的字段类型映射
     */
    Map<String, String> METATP2DBTP = new HashMap<>();

    {
        METATP2DBTP.put(EtlFieldTpEn.DATETIME, "DATE");
        METATP2DBTP.put(EtlFieldTpEn.NUMBER, "NUMBER");
        METATP2DBTP.put(EtlFieldTpEn.STRING, "VARCHAR2");
        METATP2DBTP.put(EtlFieldTpEn.UNSUPPORT, EtlFieldTpEn.UNSUPPORT);
        METATP2DBTP.put(EtlFieldTpEn.CLOB, "CLOB");
        METATP2DBTP.put(EtlFieldTpEn.BLOB, "BLOB");
    }

    @Override
    String column(EtlFieldMeta f) {
        // 格式："dt_created DATE default sysdate not null ";
        String format = "%s %s %s %s"; // TODO 当前时间默认值是否要特殊处理
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
        return "select table_name value, table_name label from user_tables WHERE table_name like @keyword";
    }

    @Override
    public boolean hasTable(Jdbc jdbc, EtlTableMeta meta, String dbName) {
        EasyParams params = new EasyParams().put("tableName", meta.getName().toUpperCase());
        String sql = "select table_name from user_tables WHERE table_name = @tableName";
        long count = jdbc.count(sql, params, null);
        return count > 0;
    }

//    public String expdp(ExpdpModel model) {
//        return FreeMarkerUtils.build(PhyTableTool.class, model, "exp-ora.tpl");
//    }

//    public String impdp(ImpdpModel model) {
//        return FreeMarkerUtils.build(PhyTableTool.class, model, "imp-ora.tpl");
//    }

//    public String excludeQuery(ExpdpModel model) {
//        return FreeMarkerUtils.build(PhyTableTool.class, model, "exp-ora-exclude-query.tpl");
//    }

    /**
     * 数据泵-导出模型
     */
//    public static class ExpdpModel {
//
//        private String user;
//
//        private String password;
//
//        private String sid;
//
//        private String dumpFileName;
//
//        private List<String> excludes;
//
//        /**
//         * 部分要导出的表名
//         */
//        private List<String> listExpTables;
//
//        public ExpdpModel(String user, String password, String sid, String dumpFileName, List<String> excludes, List<String> listExpTables) {
//            this.user = user;
//            this.password = password;
//            this.sid = sid;
//            this.dumpFileName = dumpFileName;
//            this.excludes = excludes;
//            this.listExpTables = listExpTables;
//        }
//
//        public String getUser() {
//            return user;
//        }
//
//        public void setUser(String user) {
//            this.user = user;
//        }
//
//        public String getPassword() {
//            return password;
//        }
//
//        public void setPassword(String password) {
//            this.password = password;
//        }
//
//        public String getSid() {
//            return sid;
//        }
//
//        public void setSid(String sid) {
//            this.sid = sid;
//        }
//
//        public String getDumpFileName() {
//            return dumpFileName;
//        }
//
//        public void setDumpFileName(String dumpFileName) {
//            this.dumpFileName = dumpFileName;
//        }
//
//        public List<String> getExcludes() {
//            return excludes;
//        }
//
//        public void setExcludes(List<String> excludes) {
//            this.excludes = excludes;
//        }
//
//        public List<String> getListExpTables() {
//            return listExpTables;
//        }
//
//        public void setListExpTables(List<String> listExpTables) {
//            this.listExpTables = listExpTables;
//        }
//    }

    /**
     * 数据泵- 导入模型
     */
//    public static class ImpdpModel {
//
//
//        private String user;
//
//        private String password;
//
//        private String sid;
//
//        private String dumpFileName;
//
//        private List<String> listImpTables;
//
//        /*
//            impdp yngtts/yngtts DIRECTORY=DATA_PUMP_DIR dumpfile=mydir:yngtts0514.dmp logfile=mydir:yngtts0514.log
//            remap_schema=HR:HR_NEW,SCOTT:SCOTT_NEW
//            remap_tablespace='(TS_YNGT:TS_YNGT,CWFTS:TS_YNGT,TS_EDIDB_DEFAULT:TS_YNGT)'
//         */
//
//
//        private String remapSchema;
//        private String remapTablespace;
//
//
//        public ImpdpModel(String user, String password, String sid, String dumpFileName, List<Pair<String, String>> remapTablespace, List<Pair<String, String>> remapSchema, List<String> listImpTables) {
//            this.user = user;
//            this.password = password;
//            this.sid = sid;
//            this.dumpFileName = dumpFileName;
//            this.listImpTables = listImpTables;
//
//            this.remapSchema = remapSchema.stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(","));
//            this.remapTablespace = String.format("'(%s)'", remapTablespace.stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")));
//
//        }
//
//        public String getUser() {
//            return user;
//        }
//
//        public void setUser(String user) {
//            this.user = user;
//        }
//
//        public String getPassword() {
//            return password;
//        }
//
//        public void setPassword(String password) {
//            this.password = password;
//        }
//
//        public String getSid() {
//            return sid;
//        }
//
//        public void setSid(String sid) {
//            this.sid = sid;
//        }
//
//        public String getDumpFileName() {
//            return dumpFileName;
//        }
//
//        public void setDumpFileName(String dumpFileName) {
//            this.dumpFileName = dumpFileName;
//        }
//
//        public List<String> getListImpTables() {
//            return listImpTables;
//        }
//
//        public void setListImpTables(List<String> listImpTables) {
//            this.listImpTables = listImpTables;
//        }
//
//        public String getRemapSchema() {
//            return remapSchema;
//        }
//
//        public void setRemapSchema(String remapSchema) {
//            this.remapSchema = remapSchema;
//        }
//
//        public String getRemapTablespace() {
//            return remapTablespace;
//        }
//
//        public void setRemapTablespace(String remapTablespace) {
//            this.remapTablespace = remapTablespace;
//        }
//    }

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
        meta.setCols(EtlFieldMetaHelper.fieldsMetaByTableName(jdbc.getDataSource(), tableName, false));
        meta.setName(tableName);
        meta.setDes(des);
        EasyParams params = new EasyParams().put("tableName", tableName);
        //查询字段注释
        String commentsSql = "SELECT COLUMN_NAME columnName, COMMENTS comments FROM USER_COL_COMMENTS WHERE TABLE_NAME = @tableName";
        List<EasyRecord> list = jdbc.query(commentsSql, params, null);
        Map<String, String> columnCommentsMap = new HashMap<>();
        list.forEach(record -> columnCommentsMap.put(record.getString("columnName"), record.getString("comments")));
        for (EtlFieldMeta col : meta.getCols()) {
            String comment = columnCommentsMap.get(col.getName());
            if (StringUtils.isNotBlank(comment)) col.setCnName(comment);
        }
        //set索引
        // 通过SQL查询主键 begin
        String pkSql = "select  a.column_name pk from user_cons_columns a, user_constraints b where a.constraint_name = b.constraint_name  and b.constraint_type = 'P' and a.table_name = @tableName";
        meta.setPkName(getPkNameBySql(jdbc, pkSql, params));
        //end
        //通过SQL查询索引 begin
        String indexSql = "select a.index_name indexName,a.column_name columnName,b.uniqueness from user_ind_columns a, user_indexes b where a.index_name = b.index_name and a.table_name = @tableName";
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
            boolean unique = false;
            for (IndexQueryVO vo : indexList) {
                if (indexName.equals(vo.getIndexName())) {
                    cols.add(vo.getColumnName());
                    unique = "UNIQUE".equals(vo.getUniqueness());
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
        String sql = "SELECT COLUMN_NAME, DATA_TYPE\n" +
                "FROM USER_TAB_COLUMNS\n" +
                "WHERE TABLE_NAME = @tableName AND COLUMN_NAME IN (\n" +
                "    SELECT COLUMN_NAME\n" +
                "    FROM USER_CONS_COLUMNS\n" +
                "    WHERE TABLE_NAME = @tableName AND CONSTRAINT_NAME IN (\n" +
                "        SELECT CONSTRAINT_NAME\n" +
                "        FROM USER_CONSTRAINTS\n" +
                "        WHERE TABLE_NAME = @tableName AND CONSTRAINT_TYPE = 'P'\n" +
                "    )\n" +
                ") ";
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

    @Override
    public List<TableVO> userTables(Jdbc jdbc) {
        String sql = "SELECT t.table_name table_name, c.comments comments FROM user_tables t LEFT JOIN user_tab_comments c ON t.table_name = c.table_name order by t.table_name ASC";
        List<EasyRecord> list = jdbc.query(sql, null, null);
        return list.stream().map(r -> new TableVO(r.getString("table_name"), r.getString("comments"))).collect(Collectors.toList());
    }
}
