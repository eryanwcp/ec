/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.task;

import com.eryansky.modules.sys.event.SysLogEvent;
import com.eryansky.modules.sys.mapper.Log;
import com.eryansky.modules.sys.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Eryan
 * @date 2020-03-16
 */
@Component
public class SysLogListener implements ApplicationListener<SysLogEvent> {

    @Autowired
    private LogService logService;

    @Async
    @Override
    public void onApplicationEvent(SysLogEvent event) {
        Log log = (Log) event.getSource();
        logService.save(log);
    }


}
