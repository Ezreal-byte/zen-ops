package com.ops.zen.tpl;

import java.util.Map;

/**
 * @Author xyn
 * @Date 2025/4/22 13:08
 * @Description
 */
public interface TemplateService {

    TemplateProviderType type();

    /**
     * 根据参数和模板生成处理结果
     *
     * @param template
     * @param paramName
     * @param param
     * @return
     */
    String process(String template, String paramName, Object param);

    String process(String template, Map<String, Object> param);

    String processEntity(String template, Object entity);

}
