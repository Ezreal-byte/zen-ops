package com.ops.zen.utils.en;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author xiaoyingnan
 * @Date 2020/6/11 13:25
 * @Description
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumDescription {

    /**
     * 描述
     *
     * @return
     */
    String remark();

    /**
     * 名称
     *
     * @return
     */
    String name() default "";

    /**
     * 分组
     *
     * @return
     */
    String group() default "";

    /**
     * 类型（扩展用）
     *
     * @return
     */
    Class<?> extType() default String.class;

    /**
     * 枚举对应的默认值（扩展用）
     *
     * @return
     */
    String extDefaultValue() default "";
}
