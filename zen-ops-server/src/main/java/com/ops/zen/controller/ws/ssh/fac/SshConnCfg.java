package com.ops.zen.controller.ws.ssh.fac;

/**
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public interface SshConnCfg {

    /**
     * com.uis.nx.soar.common.ssh.fac.SshConnCfgTpEn
     *
     * @return [PL_PWD, PL_PRV_KEY, ID]
     */
    String getType();

    /**
     * com.uis.nx.soar.common.ssh.fac.SshChannelTpEn
     *
     * @return [SHELL, SFTP]
     */
    String getChannelType();

    /**
     * 初始路径
     *
     * @return
     */
    String getInitPath();


    /**
     * 当type为ID时标记ID的类型
     * @return
     */
    String getIdType();

    String getInitCmd();

    /**
     * 终端类型 参考{@link com.uis.nx.soar.common.ssh.jsch.TermTypeEn}
     * @return
     */
    String getTermTp();


    Long getServerId();

}
