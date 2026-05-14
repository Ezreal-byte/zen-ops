package com.ops.zen.jdbc.annotation;

import java.lang.annotation.*;

/**
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})//, ElementType.METHOD
@Documented
public @interface EntityField {

    /**
     * 表的字段名，下划线方式，大小写不敏感，在进行处理时全部转换为小写
     *
     * @return
     */
    String name() default "";

    /**
     * 是否为主键
     *
     * @return
     */
    boolean pk() default false;

}
