package com.ops.zen.starter;

import com.ops.zen.en.BooleanEn;
import com.ops.zen.entity.ScheduleExample;
import com.ops.zen.entity.ScheduleWithBLOBs;
import com.ops.zen.mapper.ScheduleMapper;
import com.ops.zen.quartz.JobConfig;
import com.ops.zen.quartz.QuartzMgr;
import com.ops.zen.utils.PropertiesUtils;
import com.ops.zen.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author xyn
 * @date 2025/5/23 21:33
 * @description
 **/
@Slf4j
//@Component
public class ScheduleStarter implements ApplicationRunner {

    @Autowired
    ScheduleMapper scheduleMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<ScheduleWithBLOBs> schedules = getActiveSchedules();
        for (ScheduleWithBLOBs schedule : schedules) {
            //执行任务类
            Class clazz = Class.forName(schedule.getJobClass());
            Map<String, Object> jobConfigMap = parseJonConfigMap(schedule.getClobCfg());
            // 解析properties为参数
            JobConfig jobConfig = new JobConfig(jobConfigMap);
            jobConfig.setPkSchedule(schedule.getPkSchedule());
            jobConfig.setName(schedule.getName());
            jobConfig.setGroupName(schedule.getPkScheduleGroup().toString());
            jobConfig.setTriggerType(schedule.getTriggerType());
            jobConfig.setTriggerExpr(schedule.getTriggerExpr());
            jobConfig.setClobCfg(schedule.getClobCfg());
            QuartzMgr.inst().addJob(
                    schedule.getPkScheduleGroup().toString(),
                    schedule.getName(),
                    clazz,
                    schedule.getTriggerType(),
                    schedule.getTriggerExpr(),
                    jobConfig
                    );
            log.info("register job to scheduler: " + schedule.getName());
        }
    }

    /**
     * 解析properties
     * @param clobCfg
     * @return
     */
    private Map<String, Object> parseJonConfigMap(String clobCfg) throws IOException {
        if (StringUtils.isEmpty(clobCfg)) {
            return null;
        } else {
            return PropertiesUtils.strToHashMap(clobCfg);
        }
    }

    private List<ScheduleWithBLOBs> getActiveSchedules() {
        ScheduleExample example = new ScheduleExample();
        ScheduleExample.Criteria criteria = example.createCriteria();
        criteria.andIsActiveEqualTo(BooleanEn.TRUE);
        example.setOrderByClause("dt_created asc");
        return scheduleMapper.selectByExampleWithBLOBs(example);
    }
}
