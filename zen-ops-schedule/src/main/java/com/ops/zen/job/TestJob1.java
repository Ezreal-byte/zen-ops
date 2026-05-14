package com.ops.zen.job;

import com.ops.zen.quartz.AbstractJob;
import com.ops.zen.quartz.JobConfig;
import com.ops.zen.utils.ThreadUtils;

/**
 * @author xyn
 * @date 2025/5/23 21:26
 * @description
 **/
public class TestJob1 extends AbstractJob {


    @Override
    public String execute(JobConfig jobConfig) throws Exception {
        logger.info("任务【{}】开始执行, 参数【{}】", jobConfig.getName(), jobConfig.toString());
        for (int i = 0; i < 100; i++) {
            logger.info("任务【{}】正在执行，循环第【{}】次", jobConfig.getName(), i+1);
            ThreadUtils.sleep(1000);
        }

        return "hahaha";
    }
}
