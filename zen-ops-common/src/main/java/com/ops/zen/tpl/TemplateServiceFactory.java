package com.ops.zen.tpl;

import com.ops.zen.tpl.beetl.BeetlTemplateService;
import com.ops.zen.tpl.freemarker.FreeMarkerTemplateService;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 模板服务提供者
 *
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public class TemplateServiceFactory {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateServiceFactory.class);

    private ConcurrentMap<TemplateProviderType, TemplateService> services = new ConcurrentHashMap<>();

    private volatile static TemplateServiceFactory _inst;


    private TemplateServiceFactory() {
        init();
    }

    public static TemplateServiceFactory inst() {
        if (_inst == null) {
            synchronized (TemplateServiceFactory.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new TemplateServiceFactory();
            }
        }
        return _inst;
    }
    //</editor-fold>

    private void init() {
        //SPI
//        ServiceLoader<TemplateService> loader = ServiceLoader.load(TemplateService.class);
//        Iterator<TemplateService> iterator = loader.iterator();
//        while (iterator.hasNext()) {
//            try {
//                TemplateService service = iterator.next();
//                logger.info("发现TemplateService {}", service.type());
//                services.put(service.type(), service);
//            } catch (Throwable e) {
//                logger.warn("", e);
//            }
//        }
//        // 保证在cube中使用时，未依赖freemarker时的可用性
//        if (!services.containsKey(TemplateProviderType.BEETL)) {
//            throw new RuntimeException("最小化的模板引擎需求未满足，不存在beetl模板引擎服务");
//        }
        services.put(TemplateProviderType.BEETL, new BeetlTemplateService());
        services.put(TemplateProviderType.FREEMARKER, new FreeMarkerTemplateService());
    }

    /**
     * 优先取beetl，jar包如果没有添加进依赖，则使用no_op服务
     *
     * @return
     */
    public TemplateService getDefault() {
        return services.getOrDefault(TemplateProviderType.BEETL, services.get(TemplateProviderType.NO_OP));
    }

    public TemplateService get(TemplateProviderType type) {
        return services.get(type);
    }
}
