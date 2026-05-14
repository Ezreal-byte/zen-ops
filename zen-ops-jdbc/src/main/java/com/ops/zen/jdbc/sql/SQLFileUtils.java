package com.ops.zen.jdbc.sql;

/**
 * <pre>
 * META-INF/aaa/bbb/EmployeeDao/selectById.sql
 * META-INF/aaa/bbb/EmployeeDao/selectById-postgres.sql
 *
 * DB2	org.seasar.doma.jdbc.dialect.Db2Dialect	db2
 * H2 Database Engine 1.2.126	org.seasar.doma.jdbc.dialect.H212126Dialect	h2
 * H2 Database Engine	org.seasar.doma.jdbc.dialect.H2Dialect	h2
 * HSQLDB	org.seasar.doma.jdbc.dialect.HsqldbDialect	hsqldb
 * Microsoft SQL Server 2008	org.seasar.doma.jdbc.dialect.Mssql2008Dialect	mssql2008
 * Microsoft SQL Server	org.seasar.doma.jdbc.dialect.MssqlDialect	mssql
 * MySQL	org.seasar.doma.jdbc.dialect.MySqlDialect	mysql
 * Oracle Database	org.seasar.doma.jdbc.dialect.OracleDialect	oracle
 * PostgreSQL	org.seasar.doma.jdbc.dialect.PostgresDialect	postgres
 * SQLite	org.seasar.doma.jdbc.dialect.SqliteDialect	sqlite
 * </pre>
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class SQLFileUtils {

    /**
     * 在工程或jar包的根目录开始的sql文件路径
     *
     * @param loader
     * @param fileNameWithoutSuffix
     * @return
     */
    public static String filePathInMetaInf(Class<?> loader, String fileNameWithoutSuffix) {
        String filePath = String.format("/META-INF/%s/%s.sql", loader.getName().replaceAll("\\.", "/"), fileNameWithoutSuffix);
        return filePath;
    }
}
