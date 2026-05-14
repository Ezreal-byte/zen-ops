package com.ops.zen.sql.vo;

/**
 * @author Ezreal
 * @date 2023/8/18 9:04
 * @description
 **/
public class FFSqlDMLResult {

    /**
     * 受影响的行数
     */
    private int updateRows = 0;

    /**
     * 事务ID
     */
    private String connectionId;

    public void plusUpdateRows(int i){
        this.updateRows = this.updateRows + i;
    }

    public int getUpdateRows() {
        return updateRows;
    }

    public void setUpdateRows(int updateRows) {
        this.updateRows = updateRows;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
