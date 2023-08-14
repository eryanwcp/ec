/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.quartz;

import com.eryansky.common.utils.ThreadUtils;
import com.eryansky.core.quartz.QuartzJob;
import com.eryansky.modules.notice.service.NoticeService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 通知 后台定时任务..
 * @author Eryan
 * @date 2015-2-4 上午09:08:18
 */
//@QuartzJob(name = "NoticeJob", cronExp = "0 0/1 * * * ?",instanceId = "ERYAN")
@QuartzJob(name = "NoticeJob", cronExp = "0 0/10 6-23 * * ?")
public class NoticeJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(NoticeJob.class);

    @Autowired
    private NoticeService noticeService;

    /**
     * 轮询通知
     */
    public void execute() {
        logger.debug("通知公告-轮询任务开始...");
        noticeService.pollNotice();
        logger.debug("通知公告-轮询任务结束.");
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        execute();
    }
}