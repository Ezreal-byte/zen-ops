package com.ops.zen.quartz;

import com.google.common.base.Stopwatch;
import com.ops.zen.en.BooleanEn;
import com.ops.zen.en.RunStatusEn;
import com.ops.zen.entity.ScheduleLogWithBLOBs;
import com.ops.zen.mapper.ScheduleLogMapper;
import com.ops.zen.quartz.log.ContextAwareLogger;
import com.ops.zen.quartz.log.LogContextManager;
import com.ops.zen.utils.ApplicationContextUtils;
import com.ops.zen.utils.pk.SnowPkGenerator;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xyn
 * @date 2025/5/23 21:11
 * @description
 **/
@Slf4j
public abstract class AbstractJob implements Job {


    // 使用自定义的上下文感知日志记录器
    protected static final ContextAwareLogger logger = new ContextAwareLogger(AbstractJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //获取jsb配置
        LocalDateTime dtRun = LocalDateTime.now();
        JobConfig jobConfig = (JobConfig) jobExecutionContext.getJobDetail().getJobDataMap().get(JobConfig.JOB_CONFIG);
        //先日志入库  最后更新日志
        Long logId = SnowPkGenerator.generateSnow();
        insertLog(logId, jobConfig.getPkSchedule(), dtRun, jobConfig.getClobCfg(), jobConfig.isManual());
        Stopwatch sw = Stopwatch.createStarted();
        boolean execSuccess = false;
//        String errorMsg = null;
        String execResult = null;
        try {
            execResult = execute(jobConfig);
            execSuccess = true;
        } catch (Exception e) {
            logger.error("定时任务执行异常,任务名称【{}】", jobConfig.getName(), e);
            execSuccess = false;
//            errorMsg = Exceptions.trace(e);
        } finally {
            sw.stop();
            long elapsed = sw.elapsed(TimeUnit.MILLISECONDS);
            logger.info("任务【{}】执行完毕，耗时【{}】毫秒", jobConfig.getName(), elapsed);
            //获取代理对象的所有log日志
            updateLog(logId,
                    execSuccess ? RunStatusEn.SUCCESS : RunStatusEn.FAIL,
                    execResult, elapsed, LogContextManager.getLogsAsString());
        }
    }



    /**
     * 由子类实现
     *
     * @param jobConfig
     * @return
     * @throws Exception
     */
    public abstract String execute(JobConfig jobConfig) throws Exception;


    private void insertLog(Long pkScheduleLog, Long pkSchedule,
                           LocalDateTime dtRun, String runCfg, boolean isManual) {
        ScheduleLogWithBLOBs log = new ScheduleLogWithBLOBs();
        log.setPkScheduleLog(pkScheduleLog);
        log.setPkSchedule(pkSchedule);
        log.setRunCfg(runCfg);
        log.setDtRun(dtRun);
        log.setStatusRun(RunStatusEn.RUNNING);
        log.setDtCreated(LocalDateTime.now());
        log.setIsManual(isManual ? BooleanEn.TRUE : BooleanEn.FALSE);
        ScheduleLogMapper mapper = ApplicationContextUtils.get(ScheduleLogMapper.class);
        mapper.insertSelective(log);
    }


    /**
     * 插入执行日志
     * @param pkScheduleLog
     * @param statusRun
     * @param resultRun
     * @param elapsedTime
     * @param clobLog
     */
    private void updateLog(Long pkScheduleLog,
                           Byte statusRun, String resultRun,
                           Long elapsedTime, String clobLog) {
        //插入日志和时间
        //异步插入
        CompletableFuture.runAsync(() -> {
            try {
                ScheduleLogWithBLOBs log = new ScheduleLogWithBLOBs();
                log.setPkScheduleLog(pkScheduleLog);
                log.setStatusRun(statusRun);
                log.setResultRun(resultRun);
                log.setElapsedTime(elapsedTime.toString());
                log.setClobLog(clobLog);
                log.setDtCreated(LocalDateTime.now());
                //获取 scheduleLogMapper 对象
                ScheduleLogMapper mapper = ApplicationContextUtils.get(ScheduleLogMapper.class);
                mapper.updateByPrimaryKeySelective(log);
            } catch (Exception e) {
                log.error("定时任务日志更新异常,任务名称【{}】", pkScheduleLog, e);
            }
        });

    }
}
