/**
 * Copyright (c) 2012-2017 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.quartz;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.quartz.QuartzJob;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.SystemService;
import com.eryansky.modules.sys.service.UserService;
import com.google.common.collect.Lists;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;

/**
 * 数据修正任务
 *
 * @author Eryan
 * @date 2023-03-09
 */
@QuartzJob(enable = false,name = "FixedDataJob", cronExp = "0 0 5 * * ?")
public class FixedDataJob extends QuartzJobBean {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    /**
     * 执行任务
     */
    public void execute() {
        logger.info("定时任务...开始：数据修正任务");
        logger.info("清理用户不在部门的岗位信息...");
        //自动清理用户不在部门的岗位信息 仅保留当前部门岗位信息
        List<User> userList = userService.findAllNormal();
        userList.forEach(v->{
            int d = userService.deleteNotInUserOrgansPostsByUserId(v.getId(), Lists.newArrayList(v.getDefaultOrganId()));
            if(d != 0){
                logger.warn("删除用户不在部门的岗位数据：{} {} {}",d,v.getId(), v.getDefaultOrganId());
            }
        });
        logger.info("清理用户不在部门的岗位信息结束");
        logger.info("定时任务...结束：数据修正任务");
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        execute();
    }

}
