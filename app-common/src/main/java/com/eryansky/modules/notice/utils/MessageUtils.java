/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.utils;

import com.eryansky.common.exception.SystemException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.notice._enum.MessageChannel;
import com.eryansky.modules.notice._enum.MessageReceiveObjectType;
import com.eryansky.modules.notice.mapper.Message;
import com.eryansky.modules.notice.mapper.MessageReceive;
import com.eryansky.modules.notice.service.MessageReceiveService;
import com.eryansky.modules.notice.service.MessageService;
import com.eryansky.modules.notice.task.MessageTask;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.google.common.collect.Lists;
import org.springframework.scheduling.annotation.Async;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 消息工具类
 *
 * @author Eryan
 * @date 2016-03-14
 */
public class MessageUtils {

    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    public static final class Static {
        private static MessageService messageService = SpringContextHolder.getBean(MessageService.class);
        private static MessageReceiveService messageReceiveService = SpringContextHolder.getBean(MessageReceiveService.class);
        private static MessageTask messageTask = SpringContextHolder.getBean(MessageTask.class);
        private static UserService userService = SpringContextHolder.getBean(UserService.class);
    }

    private MessageUtils() {

    }

    public static Message get(String id) {
        if (StringUtils.isNotBlank(id)) {
            return Static.messageService.get(id);
        }
        return null;
    }

    /**
     * 发送系统消息给用户
     *
     * @param receiveUserId 接收用户ID
     * @param content       消息内容
     */
    public static CompletableFuture<Message> sendToUserMessage(String receiveUserId, String content) {
        return sendToUserMessage(receiveUserId, content, null);
    }

    /**
     * 发送系统消息给用户
     *
     * @param receiveUserIds 接收用户IDS
     * @param content        消息内容
     */
    public static CompletableFuture<Message> sendToUserMessage(Collection<String> receiveUserIds, String content) {
        return sendUserMessage(User.SUPERUSER_ID,content,null,receiveUserIds,null);
    }

    /**
     * 发送系统消息给用户
     *
     * @param receiveUserId 接收用户ID
     * @param content       消息内容
     * @param linkUrl       消息链接
     */
    public static CompletableFuture<Message> sendToUserMessage(String receiveUserId, String content,
                                         String linkUrl) {
        return sendUserMessage(User.SUPERUSER_ID, content, linkUrl, Lists.newArrayList(receiveUserId), null);
    }

    /**
     * 发送消息
     *
     * @param sender          发送者用户ID {@link User}
     * @param content         必选 消息内容
     * @param linkUrl         消息URL链接地址
     * @param receiveUserIds   必选 接收对象ID集合
     * @param messageChannels 可选 消息接收通道 默认值：{@link MessageChannel#Message}
     */
    public static CompletableFuture<Message> sendUserMessage(String sender,
                                       String content,
                                       String linkUrl,
                                       Collection<String> receiveUserIds,
                                       List<MessageChannel> messageChannels) {
        return sendMessage(null, sender, content, linkUrl, MessageReceiveObjectType.User.getValue(), receiveUserIds, messageChannels);
    }


    /**
     * 发送消息
     *
     * @param sender            发送者用户ID {@link User}
     * @param content           必选 消息内容
     * @param linkUrl           消息URL链接地址
     * @param receiveObjectType 必选 接收类型 {@link MessageReceiveObjectType}
     * @param receiveObjectIds  必选 接收对象ID集合
     * @param messageChannels   可选 消息接收通道 默认值：{@link MessageChannel#Message}
     */
    public static CompletableFuture<Message> sendMessage(String appId, String sender, String content,
                                   String linkUrl,
                                   String receiveObjectType,
                                   Collection<String> receiveObjectIds,
                                   List<MessageChannel> messageChannels) {
        return sendMessage(appId, sender, content, linkUrl, receiveObjectType, receiveObjectIds, null, messageChannels);
    }

    /**
     * 发送消息
     *
     * @param sender            发送者用户ID {@link User}
     * @param content           必选 消息内容
     * @param linkUrl           消息URL链接地址
     * @param receiveObjectType 必选 接收类型 {@link MessageReceiveObjectType}
     * @param receiveObjectIds  必选 接收对象ID集合
     * @param messageChannels   可选 消息接收通道 默认值：{@link MessageChannel#Message}
     */
    public static CompletableFuture<Message> sendMessage(String appId, String sender, String content,
                                   String linkUrl,
                                   String receiveObjectType, Collection<String> receiveObjectIds, Date date, List<MessageChannel> messageChannels) {
        MessageReceiveObjectType m = MessageReceiveObjectType.getByValue(receiveObjectType);
        return sendMessage(appId, sender, null,null, content, linkUrl, m, receiveObjectIds, date, messageChannels);
    }


    /**
     * 发送消息
     *
     * @param sender                   发送者用户ID {@link User}
     * @param content                  必选 消息内容
     * @param linkUrl                  消息URL链接地址
     * @param messageReceiveObjectType 必选 接收类型 {@link MessageReceiveObjectType}
     * @param receiveObjectIds         必选 接收对象ID集合
     * @param messageChannels          可选 消息接收通道 默认值：{@link MessageChannel#Message}
     */
    public static CompletableFuture<Message> sendMessage(String sender, String content,
                                   String linkUrl,
                                   MessageReceiveObjectType messageReceiveObjectType, List<String> receiveObjectIds, List<MessageChannel> messageChannels) {
        return sendMessage(null, sender,null, null, content, linkUrl, messageReceiveObjectType, receiveObjectIds, null, messageChannels);
    }

    /**
     * 发送消息
     *
     * @param appId                    可选 应用ID
     * @param sender                   发送者用户ID {@link User}
     * @param title                    标题
     * @param category                 可选 消息分类
     * @param content                  必选 消息内容
     * @param linkUrl                  消息URL链接地址
     * @param messageReceiveObjectType 必选 接收类型 {@link MessageReceiveObjectType}
     * @param receiveObjectIds         必选 接收对象ID集合
     * @param messageChannels          可选 消息接收通道 默认值：{@link MessageChannel#Message}
     */
    public static CompletableFuture<Message> sendMessage(String appId, String sender, String title, String category, String content,
                                                         String linkUrl,
                                                         MessageReceiveObjectType messageReceiveObjectType, Collection<String> receiveObjectIds, Date date, List<MessageChannel> messageChannels) {
        Message model = new Message();
        User user = UserUtils.getUser(sender);
        if (user == null) {
            throw new SystemException("[" + sender + "]用户不存在");
        }
        if (Collections3.isEmpty(messageChannels)) {
//            model.setTipMessage(MessageChannel.Message.getValue() + "," + MessageChannel.QYWeixin.getValue() + "," + MessageChannel.APP.getValue());
            model.setTipMessage(NoticeConstants.getMessageDefaultTipChannel());
        } else {
            model.setTipMessage(Collections3.extractToString(messageChannels, "value", ","));
        }

        model.setAppId(appId);
        model.setTitle(title);
        model.setCategory(category);
        model.setOrganId(user.getCompanyId());
        model.setSender(user.getId());
        model.setContent(content);
        model.setUrl(linkUrl);
        model.setSendTime(null != date ? date:Calendar.getInstance().getTime());
        Static.messageService.save(model);
        return Static.messageTask.saveAndSend(model, messageReceiveObjectType, receiveObjectIds);
    }

    /**
     * 消息推送（仅限推送,由切面实现）
     * @param messageId
     * @return
     */
    public static CompletableFuture<Message> pushMessage(String messageId) {
        return Static.messageTask.push(messageId);
    }

    /**
     * 用户消息
     *
     * @param pageNo   第几页
     * @param pageSize 分页大小 不分页：-1
     * @param userId   用户ID
     * @return
     */
    public static Page<MessageReceive> findUserMessages(String userId, int pageNo, int pageSize, String isRead) {
        Page<MessageReceive> page = new Page<>(pageNo, pageSize);
        return Static.messageReceiveService.findUserPage(page, userId, isRead);
    }

    /**
     * 用户消息
     *
     * @param pageNo   第几页
     * @param pageSize 分页大小 不分页：-1
     * @param userId   用户ID
     * @return
     */
    public static Page<MessageReceive> findUserMessages(String userId, int pageNo, int pageSize, String appId, String isRead, String isSend) {
        Page<MessageReceive> page = new Page<>(pageNo, pageSize);
        return Static.messageReceiveService.findUserPage(page, appId, userId,isRead,null,null);
    }
}
