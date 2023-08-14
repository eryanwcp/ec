/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.mapper;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.modules.notice._enum.NoticeReadMode;
import com.eryansky.modules.notice.utils.NoticeUtils;
import com.eryansky.modules.sys.utils.UserUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 通知接收信息
 *
 * @author Eryan
 * @date 2015-10-15
 */
@JsonFilter(" ")
public class NoticeReceiveInfo extends BaseEntity<NoticeReceiveInfo> {
    public static final String FOLDER_NOTICE_RECEIVE = "noticeReceive";
    /**
     * 用户
     */
    private String userId;
    /**
     * 是否发送成功 ${@link YesOrNo}
     */
    private String isSend;
    /**
     * 是否已读 默认值：否 {@link NoticeReadMode}
     */
    private String isRead;
    /**
     * 读取时间
     */
    private Date readTime;
    /**
     * 是否回复
     */
    private String isReply;
    /**
     * 回复时间
     */
    private Date replyTime;
    /**
     * 回复内容
     */
    private String replyContent;
    /**
     * 回复附件
     */
    private String replyFileIds;

    private String noticeId;

    private Notice notice;

    public NoticeReceiveInfo() {
        super();
        this.isRead = NoticeReadMode.unreaded.getValue();
    }

    public NoticeReceiveInfo(String id) {
        super(id);
    }

    public NoticeReceiveInfo(String userId, String noticeId) {
        this.userId = userId;
        this.noticeId = noticeId;
        this.isRead = NoticeReadMode.unreaded.getValue();
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


    public String getIsReply() {
        return isReply;
    }

    public void setIsReply(String isReply) {
        this.isReply = isReply;
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(Date replyTime) {
        this.replyTime = replyTime;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public String getReplyFileIds() {
        return replyFileIds;
    }

    public void setReplyFileIds(String replyFileIds) {
        this.replyFileIds = replyFileIds;
    }

    /**
     * 接收人姓名
     *
     * @return
     */
    public String getUserName() {
        return UserUtils.getUserName(this.userId);
    }

    /**
     * 是否读取
     *
     * @return
     */
    public String getIsReadView() {
        NoticeReadMode s = NoticeReadMode.getByValue(isRead);
        String str = "";
        if (s != null) {
            str = s.getDescription();
        }
        return str;
    }


    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return getNotice().getTitle();
    }

    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    private Notice getNotice() {
        if (this.notice == null && StringUtils.isNotBlank(this.noticeId)) {
            return NoticeUtils.getNotice(this.noticeId);
        }
        return this.notice;
    }


    public String getType() {
        return getNotice().getType();
    }

    public String getTypeView() {
        return getNotice().getTypeView();
    }

    public String getContent() {
        return getNotice().getContent();
    }

    public String getPublishUserName() {
        return getNotice().getPublishUserName();
    }

    public String getPublishOrganName() {
        return getNotice().getPublishOrganName();
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getPublishTime() {
        return getNotice().getPublishTime();
    }

    public String getIsTop() {
        return getNotice().getIsTop();
    }

    public String getIsTopView() {
        return getNotice().getIsTopView();
    }

    public String getIsNeedReply() {
        return getNotice().getIsReply();
    }

    public String getIsNeedReplyView() {
        return getNotice().getIsReplyView();
    }

    public String getIsReplyView() {
        YesOrNo e = YesOrNo.getByValue(isReply);
        return null != e ? e.getDescription() : isReply;
    }

    public String getHeadImageUrl() {
        return getNotice().getHeadImageUrl();
    }

    /**
     * 接收人部门
     *
     * @return
     */
    public String getOrganName() {
        return UserUtils.getDefaultOrganName(userId);
    }

    /**
     * 接收人单位
     *
     * @return
     */
    public String getCompanyName() {
        return UserUtils.getCompanyName(userId);
    }


}
