package com.ops.zen.quartz;

import com.ops.zen.utils.map.KvMap;
import lombok.Data;

import java.util.Map;

/**
 * @author Ezreal
 * @version 2020/7/30 14:10
 * <定时任务-配置类>
 **/
@Data
public class JobConfig extends KvMap {

    /**
     * JobConfig数据在Quartz的JobDataMap中的key
     */
    public final static String JOB_CONFIG = "jobConfig";

    private String clobCfg;

    /**
     * 定时任务主键
     */
    private Long pkSchedule;


    /**
     * 定时任务名称
     */
    private String name;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 触发器类型
     */
    private String triggerType;

    /**
     * 触发器表达式
     */
    private String triggerExpr;

    /**
     * 是否手动触发
     */
    private boolean isManual = false; // 是否手动执行

    public JobConfig(Map<String, Object> innerMap) {
        super(innerMap);
    }
}
