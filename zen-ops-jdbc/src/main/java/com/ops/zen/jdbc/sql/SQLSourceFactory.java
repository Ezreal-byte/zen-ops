package com.ops.zen.jdbc.sql;

import com.ops.zen.cache.BaseMemCache;
import com.ops.zen.cache.Nullable;
import com.ops.zen.cache.Pair;
import com.ops.zen.jdbc.dialect.DialectEn;
import com.ops.zen.jdbc.dialect.SoarDbHelper;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.Reflects;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * 优先获取非方言文件
 * 其次获取方言的文件
 * 优先获取非方言sqlId
 * 其次获取方言sqlId
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class SQLSourceFactory extends BaseMemCache<Pair<Class<?>, String>, Nullable<Map<String, SQLSource>>> {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(SQLSourceFactory.class);

    private volatile static SQLSourceFactory _inst;

    private SQLSourceFactory() {
    }

    public static SQLSourceFactory inst() {
        if (_inst == null) {
            synchronized (SQLSourceFactory.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new SQLSourceFactory();
            }
        }
        return _inst;
    }

    /**
     * @param loader
     * @param fileName
     * @param sqlId
     * @return
     * @throws IOException
     */
    public SQLSource getSqlSource(Class<?> loader, String fileName, String sqlId, DialectEn dialect) throws IOException {
        /*
        优先获取normal sql文件
        其次获取-dbTp 的sql文件
         */
        Map<String, SQLSource> value = get(new Pair<>(loader, fileName)).value();
        String dbtp = SoarDbHelper.parseDialect(dialect);
        if (value == null) {
            String fileNameDialect = SoarDbHelper.parseSqlFileName(fileName, dbtp);
            value = get(new Pair<>(loader, fileNameDialect)).value();
            Assert.notNull(value, String.format("%s类作为资源加载器，无法加载到SQLSource【sql文件：%s or %s，sqlId：%s】",
                    loader.getName(), fileName, fileNameDialect, sqlId));
        }

        // 优先获取normal sqlId
        // 其次取带有-dbTp的sqlId
        SQLSource source = value.get(sqlId);
        String sqlIdDbtp = sqlId.concat("-").concat(dbtp);
        if (source == null) {
            source = value.get(sqlIdDbtp);
        }
        Assert.notNull(source, String.format("未找到sqlId【%s or %s】", sqlId, sqlIdDbtp));
        return source;
    }

    @Override
    public String getCacheID() {
        return "SQLSourceFactory";
    }

    @Override
    protected Nullable<Map<String, SQLSource>> load(Pair<Class<?>, String> key) throws Exception {
        Class<?> loader = key.getKey();
        String fileName = key.getValue();
        String filePath = SQLFileUtils.filePathInMetaInf(loader, fileName);
        String fileTxt = Reflects.asString(loader, filePath);
        if (fileTxt == null) {
            return new Nullable<>(null);
        }
        Map<String, SQLSource> sqlSources = new SQLFileParser(fileName, fileTxt).parse();
        return new Nullable<>(sqlSources);
    }
}
