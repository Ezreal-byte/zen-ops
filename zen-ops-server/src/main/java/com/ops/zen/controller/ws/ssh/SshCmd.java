package com.ops.zen.controller.ws.ssh;

/**
 * 前端socket客户端发送给后台的命令
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class SshCmd {

    /**
     * 操作类型
     */
    private String op;

    /**
     * 操作内容
     */
    private String content;

    public SshCmd() {
    }

    public SshCmd(String op, String content) {
        this.op = op;
        this.content = content;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static class CmdResize {
        private Integer cols;
        private Integer rows;

        public Integer getCols() {
            return cols;
        }

        public void setCols(Integer cols) {
            this.cols = cols;
        }

        public Integer getRows() {
            return rows;
        }

        public void setRows(Integer rows) {
            this.rows = rows;
        }
    }
}
