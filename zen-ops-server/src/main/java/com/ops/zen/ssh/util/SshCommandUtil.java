package com.ops.zen.ssh.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.ops.zen.en.SshLoginTypeEn;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.ssh.vo.ServerInfoVo;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSH命令执行工具类
 * 提供跨平台（Linux/Windows）的系统信息采集能力
 */
@Slf4j
public class SshCommandUtil {

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int EXEC_TIMEOUT = 5000;

    // Session 缓存：key=serverId, value=SessionWrapper
    private static final Map<Long, SessionWrapper> SESSION_CACHE = new ConcurrentHashMap<>();

    // 定时清理任务
    private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor();

    // 缓存过期时间：1分钟
    private static final long CACHE_EXPIRE_MS = 60 * 1000;

    static {
        // 启动定时清理任务，每30秒检查一次
        CLEANER.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            SESSION_CACHE.entrySet().removeIf(entry -> {
                SessionWrapper wrapper = entry.getValue();
                if (now - wrapper.lastAccessTime > CACHE_EXPIRE_MS) {
                    closeSessionQuietly(wrapper.session);
                    log.info("关闭过期SSH Session: serverId={}", entry.getKey());
                    return true;
                }
                return false;
            });
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Session 包装类，记录最后访问时间
     */
    private static class SessionWrapper {
        Session session;
        long lastAccessTime;

        SessionWrapper(Session session) {
            this.session = session;
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    /**
     * 测试SSH连接
     */
    public static Map<String, Object> testConnection(ZenSsh svr) {
        Map<String, Object> result = new LinkedHashMap<>();
        Session session = null;
        try {
            session = createSession(svr, 5000);
            session.connect(5000);
            result.put("success", true);
            result.put("message", "连接成功");
        } catch (Exception e) {
            log.error("SSH测试连接失败", e);
            result.put("success", false);
            result.put("message", "连接失败: " + e.getMessage());
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return result;
    }

    /**
     * 获取服务器系统信息
     */
    public static ServerInfoVo getServerInfo(ZenSsh svr) {
        ServerInfoVo info = new ServerInfoVo();
        info.setServerName(svr.getName());
        info.setIp(svr.getIp());
        info.setPort(svr.getPortSsh());
        info.setUser(svr.getUserName());

        Session session = null;
        try {
            // 尝试从缓存获取 Session
            session = getCachedSession(svr);
            if (session == null) {
                session = createSession(svr, CONNECT_TIMEOUT);
                session.connect(CONNECT_TIMEOUT);
                // 缓存 Session
                cacheSession(svr, session);
            }

            // 检测操作系统类型
            String osType = detectOS(session);
            info.setOsType(osType);

            if ("windows".equalsIgnoreCase(osType)) {
                getWindowsInfo(session, info);
            } else {
                getLinuxInfo(session, info);
            }

            // 获取资源使用情况
            ServerInfoVo.UsageVo usage = getUsage(session, osType);
            info.setCpuUsage(usage.getCpuUsage());
            info.setMemTotal(usage.getMemTotal());
            info.setMemUsed(usage.getMemUsed());
            info.setMemUsage(usage.getMemUsage());
            info.setDiskTotal(usage.getDiskTotal());
            info.setDiskUsed(usage.getDiskUsed());
            info.setDiskUsage(usage.getDiskUsage());
        } catch (Exception e) {
            log.error("获取服务器信息失败", e);
            setDefaultUsage(info);
        }
        return info;
    }

    /**
     * 获取服务器资源使用情况（CPU、内存、磁盘占用率）
     */
    public static ServerInfoVo.UsageVo getServerUsage(ZenSsh svr) {
        Session session = null;
        try {
            // 尝试从缓存获取 Session
            session = getCachedSession(svr);
            if (session == null) {
                session = createSession(svr, CONNECT_TIMEOUT);
                session.connect(CONNECT_TIMEOUT);
                // 缓存 Session
                cacheSession(svr, session);
            }

            // 检测操作系统类型
            String osType = detectOS(session);
            return getUsage(session, osType);
        } catch (Exception e) {
            log.error("获取服务器资源使用情况失败", e);
            ServerInfoVo.UsageVo usage = new ServerInfoVo.UsageVo();
            usage.setCpuUsage("未知");
            usage.setMemUsage("未知");
            usage.setDiskUsage("未知");
            return usage;
        }
    }

    /**
     * 获取资源使用情况（内部方法）
     */
    private static ServerInfoVo.UsageVo getUsage(Session session, String osType) {
        ServerInfoVo.UsageVo usage = new ServerInfoVo.UsageVo();
        try {
            if ("windows".equalsIgnoreCase(osType)) {
                getWindowsUsage(session, usage);
            } else {
                getLinuxUsage(session, usage);
            }
        } catch (Exception e) {
            log.error("获取资源使用情况失败", e);
            usage.setCpuUsage("未知");
            usage.setMemUsage("未知");
            usage.setDiskUsage("未知");
        }
        return usage;
    }

    /**
     * 创建SSH会话
     */
    private static Session createSession(ZenSsh svr, int timeout) throws Exception {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        if (SshLoginTypeEn.PRIVATE_KEY.equals(svr.getLoginTp()) && svr.getPrvKey() != null) {
            jsch.addIdentity("ssh", svr.getPrvKey().getBytes(), null,
                    svr.getPrvKeyPasswd() != null ? svr.getPrvKeyPasswd().getBytes() : null);
        }
        Session session = jsch.getSession(svr.getUserName(), svr.getIp(), Integer.parseInt(svr.getPortSsh()));
        if (SshLoginTypeEn.PASSWORD.equals(svr.getLoginTp())) {
            session.setPassword(svr.getUserPwd());
        }
        session.setConfig(config);
        session.setTimeout(timeout);
        return session;
    }

    /**
     * 获取缓存的 Session
     */
    private static Session getCachedSession(ZenSsh svr) {
        Long serverId = svr.getPkServer();
        SessionWrapper wrapper = SESSION_CACHE.get(serverId);

        if (wrapper != null && wrapper.session != null && wrapper.session.isConnected()) {
            // 更新最后访问时间
            wrapper.lastAccessTime = System.currentTimeMillis();
            log.debug("使用缓存的SSH Session: serverId={}", serverId);
            return wrapper.session;
        }

        // 缓存失效，移除
        if (wrapper != null) {
            SESSION_CACHE.remove(serverId);
        }
        return null;
    }

    /**
     * 缓存 Session
     */
    private static void cacheSession(ZenSsh svr, Session session) {
        Long serverId = svr.getPkServer();
        SESSION_CACHE.put(serverId, new SessionWrapper(session));
        log.debug("缓存SSH Session: serverId={}", serverId);
    }

    /**
     * 安静地关闭 Session
     */
    private static void closeSessionQuietly(Session session) {
        try {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception e) {
            log.warn("关闭SSH Session失败", e);
        }
    }

    /**
     * 检测操作系统类型
     */
    private static String detectOS(Session session) {
        try {
            String output = executeCommand(session, "uname -s 2>/dev/null || ver 2>nul || echo unknown");
            if (output.toLowerCase().contains("linux")) {
                return "linux";
            } else if (output.toLowerCase().contains("windows") || output.toLowerCase().contains("microsoft")
                    || output.toLowerCase().contains("NT") || output.toLowerCase().contains("cmd")) {
                return "windows";
            } else if (!output.contains("unknown") && !output.isEmpty()) {
                return "linux";
            }
        } catch (Exception e) {
            log.warn("检测操作系统失败", e);
        }
        return "linux";
    }

    /**
     * 获取Linux系统信息
     */
    private static void getLinuxInfo(Session session, ServerInfoVo info) {
        info.setHostname(executeCommand(session, "hostname"));

        String osVersion = executeCommand(session, "cat /etc/os-release 2>/dev/null | grep 'PRETTY_NAME' | cut -d'\"' -f2");
        if (osVersion.isEmpty()) {
            osVersion = executeCommand(session, "cat /etc/redhat-release 2>/dev/null || cat /etc/issue 2>/dev/null | head -1");
        }
        info.setOsVersion(osVersion.isEmpty() ? "未知" : osVersion);

        info.setKernel(executeCommand(session, "uname -r"));

        String uptime = executeCommand(session, "uptime -p 2>/dev/null || cat /proc/uptime | awk '{print $1}'");
        info.setUptime(uptime.isEmpty() ? "未知" : uptime);

        // CPU信息
        String cpuModel = executeCommand(session, "cat /proc/cpuinfo | grep 'model name' | head -1 | cut -d':' -f2 | sed 's/^ *//'");
        if (cpuModel.isEmpty()) {
            cpuModel = executeCommand(session, "cat /proc/cpuinfo | grep 'Processor' | head -1 | cut -d':' -f2 | sed 's/^ *//'");
        }
        info.setCpuModel(cpuModel.isEmpty() ? "未知" : cpuModel);

        String cpuCores = executeCommand(session, "nproc 2>/dev/null || grep -c ^processor /proc/cpuinfo 2>/dev/null");
        if (!cpuCores.isEmpty()) {
            try {
                info.setCpuCores(Integer.parseInt(cpuCores.trim()));
            } catch (NumberFormatException e) {
                info.setCpuCores(null);
            }
        }
    }

    /**
     * 获取Windows系统信息
     */
    private static void getWindowsInfo(Session session, ServerInfoVo info) {
        info.setHostname(executeCommand(session, "hostname"));

        String osVersion = executeCommand(session, "wmic os get Caption 2>nul | findstr /v Caption");
        info.setOsVersion(osVersion.isEmpty() ? "未知" : osVersion.trim());

        String kernel = executeCommand(session, "wmic os get Version 2>nul | findstr /v Version");
        info.setKernel(kernel.isEmpty() ? "未知" : kernel.trim());

        String uptime = executeCommand(session, "net stats srv 2>nul | findstr \"Statistics since\" || wmic os get LastBootUpTime 2>nul | findstr /v LastBootUpTime");
        info.setUptime(uptime.isEmpty() ? "未知" : uptime.trim());

        String cpuModel = executeCommand(session, "wmic cpu get Name 2>nul | findstr /v Name");
        info.setCpuModel(cpuModel.isEmpty() ? "未知" : cpuModel.trim());

        String cpuCores = executeCommand(session, "wmic cpu get NumberOfCores 2>nul | findstr /v NumberOfCores");
        if (!cpuCores.isEmpty()) {
            try {
                info.setCpuCores(Integer.parseInt(cpuCores.trim()));
            } catch (NumberFormatException e) {
                info.setCpuCores(null);
            }
        }

        String cpuUsage = executeCommand(session, "wmic cpu get LoadPercentage 2>nul | findstr /v LoadPercentage");
        info.setCpuUsage(parseUsage(cpuUsage));

        String memTotal = executeCommand(session, "wmic computersystem get TotalPhysicalMemory 2>nul | findstr /v TotalPhysicalMemory");
        info.setMemTotal(formatBytes(parseLong(memTotal)));

        String memFree = executeCommand(session, "wmic os get FreePhysicalMemory 2>nul | findstr /v FreePhysicalMemory");
        long free = parseLong(memFree) * 1024;
        long total = parseLong(memTotal);
        info.setMemUsed(formatBytes(total - free));
        String memUsage = total > 0 ? String.format("%.1f", (double) (total - free) / total * 100) : "0";
        info.setMemUsage(parseUsage(memUsage));

        String diskTotal = executeCommand(session, "wmic logicaldisk where \"DeviceID='C:'\" get Size 2>nul | findstr /v Size");
        String diskFree = executeCommand(session, "wmic logicaldisk where \"DeviceID='C:'\" get FreeSpace 2>nul | findstr /v FreeSpace");
        long dTotal = parseLong(diskTotal);
        long dFree = parseLong(diskFree);
        info.setDiskTotal(formatBytes(dTotal));
        info.setDiskUsed(formatBytes(dTotal - dFree));
        String diskUsage = dTotal > 0 ? String.format("%.1f", (double) (dTotal - dFree) / dTotal * 100) : "0";
        info.setDiskUsage(parseUsage(diskUsage));
    }

    /**
     * 执行SSH命令
     */
    private static String executeCommand(Session session, String command) {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            channel.connect(EXEC_TIMEOUT);

            StringBuilder output = new StringBuilder();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) break;
                Thread.sleep(50);
            }
            channel.disconnect();

            String result = output.toString().trim().replaceAll("\\r\\n|\\r|\\n", " ");
            return result.replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            log.warn("执行命令失败: {}", command, e);
            return "";
        }
    }

    /**
     * 解析使用率字符串
     */
    private static String parseUsage(String usage) {
        if (usage == null || usage.isEmpty()) return "未知";
        usage = usage.trim().replace("%", "");
        try {
            double val = Double.parseDouble(usage);
            return String.format("%.1f%%", val);
        } catch (NumberFormatException e) {
            return usage.isEmpty() ? "未知" : usage;
        }
    }

    /**
     * 解析长整数
     */
    private static long parseLong(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        try {
            return Long.parseLong(str.trim().replace(",", "").replace(" ", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 格式化字节数
     */
    public static String formatBytes(long bytes) {
        if (bytes <= 0) return "未知";
        if (bytes < 1024) return bytes + " B";
        int unit = 0;
        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return String.format("%.1f %s", value, units[unit]);
    }

    /**
     * 设置错误结果
     */
    private static void setErrorResult(Map<String, Object> result, String msg) {
        result.put("hostname", "获取失败: " + msg);
        result.put("osVersion", "获取失败");
        result.put("kernel", "获取失败");
        result.put("uptime", "获取失败");
        result.put("cpuModel", "获取失败");
        result.put("cpuCores", "获取失败");
        result.put("cpuUsage", "获取失败");
        result.put("memTotal", "获取失败");
        result.put("memUsed", "获取失败");
        result.put("memUsage", "获取失败");
        result.put("diskTotal", "获取失败");
        result.put("diskUsed", "获取失败");
        result.put("diskUsage", "获取失败");
    }

    /**
     * 获取Linux资源使用情况
     */
    private static void getLinuxUsage(Session session, ServerInfoVo.UsageVo usage) {
        // CPU 使用率
        String cpuIdle = executeCommand(session, "top -bn1 | grep 'Cpu(s)' | awk '{print $8}'");
        if (cpuIdle.isEmpty()) {
            cpuIdle = executeCommand(session, "top -bn1 | grep '%Cpu' | awk '{print $8}'");
        }
        double cpuUsage = 0;
        try {
            cpuUsage = 100.0 - Double.parseDouble(cpuIdle.trim());
        } catch (Exception e) {
            cpuUsage = 0;
        }
        usage.setCpuUsage(String.format("%.1f%%", Math.max(0, cpuUsage)));

        // 内存使用率
        String memInfo = executeCommand(session, "free | grep Mem");
        String[] memParts = memInfo.split("\\s+");
        if (memParts.length >= 3) {
            long memTotal = parseLong(memParts[1]);
            long memUsed = parseLong(memParts[2]);
            usage.setMemTotal(formatBytes(memTotal * 1024));
            usage.setMemUsed(formatBytes(memUsed * 1024));
            usage.setMemUsage(String.format("%.1f%%", memTotal > 0 ? (memUsed * 100.0 / memTotal) : 0));
        } else {
            usage.setMemUsage("未知");
        }

        // 磁盘使用率
        String diskInfo = executeCommand(session, "df -B1 / | tail -1");
        String[] diskParts = diskInfo.split("\\s+");
        if (diskParts.length >= 3) {
            long diskTotal = parseLong(diskParts[1]);
            long diskUsed = parseLong(diskParts[2]);
            usage.setDiskTotal(formatBytes(diskTotal));
            usage.setDiskUsed(formatBytes(diskUsed));
            String diskUsagePercent = executeCommand(session, "df / | tail -1 | awk '{print $5}'");
            usage.setDiskUsage(parseUsage(diskUsagePercent));
        } else {
            usage.setDiskUsage("未知");
        }
    }

    /**
     * 获取Windows资源使用情况
     */
    private static void getWindowsUsage(Session session, ServerInfoVo.UsageVo usage) {
        // CPU 使用率
        String cpuUsage = executeCommand(session, "wmic cpu get loadpercentage /value | findstr LoadPercentage");
        String cpuValue = cpuUsage.replaceAll(".*=", "").trim();
        usage.setCpuUsage(parseUsage(cpuValue));

        // 内存使用率
        String memTotalStr = executeCommand(session, "wmic OS get TotalVisibleMemorySize /value | findstr TotalVisibleMemorySize");
        String memFreeStr = executeCommand(session, "wmic OS get FreePhysicalMemory /value | findstr FreePhysicalMemory");
        long memTotal = parseLong(memTotalStr.replaceAll(".*=", "").trim());
        long memFree = parseLong(memFreeStr.replaceAll(".*=", "").trim());
        long memUsed = memTotal - memFree;
        usage.setMemTotal(formatBytes(memTotal * 1024));
        usage.setMemUsed(formatBytes(memUsed * 1024));
        usage.setMemUsage(String.format("%.1f%%", memTotal > 0 ? (memUsed * 100.0 / memTotal) : 0));

        // 磁盘使用率
        String diskSizeStr = executeCommand(session, "wmic logicaldisk where DeviceID='C:' get Size /value | findstr Size");
        String diskFreeStr = executeCommand(session, "wmic logicaldisk where DeviceID='C:' get FreeSpace /value | findstr FreeSpace");
        long size = parseLong(diskSizeStr.replaceAll(".*=", "").trim());
        long free = parseLong(diskFreeStr.replaceAll(".*=", "").trim());
        long used = size - free;
        usage.setDiskTotal(formatBytes(size));
        usage.setDiskUsed(formatBytes(used));
        usage.setDiskUsage(String.format("%.1f%%", size > 0 ? (used * 100.0 / size) : 0));
    }

    /**
     * 设置默认资源使用情况
     */
    private static void setDefaultUsage(ServerInfoVo info) {
        info.setCpuUsage("获取失败");
        info.setMemTotal("获取失败");
        info.setMemUsed("获取失败");
        info.setMemUsage("获取失败");
        info.setDiskTotal("获取失败");
        info.setDiskUsed("获取失败");
        info.setDiskUsage("获取失败");
    }
}
