package com.ops.zen.jdbc.annotation;

import java.lang.annotation.*;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})//, ElementType.METHOD
@Documented
public @interface Entity {

    String name() default "";

}
