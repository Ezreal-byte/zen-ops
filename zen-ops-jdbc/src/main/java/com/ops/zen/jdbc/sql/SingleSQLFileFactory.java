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

/**
 * 单文件sql的缓存
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class SingleSQLFileFactory extends BaseMemCache<Pair<Class<?>, String>, Nullable<String>> {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(SingleSQLFileFactory.class);

    private volatile static SingleSQLFileFactory _inst;

    private SingleSQLFileFactory() {
    }

    public static SingleSQLFileFactory inst() {
        if (_inst == null) {
            synchronized (SingleSQLFileFactory.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new SingleSQLFileFactory();
            }
        }
        return _inst;
    }

    public String getSqlFileContent(Class<?> loader, String fileName, DialectEn dialect) throws IOException {
        String fileNameDialect = SoarDbHelper.parseSqlFileName(fileName, dialect);
        String value = get(new Pair<>(loader, fileNameDialect)).value();
        if (value != null) {
            return value;
        }
        value = get(new Pair<>(loader, fileName)).value();
        Assert.notNull(value, String.format("%s类作为资源加载器，无法加载到【sql文件：%s or %s】",
                loader.getName(), fileName, fileNameDialect));
        return value;
    }

    @Override
    public String getCacheID() {
        return "SingleSQLFileFactory";
    }

    @Override
    protected Nullable<String> load(Pair<Class<?>, String> key) throws Exception {
        Class<?> loader = key.getKey();
        String fileName = key.getValue();
        String filePath = SQLFileUtils.filePathInMetaInf(loader, fileName);
        String fileTxt = Reflects.asString(loader, filePath);
        return new Nullable<>(fileTxt);
    }
}
