package com.ops.zen.quartz.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 自定义日志记录器，将日志同时记录到SLF4J和上下文管理器
 */
public class ContextAwareLogger {
    private final Logger slf4jLogger;

    public ContextAwareLogger(Class<?> clazz) {
        this.slf4jLogger = LoggerFactory.getLogger(clazz);
    }

    public void info(String message) {
        slf4jLogger.info(message);
        LogContextManager.addLogEntry(new LogEntry(LogLevel.INFO, message, null));
    }

    public void info(String message, Object... args) {
        slf4jLogger.info(message, args);
        LogContextManager.addLogEntry(new LogEntry(LogLevel.INFO, formatMessage(message, args), null));
    }

    public void warn(String message) {
        slf4jLogger.warn(message);
        LogContextManager.addLogEntry(new LogEntry(LogLevel.WARN, message, null));
    }

    public void warn(String message, Throwable t) {
        slf4jLogger.warn(message, t);
        LogContextManager.addLogEntry(new LogEntry(LogLevel.WARN, message, t));
    }

    public void error(String message) {
        slf4jLogger.error(message);
        LogContextManager.addLogEntry(new LogEntry(LogLevel.ERROR, message, null));
    }

    public void error(String message, Throwable t) {
        slf4jLogger.error(message, t);
        LogContextManager.addLogEntry(new LogEntry(LogLevel.ERROR, message, t));
    }

    public void error(String message, Object... args) {
        slf4jLogger.error(message, args);
        Throwable t = findThrowable(args);
        LogContextManager.addLogEntry(new LogEntry(
                LogLevel.ERROR,
                formatMessage(message, removeThrowable(args)),
                t
        ));
    }

    private Throwable findThrowable(Object[] args) {
        if (args == null || args.length == 0) return null;
        Object lastArg = args[args.length - 1];
        if (lastArg instanceof Throwable) {
            return (Throwable) lastArg;
        }
        return null;
    }

    private Object[] removeThrowable(Object[] args) {
        if (args == null || args.length == 0) return args;
        Object lastArg = args[args.length - 1];
        if (lastArg instanceof Throwable) {
            return Arrays.copyOf(args, args.length - 1);
        }
        return args;
    }

    private String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) return message;
        // 简化的消息格式化，实际使用中可能需要更复杂的实现
        return String.format(message.replace("{}", "%s"), args);
    }
}
