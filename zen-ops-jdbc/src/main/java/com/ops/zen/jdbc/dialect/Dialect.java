package com.ops.zen.jdbc.dialect;

import com.ops.zen.utils.ex.Exceptions;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class Dialect {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(Dialect.class);

    private volatile static Dialect _inst;

    private Dialect() {
    }

    public static Dialect inst() {
        if (_inst == null) {
            synchronized (Dialect.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new Dialect();
            }
        }
        return _inst;
    }

    private Map<Pattern, DialectEn> dialects = new HashMap<>();

    //TODO 支持的方言在这里添加
    {
        dialects.put(Pattern.compile("oracle.*"), DialectEn.ORACLE);
        dialects.put(Pattern.compile("clickhouse.*"), DialectEn.CLICKHOUSE);
        dialects.put(Pattern.compile("mysql.*"), DialectEn.MYSQL);
        dialects.put(Pattern.compile("microsoft sql server.*"), DialectEn.MSSQL);
        dialects.put(Pattern.compile("postgresql.*"), DialectEn.POSTGRE);
    }

    public DialectEn dialect(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String productName = meta.getDatabaseProductName().toLowerCase();
            String version = meta.getDatabaseProductVersion().toLowerCase();
            for (Map.Entry<Pattern, DialectEn> entry : dialects.entrySet()) {
                if (entry.getKey().matcher(productName).find()) {
                    return entry.getValue();
                }
            }
        } catch (Exception e) {
            throw Exceptions.wrapAsRt(e);
        }
        throw new RuntimeException(String.format("找不到数据源%s对应的方言", ds));
    }
}
