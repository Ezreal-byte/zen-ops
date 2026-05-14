package com.ops.zen.support;

import com.ops.zen.utils.Context;
import com.ops.zen.utils.IOUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.io.Closeable;
import java.lang.reflect.Method;


/**
 * 切面，用来控制controller的统一处理
 *
 * @author xiaoyingnan
 */
@Component
@Aspect
@Order(1)
@ControllerAdvice
public class GlobalControllerAdvice {

    private Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    /**
     * &&（and）表达式
     */
    @Pointcut(SupportConstant.POINT_CUT_CONTROLLER_METHOD_AROUND)
    private void controllerAspect() {
    }


    @AfterReturning(value = "controllerAspect()", returning = "resultMap")
    public void aftercut(JoinPoint joinpoint, Object resultMap) {
//        logger.debug("after");
    }

    /**
     * @param pjp
     * @return
     */
    @Around(value = "controllerAspect()")
    public Object packInvoke(final ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        try {
                result = pjp.proceed();
        } catch (Throwable throwable) {
            OnException annotation = method.getAnnotation(OnException.class);
            if (annotation != null) {
                Context.get().setAttribute(OnException.class.getName(), annotation);
            }
            throw throwable;
        } finally {
            closeCommandSession();
        }
        return result;
    }

    private void closeCommandSession() {
        Object commandSessionInCtx = Context.remove(ContextConst.COMMAND_SESSION);
        IOUtils.close((Closeable) commandSessionInCtx);
        commandSessionInCtx = null;
    }
}
