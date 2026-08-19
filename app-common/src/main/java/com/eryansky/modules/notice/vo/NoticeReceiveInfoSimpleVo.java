/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.vo;

import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.modules.notice._enum.NoticeReadMode;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.sys._enum.YesOrNo;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知接收信息
 *
 * @author Eryan
 * @date 2026-08-15
 */
public class NoticeReceiveInfoSimpleVo extends BaseEntity<NoticeReceiveInfoSimpleVo> {

    private String appId;

    /**
     * 用户
     */
    private String userId;

    private String noticeId;
    private String type;
    private String typeView;
    private String headImage;
    private String headImageUrl;

    /**
     * 是否置顶 ${@link YesOrNo}
     */
    private String isTop;
    /**
     * 是否已读 默认值：否 {@link NoticeReadMode}
     */
    private String isRead;
    /**
     * 是否已读 默认值：否 {@link NoticeReadMode}
     */
    private String isReadView;

    private Date publishTime;

    public NoticeReceiveInfoSimpleVo() {
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

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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


    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }
}
