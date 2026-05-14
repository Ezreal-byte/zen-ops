package com.ops.zen.quartz.log;

import com.ops.zen.utils.ex.Exceptions;

import java.time.LocalDateTime;

/**
 * 日志条目类，用于存储单条日志信息
 */
class LogEntry {
    private final LogLevel level;
    private final String message;
    private final Throwable throwable;
    private final LocalDateTime timestamp;

    public LogEntry(LogLevel level, String message, Throwable throwable) {
        this.level = level;
        this.message = message;
        this.throwable = throwable;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public LogLevel getLevel() { return level; }
    public String getMessage() { return message; }
    public Throwable getThrowable() { return throwable; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append(" [").append(level).append("] ").append(message);
        if (throwable != null) {
            sb.append("\n").append(Exceptions.trace(throwable));
        }
        return sb.toString();
    }
}
