/**
 * Copyright (c) 2012-2026 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.quartz;

import com.eryansky.core.quartz.QuartzJob;
import com.eryansky.modules.sys.service.SystemService;
import jakarta.annotation.Resource;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 同步organ扩展表
 *
 * @author Eryan
 * @date 2017-09-19
 */
@QuartzJob(name = "SyncOrganToExtendJob", cronExp = "0 0 5 * * ?",remark = "同步organ扩展表")
public class SyncOrganToExtendJob extends QuartzJobBean {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SystemService systemService;

    /**
     * 执行任务
     */
    public void execute() {
        logger.info("定时任务...开始：同步organ扩展表");
        systemService.syncOrganToExtendAuto();
        logger.info("定时任务...结束：同步organ扩展表");
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        execute();
    }

}
