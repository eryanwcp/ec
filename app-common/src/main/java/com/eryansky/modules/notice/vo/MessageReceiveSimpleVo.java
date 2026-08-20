/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.vo;

import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息接收表
 *
 * @author Eryan
 * @date 2016-03-14
 */
public class MessageReceiveSimpleVo extends BaseEntity<MessageReceiveSimpleVo> {
    private String appId;
    /**
     * 消息ID
     */
    private String messageId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 是否发送成功 ${@link YesOrNo}
     */
    private String isSend;
    /**
     * 是否读取 ${@link YesOrNo}
     */
    private String isRead;
    /**
     * 读取时间
     */
    private Date readTime;

    private String senderName;
    private String organName;
    private String companyName;
    private String content;
    private String sendTime;
    //TODO 后续删除，兼容性对象 content、sendTime
    private Message message;

    public MessageReceiveSimpleVo() {
        this.isSend = YesOrNo.YES.getValue();
    }

    public MessageReceiveSimpleVo(String messageId) {
        this();
        this.messageId = messageId;
    }

    public MessageReceiveSimpleVo(String id, String messageId) {
        super(id);
        this.messageId = messageId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIsSend() {
        return isSend;
    }

    public void setIsSend(String isSend) {
        this.isSend = isSend;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public String getIsReadView() {
        return YesOrNo.YES.getValue().equals(isRead) ? "已阅" : "未阅";
    }

    public String getIsSendView() {
        return YesOrNo.YES.getValue().equals(isSend) ? "成功" : "失败";
    }


    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getOrganName() {
        return organName;
    }

    public void setOrganName(String organName) {
        this.organName = organName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public static class Message implements Serializable {
        private String content;
        private String sendTime;

        public Message() {
        }

        public Message(String content, String sendTime) {
            this.content = content;
            this.sendTime = sendTime;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSendTime() {
            return sendTime;
        }

        public void setSendTime(String sendTime) {
            this.sendTime = sendTime;
        }
    }
}
