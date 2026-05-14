package com.ops.zen.fso;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 标注在Config字段上，用于声明前端表单元数据（label、是否必填、占位符等）
 * 后续新增中间件类型只需创建Config类并标注此注解，前端表单即可自动渲染
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormField {

    /**
     * 表单标签
     */
    String label();

    /**
     * 是否必填
     */
    boolean required() default false;

    /**
     * 占位提示
     */
    String placeholder() default "";

    /**
     * 输入类型: text / password / number 等
     */
    String inputType() default "text";

    /**
     * 排序号（越小越靠前）
     */
    int order() default 0;
}
