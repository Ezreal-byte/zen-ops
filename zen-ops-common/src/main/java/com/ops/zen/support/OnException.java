package com.ops.zen.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * controller方法如果存在该注解，当方法抛出异常以后回应将http status code置为500（内部错误）
 *
 * @Author xiaoyingnan
 * @Date 2021/10/26 9:11
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnException {

    /**
     * 被限定方法若抛出异常，回应Response的http status code的默认值
     *
     * @return
     */
    int status() default 500;

}
