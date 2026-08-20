/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.vo;

import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.core.security.xss.XssIgnore;
import com.eryansky.modules.notice._enum.NoticeReadMode;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 通知接收信息
 *
 * @author Eryan
 * @date 2026-08-15
 */
public class NoticeReceiveInfoSimpleVo extends BaseEntity<NoticeReceiveInfoSimpleVo> {

    /**
     * 是否已读 默认值：否 {@link NoticeReadMode}
     */
    private String isRead;
    /**
     * 是否已读 默认值：否 {@link NoticeReadMode}
     */
    private String isReadView;
    private String isReply;
    private String isReplyView;

    private String appId;

    /**
     * 用户
     */
    private String userId;

    private String noticeId;
    private String type;
    private String typeView;
    @XssIgnore
    private String title;
    private String headImage;
    private String headImageUrl;

    /**
     * 是否置顶 ${@link YesOrNo}
     */
    private String isTop;


    private String publishUserId;
    private String publishUserName;
    private String isNeedReply;
    private String isNeedReplyView;


    private Date publishTime;

    private String userName;
    private String organName;
    private String companyName;

    public NoticeReceiveInfoSimpleVo() {
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public String getIsReadView() {
        return isReadView;
    }

    public void setIsReadView(String isReadView) {
        this.isReadView = isReadView;
    }

    public String getIsReply() {
        return isReply;
    }

    public void setIsReply(String isReply) {
        this.isReply = isReply;
    }

    public String getIsReplyView() {
        return isReplyView;
    }

    public void setIsReplyView(String isReplyView) {
        this.isReplyView = isReplyView;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeView() {
        return typeView;
    }

    public void setTypeView(String typeView) {
        this.typeView = typeView;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public String getHeadImageUrl() {
        return headImageUrl;
    }

    public void setHeadImageUrl(String headImageUrl) {
        this.headImageUrl = headImageUrl;
    }

    public String getIsTop() {
        return isTop;
    }

    public void setIsTop(String isTop) {
        this.isTop = isTop;
    }

    public String getPublishUserId() {
        return publishUserId;
    }

    public void setPublishUserId(String publishUserId) {
        this.publishUserId = publishUserId;
    }

    public String getPublishUserName() {
        return publishUserName;
    }

    public void setPublishUserName(String publishUserName) {
        this.publishUserName = publishUserName;
    }

    public String getIsNeedReply() {
        return isNeedReply;
    }

    public void setIsNeedReply(String isNeedReply) {
        this.isNeedReply = isNeedReply;
    }

    public String getIsNeedReplyView() {
        return isNeedReplyView;
    }

    public void setIsNeedReplyView(String isNeedReplyView) {
        this.isNeedReplyView = isNeedReplyView;
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
}
