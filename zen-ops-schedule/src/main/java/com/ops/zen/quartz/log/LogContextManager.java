package com.ops.zen.quartz.log;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志上下文管理器，使用ThreadLocal存储每个线程的日志条目
 */
public class LogContextManager {
    private static final ThreadLocal<List<LogEntry>> contextHolder = ThreadLocal.withInitial(ArrayList::new);

    public static void addLogEntry(LogEntry entry) {
        contextHolder.get().add(entry);
    }

    public static List<LogEntry> getCurrentLogs() {
        return contextHolder.get();
    }

    public static String getLogsAsString() {
        StringBuilder sb = new StringBuilder();
        for (LogEntry entry : getCurrentLogs()) {
            sb.append(entry.toString()).append("\n");
        }
        return sb.toString();
    }

    public static void clear() {
        contextHolder.remove();
    }
}
