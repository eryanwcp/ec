/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.service;

import com.eryansky.common.exception.SystemException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.vo.NoticeQueryVo;
import com.eryansky.modules.sys.utils.UserUtils;
//import com.eryansky.modules.weixin.utils.WeixinUtils;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Lists;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.notice._enum.MessageMode;
import com.eryansky.modules.notice._enum.MessageReceiveObjectType;
import com.eryansky.modules.notice.dao.MessageDao;
import com.eryansky.modules.notice.mapper.Message;
import com.eryansky.modules.notice.mapper.MessageReceive;
import com.eryansky.modules.notice.mapper.MessageSender;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.service.UserService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Eryan
 * @date 2016-03-14
 */
@Service
public class MessageService extends CrudService<MessageDao, Message> {

    @Autowired
    private MessageSenderService messageSenderService;
    @Autowired
    private MessageReceiveService messageReceiveService;
    @Autowired
    private UserService userService;


    public Page<Message> findQueryPage(Page<Message> page, String appId,String userId,String status, Date startTime, Date endTime, boolean isDataScopeFilter, Map<String,Object> params) {
        Parameter parameter = Parameter.newParameter();
        Map<String, String> sqlMap = Maps.newHashMap();
        sqlMap.put("dsf", "");
        if(isDataScopeFilter){
            sqlMap.put("dsf", super.dataScopeFilter(SecurityUtils.getCurrentUser(), "o", "u"));
        }
        parameter.put("sqlMap", sqlMap);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("appId",appId);
        parameter.put("userId",userId);
        parameter.put("status",StringUtils.isBlank(status) ? DataEntity.STATUS_NORMAL:status);
        parameter.put("startTime", DateUtils.format(startTime,DateUtils.DATE_TIME_FORMAT));
        parameter.put("endTime", DateUtils.format(endTime,DateUtils.DATE_TIME_FORMAT));
        if (null != params) {
            params.forEach(parameter::putIfAbsent);
        }
        page.setResult(dao.findQueryList(parameter));
        return page;
    }

    /**
     *
     * @param page
     * @param appId
     * @param userId 分级授权用户ID
     * @param startTime
     * @param endTime
     * @param params
     * @return
     */
    public Page<Message> findPage(Page<Message> page, String appId, String userId, Date startTime, Date endTime, Map<String,Object> params) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("appId",appId);
        parameter.put("startTime", DateUtils.format(startTime,DateUtils.DATE_TIME_FORMAT));
        parameter.put("endTime", DateUtils.format(endTime,DateUtils.DATE_TIME_FORMAT));
        parameter.put("userId",userId);
        Map<String, String> sqlMap = Maps.newHashMap();
        sqlMap.put("dsf", "");
        if(StringUtils.isNotBlank(userId) ){
            sqlMap.put("dsf", super.dataScopeFilter(UserUtils.getUser(userId), "o", "u"));//数据权限控制
        }
        parameter.put("sqlMap", sqlMap);

        if (null != params) {
            params.forEach(parameter::putIfAbsent);
        }
        page.setResult(dao.findQueryList(parameter));
        return page;
    }


    /**
     * 删除
     *
     * @param entity
     * @param isRe   是否恢复删除
     */
    public void delete(Message entity, Boolean isRe) {
        if (isRe != null && isRe) {
            entity.setStatus(Message.STATUS_NORMAL);
            super.save(entity);
        } else {
            super.delete(entity);
        }
    }

    /**
     * 保存并发送 切面实现消息推送
     *
     * @param message
     * @param messageReceiveObjectType {@link MessageReceiveObjectType}
     * @param receiveObjectIds
     */
    public Message saveAndSend(Message message, MessageReceiveObjectType messageReceiveObjectType, Collection<String> receiveObjectIds) {
        if (Collections3.isEmpty(receiveObjectIds)) {
            throw new SystemException("未定义参数[receiveObjectIds]");
        }

        message.setBizMode(MessageMode.Publishing.getValue());
        if(null == message.getSendTime()){
            message.setSendTime(Calendar.getInstance().getTime());
        }
        this.save(message);
        List<MessageReceive> messageReceives = Lists.newArrayList();
        for (String objectId : receiveObjectIds) {
            MessageSender messageSender = new MessageSender(message.getId());
            messageSender.setObjectType(messageReceiveObjectType.getValue());
            messageSender.setObjectId(objectId);
            messageSenderService.save(messageSender);

            List<String> targetIds = Lists.newArrayList();
            if (MessageReceiveObjectType.User.equals(messageReceiveObjectType)) {
                targetIds.add(objectId);
            } else if (MessageReceiveObjectType.Organ.equals(messageReceiveObjectType)) {
//                targetIds = userService.findUsersLoginNamesByOrganId(objectId);
                targetIds = userService.findUserIdsByOrganId(objectId);
            } else if (MessageReceiveObjectType.Member.equals(messageReceiveObjectType)) {
                targetIds.add(objectId);

            }
            for (String targetId : targetIds) {
                MessageReceive messageReceive = new MessageReceive(message.getId());
                messageReceive.setUserId(targetId);
                messageReceive.setIsRead(YesOrNo.NO.getValue());
                messageReceive.prePersist();
//                messageReceiveService.save(messageReceive);
                messageReceives.add(messageReceive);
            }
        }
        message.setMessageReceives(messageReceives);
        messageReceiveService.insertAutoBatch(messageReceives);
        message.setBizMode(MessageMode.Published.getValue());
        this.save(message);
        return message;
    }


    /**
     * 推送（仅限推送,由切面实现）
     *
     * @param messageId
     */
    public Message push(String messageId) {
        return get(messageId);
    }
}
