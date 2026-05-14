package com.ops.zen.select;

import com.ops.zen.utils.en.EnumDescription;

/**
 * @author xyn
 * @date 2025/4/9 20:56
 * @description 下拉模型类型
 **/
public interface SelectModelTypeEnum {

    @EnumDescription(remark = "SQL查询", extType = SQLCustomSelectParams.class)
    String SQL_CUSTOM = "SQL";


    @EnumDescription(remark = "枚举", extType = EnSelectParams.class)
    String ENUM = "EN";

}
