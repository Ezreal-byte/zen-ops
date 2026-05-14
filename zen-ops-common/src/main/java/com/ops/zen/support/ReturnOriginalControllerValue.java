package com.ops.zen.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通过该注解让controller返回原始结果
 *
 * @Author xiaoyingnan
 * @Date 2020/7/3 15:42
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReturnOriginalControllerValue {
}
