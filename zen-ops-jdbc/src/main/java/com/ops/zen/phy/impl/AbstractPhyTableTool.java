package com.ops.zen.phy.impl;

import com.ops.zen.jdbc.EasyRecord;
import com.ops.zen.jdbc.Jdbc;
import com.ops.zen.jdbc.sql.EasyParams;
import com.ops.zen.phy.PhyTableTool;
import com.ops.zen.phy.meta.EtlFieldMeta;
import com.ops.zen.phy.vo.EtlTableMeta;
import com.ops.zen.phy.vo.IndexSqlVO;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.ex.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author xyn
 * @Date 2021/11/15 15:45
 * @Description
 */
public abstract class AbstractPhyTableTool implements PhyTableTool {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPhyTableTool.class);

    @Override
    public void create(Jdbc jdbc, EtlTableMeta meta, String dbName) {
        List<String> sqls = buildCreateSqls(meta, dbName);
        sqls.stream().forEach(sql -> jdbc.execute(sql, null, null));
    }

    @Override
    public List<IndexSqlVO> dropAndCreateIndex(Jdbc jdbc, EtlTableMeta meta) {
        List<IndexSqlVO> sqls = buildDropAndCreateIndexSqls(meta);
        sqls.stream().forEach(vo -> {
            try {
                jdbc.execute(vo.getSql(), null, null);
                vo.setExecStatus(IndexSqlVO.EXEC_STATUS_SUCCESS);
            } catch (Exception e) {
                logger.error("", e);
                vo.setExecStatus(IndexSqlVO.EXEC_STATUS_FAIL);
                vo.setErrMsg(Exceptions.trace(e));
            }
        });
        return sqls;
    }

    @Override
    public IndexSqlVO dropOrCreateIndex(Jdbc jdbc, IndexSqlVO vo) {
        //生成SQL
        String sql = null;
        if (IndexSqlVO.DROP_INDEX.equals(vo.getType())) {
            sql = dropIndex(vo.getName(), vo.getSeq(), vo.getTableIndex());
        } else if  (IndexSqlVO.CREATE_INDEX.equals(vo.getType())) {
            sql = index(vo.getName(), vo.getSeq(), vo.getTableIndex());
        } else {
            throw new RuntimeException(String.format("未知的语句类型 %s", vo.getType()));
        }
        vo.setSql(sql);
        try {
            jdbc.execute(sql, null, null);
            vo.setExecStatus(IndexSqlVO.EXEC_STATUS_SUCCESS);
        } catch (Exception e) {
            logger.error("", e);
            vo.setExecStatus(IndexSqlVO.EXEC_STATUS_FAIL);
            vo.setErrMsg(Exceptions.trace(e));
        }
        return vo;
    }

    @Override
    public List<String> createIndex(Jdbc jdbc, EtlTableMeta meta) {
        List<String> errors = new ArrayList<>();
        List<String> sqls = buildCreateIndexSqls(meta);
        sqls.stream().forEach(sql -> {
            try {
                jdbc.execute(sql, null, null);
            } catch (Exception e) {
                logger.error("", e);
                errors.add(String.format("索引创建语句%s，执行失败：%s", sql, Exceptions.trace(e)));
            }
        });
        return errors;
    }

    @Override
    public int drop(Jdbc jdbc, EtlTableMeta meta, String dbName) {
        return jdbc.execute("drop table  " + qualifyDDLDbName(dbName) + "" + meta.getName().toUpperCase(), null, null);
    }

    @Override
    public List<String> buildCreateSqls(EtlTableMeta meta, String dbName) {
        List<String> sqls = new ArrayList<>();
        sqls.add(createBasicTable(meta, dbName));
        String tc = tableComment(meta);
        if (StringUtils.isNotEmpty(tc))
            sqls.add(tc);
        meta.getCols().forEach(f -> {
            String e = columnComment(meta, f, dbName);
            if (StringUtils.isNotEmpty(e))
                sqls.add(e);

        });
        if (StringUtils.isNotEmpty(meta.getPkName())) {
            String pkSql = pk(meta);
            //双重校验 比如clickHouse的主键语句必须在createTableSQL的suffix中
            if (StringUtils.isNotEmpty(pkSql)) sqls.add(pkSql);
        }
//        List<EtlTableMeta.TableIndex> indexs = meta.getIndexs();
//        if (indexs != null) {
//            for (int i = 0; i < indexs.size(); i++) {
//                EtlTableMeta.TableIndex ti = indexs.get(i);
//                sqls.add(index(meta.getName(), i, ti));
//            }
//        }
        sqls.addAll(buildCreateIndexSqls(meta));
        return sqls;
    }

    @Override
    public List<String> buildCreateIndexSqls(EtlTableMeta meta) {
        List<String> sqls = new ArrayList<>();
        List<EtlTableMeta.TableIndex> indexs = meta.getIndexs();
        if (indexs != null) {
            for (int i = 0; i < indexs.size(); i++) {
                EtlTableMeta.TableIndex ti = indexs.get(i);
                String sql = index(meta.getName(), i, ti);
                if (StringUtils.isNotEmpty(sql)) sqls.add(sql);
            }
        }
        return sqls;
    }

    @Override
    public List<IndexSqlVO> buildDropAndCreateIndexSqls(EtlTableMeta meta) {
        List<IndexSqlVO> list = new ArrayList<>();
        List<EtlTableMeta.TableIndex> indexs = meta.getIndexs();
        if (indexs != null) {
            for (int i = 0; i < indexs.size(); i++) {
                EtlTableMeta.TableIndex ti = indexs.get(i);
                String dropSql = dropIndex(meta.getName(), i, ti);
                if (StringUtils.isNotEmpty(dropSql)) {
                    list.add(new IndexSqlVO(dropSql, IndexSqlVO.DROP_INDEX, meta.getName(), i, ti));
                }
                String sql = index(meta.getName(), i, ti);
                if (StringUtils.isNotEmpty(sql)) {
                    list.add(new IndexSqlVO(sql, IndexSqlVO.CREATE_INDEX, meta.getName(), i, ti));
                }
            }
        }
        return list;
    }

    /**
     * 基础表创建语句 sql
     *
     * @param meta
     * @return
     */
    protected String createBasicTable(EtlTableMeta meta, String dbName) {
        String format = "create table %s%s\n" +
                "(\n" +
                "%s" +
                ")\n %s";
        StringBuilder cols = new StringBuilder();
        meta.getCols().forEach(f -> {
            String str = column(f);
            if (cols.length() == 0) {
                cols.append(str);
            } else {
                cols.append(",\n").append(str);
            }
        });
        cols.append("\n");
        String suffix = createBasicTableSuffix(meta);
        return String.format(format, qualifyDDLDbName(dbName), meta.getName().toUpperCase(), cols.toString(), StringUtils.isEmpty(suffix) ? "" : suffix);
    }


    protected String getPkNameBySql(Jdbc jdbc, String sql, EasyParams params) {
        List<EasyRecord> pkList = jdbc.query(sql, params, null);
        if (pkList.size() == 1) return pkList.get(0).getString("pk");
        return null;
    }

    /**
     * 建表语句中的列定义
     *
     * @param f
     * @return
     */
    abstract String column(EtlFieldMeta f);

    abstract String createBasicTableSuffix(EtlTableMeta meta);

    /**
     * 表注释 sql
     *
     * @param meta
     * @return
     */
    abstract String tableComment(EtlTableMeta meta);

    /**
     * 列注释 sql
     *
     * @param tbMeta
     * @param meta
     * @param dbName
     * @return
     */
    abstract String columnComment(EtlTableMeta tbMeta, EtlFieldMeta meta, String dbName);

    /**
     * alter add 添加主键 sql
     *
     * @param meta
     * @return
     */
    abstract String pk(EtlTableMeta meta);


    /**
     * alter add index 添加索引 sql
     *
     * @param name
     * @param idx
     * @param ti
     * @return
     */
    abstract String index(String name, int idx, EtlTableMeta.TableIndex ti);

    /**
     * 删除索引  语句
     * @param name
     * @param idx
     * @param ti
     * @return
     */
    abstract String dropIndex(String name, int idx, EtlTableMeta.TableIndex ti);


}
