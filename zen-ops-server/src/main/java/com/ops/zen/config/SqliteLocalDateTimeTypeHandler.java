package com.ops.zen.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SQLite 兼容的 LocalDateTime 类型处理器
 * 解决 SQLite JDBC 驱动不支持 ResultSet.getObject() 获取 LocalDateTime 的问题
 */
@MappedTypes(LocalDateTime.class)
public class SqliteLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.format(FORMATTER));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseDateTime(value);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseDateTime(value);
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseDateTime(value);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // 支持多种日期时间格式
            if (value.contains("T")) {
                return LocalDateTime.parse(value);
            } else if (value.length() == 19) {
                return LocalDateTime.parse(value, FORMATTER);
            } else if (value.length() == 10) {
                return LocalDateTime.parse(value + " 00:00:00", FORMATTER);
            } else {
                return LocalDateTime.parse(value, FORMATTER);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse datetime: " + value, e);
        }
    }
}
