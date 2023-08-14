/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.service;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.modules.notice._enum.MessageMode;
import com.eryansky.modules.notice.dao.MessageReceiveDao;
import com.eryansky.modules.notice.mapper.Message;
import com.eryansky.modules.notice.mapper.MessageReceive;
import com.eryansky.modules.sys._enum.YesOrNo;
//import com.eryansky.modules.weixin.utils.WeixinUtils;
import com.eryansky.modules.sys.mapper.VersionLog;
import com.eryansky.utils.AppConstants;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author Eryan
 * @date 2016-03-14
 */
@Service
public class MessageReceiveService extends CrudService<MessageReceiveDao, MessageReceive> {


    /**
     * 根据消息ID删除
     *
     * @param messageId
     * @return
     */
    public int deleteByMessageId(String messageId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("messageId", messageId);
        return dao.deleteByMessageId(parameter);
    }

    @Override
    public Page<MessageReceive> findPage(Page<MessageReceive> page, MessageReceive entity) {
        entity.setEntityPage(page);
        page.setResult(dao.findList(entity));
        return page;
    }

    /**
     * 用户消息
     *
     * @param page
     * @param userId 用户ID
     * @param isRead
     * @return
     */
    public Page<MessageReceive> findUserPage(Page<MessageReceive> page, String userId, String isRead) {
        return findUserPage(page, VersionLog.DEFAULT_ID, userId, isRead, null, null);
    }

    /**
     * 用户消息
     *
     * @param page
     * @param appId
     * @param userId 用户ID
     * @param isRead
     * @param isSend
     * @param params
     * @return
     */
    public Page<MessageReceive> findUserPage(Page<MessageReceive> page, String appId, String userId, String isRead, String isSend, Map<String, Object> params) {
        Parameter parameter = new Parameter();
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(Message.FIELD_STATUS, Message.STATUS_NORMAL);
        parameter.put("bizMode", MessageMode.Published.getValue());
        parameter.put("appId", appId);
//        parameter.put("appId", StringUtils.isNotBlank(appId) ? appId : VersionLog.DEFAULT_ID);
        parameter.put("userId", userId);
        parameter.put("isRead", isRead);
        parameter.put("isSend", isSend);
        if (null != params) {
            params.forEach(parameter::putIfAbsent);
        }
        page.setResult(dao.findUserList(parameter));
        return page;
    }

    /**
     * 用户未发送成功的消息
     *
     * @param userId
     * @return
     */
    public List<MessageReceive> findUnSendByUserId(String userId) {
        Parameter parameter = new Parameter();
        parameter.put(Message.FIELD_STATUS, Message.STATUS_NORMAL);
        parameter.put("bizMode", MessageMode.Published.getValue());
        parameter.put("userId", userId);
        return dao.findUserList(parameter);
    }

    /**
     * 根据用户ID以及消息ID查找
     *
     * @param userId
     * @param messageId
     * @return
     */
    public MessageReceive getUserMessageReceiveByMessageId(String userId, String messageId) {
        Parameter parameter = new Parameter();
        parameter.put(Message.FIELD_STATUS, Message.STATUS_NORMAL);
        parameter.put("userId", userId);
        parameter.put("messageId", messageId);
        return dao.getUserMessageReceiveByMessageId(parameter);
    }


    /**
     * 根据用户ID以及消息ID设置未发送成功
     *
     * @param userId
     * @param messageId
     * @return
     */
    public int updateByUserIdAndMessageId(String userId, String messageId, String isSend, String isRead) {
        MessageReceive messageReceive = new MessageReceive();
        messageReceive.setUserId(userId);
        messageReceive.setMessageId(messageId);
        messageReceive.setIsSend(YesOrNo.YES.getValue().equals(isSend) ? YesOrNo.YES.getValue() : YesOrNo.NO.getValue());
        messageReceive.setIsRead(YesOrNo.YES.getValue().equals(isRead) ? YesOrNo.YES.getValue() : YesOrNo.NO.getValue());
        if(YesOrNo.YES.getValue().equals((messageReceive.getIsRead()))){
            messageReceive.setReadTime(Calendar.getInstance().getTime());
        }
        return dao.updateByUserIdAndMessageId(messageReceive);
    }


    /**
     * 设置通知已读状态
     *
     * @param receive
     */
    public void setRead(MessageReceive receive) {
        receive.setIsRead(YesOrNo.YES.getValue());
        receive.setReadTime(Calendar.getInstance().getTime());
        receive.setIsSend(YesOrNo.YES.getValue());
        this.save(receive);
    }

    /**
     * 设置通知已读状态
     *
     * @param userId
     * @param isRead
     */
    public int setReadAll(String userId, String isRead) {
        return setReadAll(Message.DEFAULT_ID, userId, isRead);
    }

    /**
     * 设置通知已读状态
     *
     * @param userId
     * @param isRead
     */
    public int setReadAll(String appId, String userId, String isRead) {
        Parameter parameter = new Parameter();
        parameter.put(Message.FIELD_STATUS, Message.STATUS_NORMAL);
        parameter.put("appId", appId);
        parameter.put("userId", userId);
        parameter.put("isSend", YesOrNo.YES.getValue());
        parameter.put("isRead", StringUtils.isBlank(isRead) ? YesOrNo.YES.getValue() : isRead);
        parameter.put("readTime", Calendar.getInstance().getTime());
        return dao.setUserMessageRead(parameter);
    }


    /**
     * 重新推送
     *
     * @param userId
     */
    public void reSendByUserId(String userId) {
        List<MessageReceive> list = findUnSendByUserId(userId);
        for (MessageReceive messageReceive : list) {
            reSendByMessageReceive(messageReceive);
        }
    }

    /**
     * 重新推送
     *
     * @param messageReceive
     */
    private void reSendByMessageReceive(MessageReceive messageReceive) {
//        Message message = messageReceive.getMessage();
//        boolean flag = WeixinUtils.sendTextMsg(messageReceive.getUserId(), message.getContent(), message.getUrl());
//        messageReceive.setIsSend(flag ? YesOrNo.YES.getValue() : YesOrNo.NO.getValue());
//        save(messageReceive);
    }


    /**
     * 重新推送
     *
     * @param messageReceiveId
     */
    public void reSendByMessageReceiveId(String messageReceiveId) {
//        MessageReceive messageReceive = get(messageReceiveId);
//        Message message = messageReceive.getMessage();
//        boolean flag = WeixinUtils.sendTextMsg(messageReceive.getUserId(), message.getContent(), message.getUrl());
//        messageReceive.setIsSend(flag ? YesOrNo.YES.getValue() : YesOrNo.NO.getValue());
//        save(messageReceive);
    }

    /**
     * 根据消息ID查找
     *
     * @param messageId 消息ID
     * @return
     */
    public List<MessageReceive> findByMessageId(String messageId) {
        return findByMessageId(messageId, null, null);
    }


    /**
     * 根据消息ID查找
     *
     * @param messageId 消息ID
     * @param isRead    是否阅读 {@link YesOrNo}
     * @param isSend    消是否发送成功 {@link YesOrNo}
     * @return
     */
    public List<MessageReceive> findByMessageId(String messageId, String isRead, String isSend) {
        Parameter parameter = new Parameter();
        parameter.put(Message.FIELD_STATUS, Message.STATUS_NORMAL);
        parameter.put("messageId", messageId);
        parameter.put("isRead", isRead);
        parameter.put("isSend", isSend);
        return dao.findByMessageId(parameter);
    }

    /**
     * 根据消息ID查找
     *
     * @param page
     * @param messageId 消息ID
     * @return
     */
    public Page<MessageReceive> findPageByMessageId(Page<MessageReceive> page, String messageId) {
        return findPageByMessageId(page, messageId,null,null);
    }

    /**
     * 根据消息ID查找
     *
     * @param page
     * @param messageId 消息ID
     * @param isRead    是否阅读 {@link YesOrNo}
     * @param isSend    消是否发送成功 {@link YesOrNo}
     * @return
     */
    public Page<MessageReceive> findPageByMessageId(Page<MessageReceive> page, String messageId, String isRead, String isSend) {
        Parameter parameter = new Parameter();
        parameter.put(Message.FIELD_STATUS, Message.STATUS_NORMAL);
        parameter.put("messageId", messageId);
        parameter.put("isRead", isRead);
        parameter.put("isSend", isSend);
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        return page.setResult(dao.findByMessageId(parameter));
    }


    /**
     * 根据消息发送人查找
     *
     * @param senderUserId 消息发送人ID
     * @return
     */
    public List<MessageReceive> findBySenderUserId(String senderUserId) {
        return findBySenderUserId(senderUserId, null, null, null);
    }


    /**
     * 根据消息发送人查找
     *
     * @param senderUserId 消息发送人ID
     * @param messageId    消息ID
     * @param isRead       是否阅读 {@link YesOrNo}
     * @param isSend       消是否发送成功 {@link YesOrNo}
     * @return
     */
    public List<MessageReceive> findBySenderUserId(String senderUserId, String messageId, String isRead, String isSend) {
        Parameter parameter = new Parameter();
        parameter.put(Message.FIELD_STATUS, Message.STATUS_NORMAL);
        parameter.put("senderUserId", senderUserId);
        parameter.put("messageId", messageId);
        parameter.put("isRead", isRead);
        parameter.put("isSend", isSend);
        return dao.findBySenderUserId(parameter);
    }


    /**
     * 根据消息发送人查找（分页）
     *
     * @param senderUserId 消息发送人ID
     * @return
     */
    public Page<MessageReceive> findPageBySenderUserId(Page<MessageReceive> page, String senderUserId) {
        return findPageBySenderUserId(page, senderUserId, null, null, null);
    }

    /**
     * 根据消息发送人查找（分页）
     *
     * @param senderUserId 消息发送人ID
     * @param messageId    消息ID
     * @param isRead       是否阅读 {@link YesOrNo}
     * @param isSend       消是否发送成功 {@link YesOrNo}
     * @return
     */
    public Page<MessageReceive> findPageBySenderUserId(Page<MessageReceive> page, String senderUserId, String messageId, String isRead, String isSend) {
        Parameter parameter = new Parameter();
        parameter.put(Message.FIELD_STATUS, Message.STATUS_NORMAL);
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("senderUserId", senderUserId);
        parameter.put("messageId", messageId);
        parameter.put("isRead", isRead);
        parameter.put("isSend", isSend);
        return page.setResult(dao.findBySenderUserId(parameter));
    }

}
