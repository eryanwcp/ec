/**
 * Copyright (c) 2012-2017 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.quartz;

import com.eryansky.core.quartz.QuartzJob;
import com.eryansky.modules.sys.service.SystemService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 同步organ扩展表
 *
 * @author Eryan
 * @date 2017-09-19
 */
@QuartzJob(name = "SyncOrganToExtendJob", cronExp = "0 0 5 * * ?")
public class SyncOrganToExtendJob extends QuartzJobBean {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SystemService systemService;

    /**
     * 执行任务
     */
    public void execute() {
        logger.info("定时任务...开始：同步organ扩展表");
        systemService.syncOrganToExtend();
        logger.info("定时任务...结束：同步organ扩展表");
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        execute();
    }

}
