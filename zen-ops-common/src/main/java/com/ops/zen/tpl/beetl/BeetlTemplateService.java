package com.ops.zen.tpl.beetl;

import com.ops.zen.tpl.beetl.ex.ExceptionThrowUtils;
import com.ops.zen.tpl.beetl.ex.VerifyFailed;
import com.ops.zen.cache.CacheCleanPlan;
import com.ops.zen.cache.CacheManager;
import com.ops.zen.tpl.TemplateProviderType;
import com.ops.zen.tpl.TemplateService;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.ex.Exceptions;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public class BeetlTemplateService implements TemplateService, CacheCleanPlan {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(BeetlTemplateService.class);

    private GroupTemplate groupTemplate = null;

    private volatile static BeetlTemplateService _inst;

    public BeetlTemplateService() {
        init();//从inst方法中挪出来，解决并发访问时不完全初始化问题
        CacheManager.inst().pushCacheCleanPlan(this);
    }

    public GroupTemplate getGroupTemplate() {
        return groupTemplate;
    }

    // 注册clzz的静态方法
    public void registerFunc(String namespace, Class clzz) {
        groupTemplate.registerFunctionPackage(namespace, clzz);
    }

    public void registerFunc(String namespace, Object obj) {
        groupTemplate.registerFunctionPackage(namespace, obj);
    }

//    public static BeetlTemplateService inst() {
//        if (_inst == null) {
//            synchronized (BeetlTemplateService.class) {
//                if (_inst != null) {
//                    return _inst;
//                }
//                _inst = new BeetlTemplateService();
//            }
//        }
//        return _inst;
//    }

    /**
     * 只执行一次
     *
     * @throws IOException
     */
    private void init() {
        try {
            InputStream resourceAsStream = BeetlTemplateService.class.getResourceAsStream("beetl.properties");
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            //初始化代码
            StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
            Configuration cfg = new Configuration(properties);
            groupTemplate = new GroupTemplate(resourceLoader, cfg);

            registerFunc("str", StringUtils.class);
            registerFunc("ex", ExceptionThrowUtils.class);
            //TODO 配置可以通过代码直接写，例如
//            cfg.setStatementStart(statementStart);
//            cfg.setStatementEnd(statementEnd);
//            cfg.setHtmlTagSupport(false);
//            gt = new GroupTemplate(new StringTemplateResourceLoader(), cfg);
//            gt.registerFunctionPackage("Strings", Strings.class);
//            gt.registerFunctionPackage("Times", Times.class);
//            gt.setErrorHandler((beeExceptionos, writer) -> {
//                throw beeExceptionos;
//            });
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Exceptions.throwAsRuntimeException(e);
        }
    }


    @Override
    public TemplateProviderType type() {
        return TemplateProviderType.BEETL;
    }

    @Override
    public String process(String template, String paramName, Object param) {
//        groupTemplate.getProgramCache().clearAll();
        //获取模板
        Template t = groupTemplate.getTemplate(template);
        t.binding(paramName, param);
        //渲染结果
        String str = t.render();
        return str;
    }

    @Override
    public String process(String template, Map<String, Object> param) {
        try {
            if (groupTemplate == null) {
                logger.error("beetl groupTemplate为空");
            }
            //获取模板
            Template t = groupTemplate.getTemplate(template);
            //param可为空 不做判断
            t.binding(param);
            //渲染结果
            String str = t.render();
            return str;
        } catch (BeetlException e) {
            logger.error("", e);
            if (e.getCause() instanceof VerifyFailed) {
                throw (VerifyFailed) e.getCause();
            } else {
                Exceptions.throwAsRuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public String processEntity(String template, Object entity) {
        try {
            if (groupTemplate == null) {
                logger.error("beetl groupTemplate为空");
            }
            //获取模板
            Template t = groupTemplate.getTemplate(template);
            //param可为空 不做判断
            t.binding("root", entity);
            //渲染结果
            String str = t.render();
            return str;
        } catch (BeetlException e) {
            logger.error("", e);
            if (e.getCause() instanceof VerifyFailed) {
                throw (VerifyFailed) e.getCause();
            } else {
                Exceptions.throwAsRuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public String getCacheID() {
        return "SQL_LOADER_TEMPLAT_GROUP";
    }

    @Override
    public void invalidateAll() {
        groupTemplate.getProgramCache().clearAll();
    }
}
