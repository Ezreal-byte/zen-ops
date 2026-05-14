package com.ops.zen.tpl.freemarker;

import com.ops.zen.cache.CacheCleanPlan;
import com.ops.zen.tpl.TemplateProviderType;
import com.ops.zen.tpl.TemplateService;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.Version;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author xyn
 * @Date 2025/4/1 17:08
 * @Description
 */
public class FreeMarkerTemplateService implements TemplateService, CacheCleanPlan {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(FreeMarkerTemplateService.class);

    private Configuration configuration;

    /**
     * 缓存template
     */
    private ConcurrentHashMap<String, Template> templates = new ConcurrentHashMap<>();

    private volatile static FreeMarkerTemplateService _inst;

    public FreeMarkerTemplateService() {
        init();
    }

//    public static FreeMarkerTemplateService inst() {
//        if (_inst == null) {
//            synchronized (FreeMarkerTemplateService.class) {
//                if (_inst != null) {
//                    return _inst;
//                }
//                _inst = new FreeMarkerTemplateService();
//            }
//        }
//        return _inst;
//    }

    /**
     * @throws IOException
     */
    private void init() {
        Version version = new Version("2.3.30");
        configuration = new Configuration(version);
        configuration.setObjectWrapper(new
                DefaultObjectWrapper(version));
        configuration.setDefaultEncoding("UTF-8");
    }

    @Override
    public TemplateProviderType type() {
        return TemplateProviderType.FREEMARKER;
    }

    @Override
    public String process(String template, String paramName, Object param) {
        try {
            Template fmTemplate = getTemplate(template);
            StringWriter sw = new StringWriter();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put(paramName, param);
            fmTemplate.process(paramMap, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String process(String template, Map<String, Object> param) {
        try {
            Template fmTemplate = getTemplate(template);
            StringWriter sw = new StringWriter();
            fmTemplate.process(param, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String processEntity(String template, Object entity) {
        try {
            Template fmTemplate = getTemplate(template);
            StringWriter sw = new StringWriter();
            fmTemplate.process(entity, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //需要一个模板名字，用来跟踪模板
    private Template getTemplate(String template) throws IOException {
        Template t = templates.get(template);
        if (t != null) {
            return t;
        }
        if (t == null) {
            t = new Template(null, new StringReader(template), configuration);
            templates.put(template, t);
        }
        return t;
    }

    @Override
    public String getCacheID() {
        return "SQL_LOADER_TEMPLAT_GROUP";
    }

    @Override
    public void invalidateAll() {
        this.templates.clear();
    }
}
