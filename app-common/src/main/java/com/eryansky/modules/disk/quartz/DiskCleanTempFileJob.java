/**
 * Copyright (c) 2012-2017 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.disk.quartz;

import com.eryansky.core.quartz.QuartzJob;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.utils.AppConstants;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 定期清理缓存文件夹
 *
 * @author Eryan
 * @date 2024-01-18
 */
@QuartzJob(enable = true,name = "DiskCleanTempFileJob", cronExp = "0 0 0 * * ?")
public class DiskCleanTempFileJob extends QuartzJobBean {

    private static Logger logger = LoggerFactory.getLogger(DiskCleanTempFileJob.class);


    /**
     * 执行任务
     */
    public void execute() {
        logger.info("定时任务...开始：清空缓存目录[{}]...", AppConstants.getDiskTempDir());
        DiskUtils.clearTempDir();
        logger.info("定时任务...结束：清空缓存目录");
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        execute();
    }

}
