package com.ops.zen.support;

import com.ops.zen.utils.Context;
import com.ops.zen.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.Annotation;

@ControllerAdvice(basePackages = SupportConstant.BASE_PACKAGES)
public class GlobalRestfulResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private Logger logger = LoggerFactory.getLogger(GlobalRestfulResponseBodyAdvice.class);

    @Override
    public Object beforeBodyWrite(Object obj, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest req, ServerHttpResponse resp) {
        try {
            Annotation originalControllerReturnValue = methodParameter.getMethodAnnotation(ReturnOriginalControllerValue.class);
            if (originalControllerReturnValue != null) {
                // 方法存在@ReturnOriginalControllerValue注解或通过消息头判断出是远程调用时不包装为JsonResult
                return obj;
            }
            JsonResult value;
            if (obj instanceof JsonResult) {
                return obj;
            }
            value = new JsonResult(obj);
            if (methodParameter.getParameterType() == String.class) {
                return JsonUtils.toJSONString(value);
            }
            return value;
        } finally {
            String name = OnException.class.getName();
            OnException annoOE = Context.get().getAttribute(name, OnException.class);
            Context.remove(name);
            if (annoOE != null) {
                resp.setStatusCode(HttpStatus.resolve(annoOE.status())); // org.springframework.http.HttpStatus.resolve method resolve's for statement performance? better way
            }
        }
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

}
