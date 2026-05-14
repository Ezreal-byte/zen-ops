package com.ops.zen.ssh.vo;

import lombok.Data;

/**
 * 服务器信息VO
 */
@Data
public class ServerInfoVo {
    // 基本信息
    private String serverName;
    private String ip;
    private String port;
    private String user;
    private String hostname;
    private String osType;
    private String osVersion;
    private String kernel;
    private String uptime;
    private String cpuModel;
    private Integer cpuCores;

    // 资源使用情况
    private String cpuUsage;
    private String memTotal;
    private String memUsed;
    private String memUsage;
    private String diskTotal;
    private String diskUsed;
    private String diskUsage;

    /**
     * 资源使用情况内部类
     */
    @Data
    public static class UsageVo {
        private String cpuUsage;
        private String memTotal;
        private String memUsed;
        private String memUsage;
        private String diskTotal;
        private String diskUsed;
        private String diskUsage;
    }
}
