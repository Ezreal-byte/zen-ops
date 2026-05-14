package com.ops.zen.jdbc.mixed;


import com.ops.zen.jdbc.annotation.*;
import com.ops.zen.jdbc.annotation.*;
import com.ops.zen.utils.Reflects;
//import org.nutz.dao.entity.annotation.Column;
//import org.nutz.dao.entity.annotation.Id;
//import org.nutz.dao.entity.annotation.Table;

import java.lang.reflect.Field;

/**
 * 将EasyJdbc和Nutz Dao的注解融合使用，优先使用EasyJdbc的注解<br>
 * 如果没找到则使用Nutz Dao的注解，注意Nutz依赖范围是provided - （生命周期是编译、测试）
 * 缓存方式获取注解，提升性能
 *
 * @Author xyn
 * @Date 2025/04/20 10:58
 * @Description
 */
public class AnnotationMixed {


    public static EntityFieldWrapper getEntityField(Field fld) {

        EntityField annotation = Reflects.getFldAnnotation(fld, EntityField.class);
        if (annotation == null) {
            Column column = Reflects.getFldAnnotation(fld, Column.class);
            if (column == null) {
                return null;
            } else {
                return new EntityFieldWrapper(column.value(), Reflects.getFldAnnotation(fld, Id.class) != null);
            }
        } else {
            return new EntityFieldWrapper(annotation.name(), annotation.pk());
        }
    }

    public static String parseTableName(Class<?> clazz) {
        Entity annotation = Reflects.getClsAnnotation(clazz, Entity.class);
        if (annotation != null) {
            return annotation.name();
        } else {
            Table table = Reflects.getClsAnnotation(clazz, Table.class);
            if (table != null) {
                return table.value();
            } else {
                throw new RuntimeException("实体类注解Entity注解不能为空");
            }
        }
    }
}
