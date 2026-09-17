/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.task;

import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys.service.UserDeviceService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 安全相关送异步任务
 *
 * @author Eryan
 * @date 2026-09-17
 */
@Component
public class SecurityTask {

    private static final Logger logger = LoggerFactory.getLogger(SecurityTask.class);

    @Resource
    private UserDeviceService userDeviceService;

    @Async
    public void saveOrUpdateUserDevice(SessionInfo sessionInfo) {
        userDeviceService.saveOrUpdate(sessionInfo);
    }
}
