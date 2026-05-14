package com.ops.zen.quartz;

import com.ops.zen.en.TriggerTypeEn;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;

/**
 * @author Ezreal
 * @version 2020/7/30 9:37
 * <定时任务-任务管理器>
 **/
public class QuartzMgr {

    private static final Logger logger = LoggerFactory.getLogger(QuartzMgr.class);

    //通过一个Scheduler实例，方便管理正在执行的Job  Scheduler代表一个调度容器   一个调度容器可以包含多个Tirgger和JobDetail
    private Scheduler scheduler;


    private volatile static QuartzMgr _inst;

    private QuartzMgr() {
    }

    public static QuartzMgr inst() {
        if (_inst == null) {
            synchronized (QuartzMgr.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new QuartzMgr();
                _inst.start();
            }
        }
        return _inst;
    }

    /**
     * 是否已启动
     */
    private boolean isStart = false;

    public boolean isStart() {
        return isStart;
    }

    /**
     *
     */
    public void start() {
        if (isStart) {
            throw new RuntimeException("只能start一个scheduler实例!");
        }
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            this.isStart = true;
        } catch (SchedulerException e) {
            logger.error("定时任务管理器-构造异常,异常信息【{}】", e);
        }
    }

    /**
     * 添加定时任务
     *
     * @param groupName 组名称
     * @param jobName   不重复的任务名称
     * @param jobClazz  AbstractJob实现类
     * @param triggerExpr      cron表达式
     * @param jobConfig 携带的参数，可在Job内部获取该参数
     */
    public void addJob(String groupName, String jobName, Class<? extends Job> jobClazz, String triggerType, String triggerExpr, JobConfig jobConfig) throws Exception {
        logger.info("定时任务管理器-添加定时任务,组名称->【{}】,任务名称->【{}】,任务类->【{}】,cron表达式->【{}】", groupName, jobName, jobClazz, triggerExpr);
        try {

            JobDataMap map = new JobDataMap();
            if (jobConfig != null) {
                map.put(JobConfig.JOB_CONFIG, jobConfig);
            }

            ScheduleBuilder schedBuilder = Objects.equals(TriggerTypeEn.CRON, triggerType) ?
                    CronScheduleBuilder.cronSchedule(triggerExpr)
                    : SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(Integer.parseInt(triggerExpr)).repeatForever();
            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(jobName, groupName)
                    .withSchedule(schedBuilder)
                    .build();
            JobDetail jobDetail = JobBuilder.newJob(jobClazz)
                    .usingJobData(map)
                    .withIdentity(jobName, groupName)
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            logger.error("定时任务管理器-添加定时任务的异常信息-->【{}】", e);
            throw e;
        }
    }

    /**
     * 在预定的时间执行一次任务
     *
     * @param groupName   组名称
     * @param jobName     不重复的任务名称
     * @param jobClazz    AbstractJob实现类
     * @param executeTime 执行任务的时间
     * @param jobConfig   附带的参数
     */
    public void addOneTimeJob(String groupName, String jobName, Class<? extends Job> jobClazz, long executeTime, JobConfig jobConfig) {
        logger.info("定时任务管理器-添加一个在预定的时间执行一次任务,组名称->【{}】,任务名称->【{}】,任务类->【{}】,执行时间->【{}】", groupName, jobName, jobClazz, executeTime);
        try {
            JobDataMap map = new JobDataMap();
            if (jobConfig != null) {
                map.put(JobConfig.JOB_CONFIG, jobConfig);
            }

            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(jobName, groupName)
                    .startAt(new Date(executeTime))
                    .build();
            JobDetail jobDetail = JobBuilder.newJob(jobClazz)
                    .usingJobData(map)
                    .withIdentity(jobName, groupName)
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            logger.error("定时任务管理器-添加一个在预定的时间执行一次任务的异常信息-->【{}】", e);
        }
    }

    /**
     * 删除任务
     *
     * @param groupName
     * @param jobName
     */
    public void removeJob(String groupName, String jobName) {
        try {
            JobKey jkey = new JobKey(jobName, groupName);
            if (!scheduler.checkExists(jkey)) {
                return;
            }
            logger.info("定时任务管理器-注销一个定时任务,组名称->【{}】,任务名称->【{}】", groupName, jobName);
            TriggerKey key = new TriggerKey(jobName, groupName);
            scheduler.pauseTrigger(key);
            scheduler.unscheduleJob(key);
            scheduler.deleteJob(new JobKey(jobName, groupName));
        } catch (Exception e) {
            logger.error("定时任务管理器-删除定时任务异常信息-->【{}】", e);
        }
    }

    /**
     * 关闭定时任务
     */
    public void shutDown() {
        logger.info("定时任务管理器-关闭定时任务-->shutDown()");
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            logger.error("定时任务管理器-关闭定时任务异常信息-->【{}】", e);
        }
    }

}
