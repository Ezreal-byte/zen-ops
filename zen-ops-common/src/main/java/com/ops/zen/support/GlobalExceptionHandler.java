package com.ops.zen.support;

import com.ops.zen.utils.ex.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackages = SupportConstant.BASE_PACKAGES)
public class GlobalExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonResult<Object> jsonErrorHandler(HttpServletRequest req, Exception ex) throws Exception {
        JsonResult<Object> value = null;
        if (ex != null) {
            logger.error("Controller全局异常处理器捕获异常", ex);
            value = new JsonResult<Object>(-1, ex.getMessage());
        }
        value.setStack(Exceptions.trace(ex));
        return value;
    }
}
