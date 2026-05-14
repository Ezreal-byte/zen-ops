package com.ops.zen.phy.vo;


/**
 * @author Ezreal
 * @date 2022/5/20 17:21
 * @description 创建索引SQL VO
 **/
public class IndexSqlVO {

    public static final String DROP_INDEX = "drop";
    public static final String CREATE_INDEX = "create";
    public static final Byte EXEC_STATUS_NOT_EXEC = 0;
    public static final Byte EXEC_STATUS_FAIL = 1;
    public static final Byte EXEC_STATUS_SUCCESS = 2;

    private String sql;

    //DROP  CREATE
    private String type;

    private String name;

    private Integer seq;

    private EtlTableMeta.TableIndex tableIndex;

    private Byte execStatus = EXEC_STATUS_NOT_EXEC;//执行状态

    private String errMsg;//错误信息

    public IndexSqlVO() {
    }

    public IndexSqlVO(String sql, String type, String name, Integer seq, EtlTableMeta.TableIndex tableIndex) {
        this.sql = sql;
        this.type = type;
        this.name = name;
        this.seq = seq;
        this.tableIndex = tableIndex;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public EtlTableMeta.TableIndex getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(EtlTableMeta.TableIndex tableIndex) {
        this.tableIndex = tableIndex;
    }

    public Byte getExecStatus() {
        return execStatus;
    }

    public void setExecStatus(Byte execStatus) {
        this.execStatus = execStatus;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
