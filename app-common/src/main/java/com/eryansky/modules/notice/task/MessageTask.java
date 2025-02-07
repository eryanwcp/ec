/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.task;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.modules.notice._enum.MessageReceiveObjectType;
import com.eryansky.modules.notice.mapper.Message;
import com.eryansky.modules.notice.mapper.MessageReceive;
import com.eryansky.modules.notice.service.MessageReceiveService;
import com.eryansky.modules.notice.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 消息发送异步任务
 *
 * @author Eryan
 * @date 2016-03-15
 */
@Component
public class MessageTask {

    private static Logger logger = LoggerFactory.getLogger(MessageTask.class);

    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageReceiveService messageReceiveService;

    /**
     * 消息推送（仅限推送,由切面实现）
     * @param messageId
     * @return
     */
    @Async
    public CompletableFuture<Message> push(String messageId) {
        return CompletableFuture.completedFuture(messageService.push(messageId));
    }

    /**
     * 设置通知已读状态
     *
     * @param receive
     */
    @Async
    public CompletableFuture<Void> setRead(MessageReceive receive) {
        messageReceiveService.setRead(receive);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 设置通知已读状态
     *
     * @param userId
     */
    @Async
    public CompletableFuture<Void> setReadAll(String userId) {
        messageReceiveService.setReadAll(userId, null);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 重新推送用户消息
     *
     * @param userId
     */
    @Async
    public CompletableFuture<Void>  reSendByUserId(String userId) {
        messageReceiveService.reSendByUserId(userId);
        return CompletableFuture.completedFuture(null);
    }
}
