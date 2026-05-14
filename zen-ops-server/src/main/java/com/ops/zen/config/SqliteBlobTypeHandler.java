package com.ops.zen.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;

/**
 * SQLite 兼容的 BLOB 类型处理器
 * 解决 SQLite JDBC 驱动不支持 ResultSet.getObject() 获取 byte[] 的问题
 */
@MappedTypes(byte[].class)
public class SqliteBlobTypeHandler extends BaseTypeHandler<byte[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, JdbcType jdbcType) throws SQLException {
        // 使用 setBytes 或 setBinaryStream 设置 BLOB 数据
        if (parameter.length > 0) {
            ps.setBytes(i, parameter);
        } else {
            ps.setNull(i, Types.BLOB);
        }
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 使用 getBytes 代替 getObject
        return rs.getBytes(columnName);
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 使用 getBytes 代替 getObject
        return rs.getBytes(columnIndex);
    }

    @Override
    public byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 使用 getBytes 代替 getObject
        return cs.getBytes(columnIndex);
    }
}
