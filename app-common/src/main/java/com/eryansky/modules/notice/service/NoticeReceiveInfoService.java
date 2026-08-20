/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.service;

import com.eryansky.common.exception.DaoException;
import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.notice._enum.NoticeMode;
import com.eryansky.modules.notice._enum.NoticeReadMode;
import com.eryansky.modules.notice.dao.NoticeReceiveInfoDao;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.notice.utils.NoticeUtils;
import com.eryansky.modules.notice.vo.NoticeQueryVo;
import com.eryansky.modules.notice.vo.NoticeReceiveInfoSimpleVo;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.utils.DictionaryUtils;
import com.eryansky.utils.AppConstants;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * @author Eryan
 * @date 2015-10-15
 */
@Service
public class NoticeReceiveInfoService extends CrudService<NoticeReceiveInfoDao, NoticeReceiveInfo> {

    public NoticeReceiveInfo getUserNotice(String userId, String noticeId) {
        NoticeReceiveInfo receiveInfo = new NoticeReceiveInfo();
        receiveInfo.setUserId(userId);
        receiveInfo.setNoticeId(noticeId);
        return dao.getUserNotice(receiveInfo);
    }

    /**
     * 我的通知 分页查询.
     *
     * @param page
     * @param userId        用户ID
     * @param noticeQueryVo 查询条件
     * @return
     * @throws SystemException
     * @throws ServiceException
     * @throws DaoException
     */
    public Page<NoticeReceiveInfoSimpleVo> findNoticePageByUserId(Page<NoticeReceiveInfoSimpleVo> page, String userId, NoticeQueryVo noticeQueryVo) {
        Assert.notNull(userId, "参数[userId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("bizMode", NoticeMode.Effective.getValue());
        parameter.put("userId", userId);
        if (null != noticeQueryVo) {
            parameter.put("isTop", noticeQueryVo.getIsTop());
            parameter.put("type", noticeQueryVo.getType());
            parameter.put("isRead", noticeQueryVo.getIsRead());

            if (noticeQueryVo.getStartTime() != null) {
                parameter.put("startTime", DateUtils.format(noticeQueryVo.getStartTime(), DateUtils.DATE_TIME_FORMAT));
            }
            if (noticeQueryVo.getEndTime() != null) {
                parameter.put("endTime", DateUtils.format(noticeQueryVo.getEndTime(), DateUtils.DATE_TIME_FORMAT));
            }
        }
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        page.autoResult(dao.findQueryListByUserId(parameter));
        page.getResult().forEach(noticeReceiveInfo -> {
            noticeReceiveInfo.setHeadImageUrl(DiskUtils.getFileUrl(noticeReceiveInfo.getHeadImage()));
            noticeReceiveInfo.setTypeView(DictionaryUtils.getDictionaryNameByDV(NoticeUtils.DIC_NOTICE, noticeReceiveInfo.getType(), noticeReceiveInfo.getType()));
            noticeReceiveInfo.setIsReadView(GenericEnumUtils.getDescriptionByValue(NoticeReadMode.class, noticeReceiveInfo.getIsRead(), noticeReceiveInfo.getIsRead()));
            noticeReceiveInfo.setIsReplyView(GenericEnumUtils.getDescriptionByValue(YesOrNo.class, noticeReceiveInfo.getIsReply(), noticeReceiveInfo.getIsReply()));
            noticeReceiveInfo.setIsNeedReplyView(GenericEnumUtils.getDescriptionByValue(YesOrNo.class, noticeReceiveInfo.getIsNeedReply(), noticeReceiveInfo.getIsNeedReply()));
        });
        return page;
    }

    public Page<NoticeReceiveInfo> findUserUnreadNotices(Page<NoticeReceiveInfo> page, String userId) {
        NoticeReceiveInfo noticeReceiveInfo = new NoticeReceiveInfo();
        noticeReceiveInfo.setUserId(userId);
        noticeReceiveInfo.setIsRead(NoticeReadMode.unreaded.getValue());
        Notice notice = new Notice();
        notice.setBizMode(NoticeMode.Effective.getValue());
        noticeReceiveInfo.setNotice(notice);
        noticeReceiveInfo.setEntityPage(page);
        page.autoResult(dao.findUserUnreadNotices(noticeReceiveInfo));
        return page;
    }

    /**
     * 设置用户通知为已读状态
     *
     * @param userId 用户ID
     * @return
     */
    public int markUserNoticeReaded(String userId) {
        return updateUserNotices(userId, null, NoticeReadMode.readed.getValue());
    }

    /**
     * 设置用户通知为已读状态
     *
     * @param userId    用户ID
     * @param noticeIds 通知IDS
     * @return
     */
    public int markUserNoticeReaded(String userId, Collection<String> noticeIds) {
        return updateUserNotices(userId, noticeIds, NoticeReadMode.readed.getValue());
    }

    /**
     * 设置用户通知为已读状态
     *
     * @param userId    用户ID
     * @param noticeIds 通知IDS
     * @param isRead    是否读
     * @return
     */
    public int updateUserNotices(String userId, Collection<String> noticeIds, String isRead) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("userId", userId);
        parameter.put("noticeIds", noticeIds);
        parameter.put("isRead", isRead);
        if (YesOrNo.YES.getValue().equals(isRead)) {
            parameter.put("readTime", Calendar.getInstance().getTime());
        }
        return dao.updateUserNotices(parameter);
    }

    /**
     * 设置用户通知为已读状态
     *
     * @param id 接收ID
     * @return
     */
    public int updateReadById(String id) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("isRead", YesOrNo.YES.getValue());
        parameter.put("readTime", Calendar.getInstance().getTime());
        return dao.updateReadById(parameter);
    }

    /**
     * 根据通知ID查看
     *
     * @param page
     * @param noticeId
     * @return
     */
    public Page<NoticeReceiveInfoSimpleVo> findNoticeReceiveInfosByNoticeId(Page<NoticeReceiveInfoSimpleVo> page, String noticeId) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("noticeId", noticeId);
        List<NoticeReceiveInfoSimpleVo> list = dao.findQueryListByNoticeId(parameter);
        page.autoResult(list);
        return page;
    }

    /**
     * 根据通知ID删除
     *
     * @param noticeId
     * @return
     */
    public int deleteByNoticeId(String noticeId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("noticeId", noticeId);
        return dao.deleteByNoticeId(parameter);
    }
}
