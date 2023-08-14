/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.service;

import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.notice._enum.*;
import com.eryansky.modules.notice.mapper.NoticeSendInfo;
import com.eryansky.modules.notice.utils.NoticeConstants;
import com.eryansky.modules.notice.utils.NoticeUtils;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.google.common.collect.Lists;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.modules.notice.dao.NoticeDao;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.notice.vo.NoticeQueryVo;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知管理
 */
@Service
public class NoticeService extends CrudService<NoticeDao, Notice> {

    @Autowired
    private NoticeSendInfoService noticeSendInfoService;
    @Autowired
    private NoticeReceiveInfoService noticeReceiveInfoService;
    @Autowired
    private UserService userService;
    @Autowired
    private ContactGroupService contactGroupService;

    /**
     * 保存通知和文件
     *
     * @param entity
     * @param isPub
     * @param userIds
     * @param organIds
     * @param fileIds
     */
    public Notice saveNoticeAndFiles(Notice entity, Boolean isPub, Collection<String> userIds, Collection<String> organIds, Collection<String> contactGroupIds, List<String> fileIds) {
        List<String> oldFileIds = Collections.emptyList();
        if (!entity.getIsNewRecord()) {
            oldFileIds = findFileIdsByNoticeId(entity.getId());
        }
        super.save(entity);
        saveNoticeFiles(entity.getId(), fileIds);

        List<String> removeFileIds = Collections3.subtract(oldFileIds, fileIds);
        if (Collections3.isNotEmpty(removeFileIds)) {
            deleteNoticeFiles(entity.getId(), removeFileIds);
            DiskUtils.deleteFolderFiles(removeFileIds);
        }

        //历史数据
//        noticeReceiveInfoService.deleteByNoticeId(entity.getId());
        noticeSendInfoService.deleteByNoticeId(entity.getId());

        saveNoticeSendInfos(userIds, entity.getId(), ReceiveObjectType.User.getValue());
        saveNoticeSendInfos(organIds, entity.getId(), ReceiveObjectType.Organ.getValue());
        saveNoticeSendInfos(contactGroupIds, entity.getId(), ReceiveObjectType.ContactGroup.getValue());

        if (isPub != null && isPub) {
            publish(entity);
        }
        return entity;
    }

    private void saveNoticeSendInfos(Collection<String> ids, String noticeId, String receieveObjectType) {
        if (Collections3.isNotEmpty(ids)) {
            for (String id : ids) {
                if(StringUtils.isBlank(id)){
                    continue;
                }
                NoticeSendInfo noticeSendInfo = new NoticeSendInfo();
                noticeSendInfo.setReceiveObjectType(receieveObjectType);
                noticeSendInfo.setNoticeId(noticeId);
                noticeSendInfo.setReceiveObjectId(id);
                noticeSendInfoService.save(noticeSendInfo);
            }
        }

    }

    /**
     * 删除通知
     *
     * @param ids
     */
    public void deleteByIds(List<String> ids) {
        if (Collections3.isNotEmpty(ids)) {
            for (String id : ids) {
                this.delete(new Notice(id));
            }
        }
    }

    /**
     * 属性过滤器查找得到分页数据.
     *
     * @param page          分页对象
     * @param userId        发布人 查询所有则传null
     * @param noticeQueryVo 标查询条件
     * @return
     */
    public Page<Notice> findPage(Page<Notice> page, Notice notice, String userId, NoticeQueryVo noticeQueryVo) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);

        if (noticeQueryVo != null && Collections3.isNotEmpty(noticeQueryVo.getPublishUserIds())) {
            parameter.put("userId", noticeQueryVo.getPublishUserIds().get(0));
        } else {
            parameter.put("userId", userId);
        }

        if (noticeQueryVo != null) {
            parameter.put("isTop", noticeQueryVo.getIsTop());
            parameter.put("query", noticeQueryVo.getQuery());

            if (noticeQueryVo.getStartTime() != null) {
                parameter.put("startTime", DateUtils.format(noticeQueryVo.getStartTime(), DateUtils.DATE_TIME_FORMAT));
            }
            if (noticeQueryVo.getEndTime() != null) {
                parameter.put("endTime", DateUtils.format(noticeQueryVo.getEndTime(), DateUtils.DATE_TIME_FORMAT));
            }
        }

        notice.setEntityPage(page);
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("dbName", notice.getDbName());
        Map<String, String> sqlMap = Maps.newHashMap();
        sqlMap.put("dsf", super.dataScopeFilter(SecurityUtils.getCurrentUser(), "o", "u"));
        parameter.put("sqlMap", sqlMap);
        page.setResult(dao.findQueryList(parameter));

        return page;

    }


    /**
     * 发布公告 切面实现消息推送
     *
     * @param noticeId 公告ID
     */
    public Notice publish(String noticeId) {
        Notice notice = this.get(noticeId);
        if (notice == null) {
            throw new ServiceException("公告[" + noticeId + "]不存在.");
        }
        return publish(notice);
    }

    /**
     * 发布公告 切面实现消息推送
     *
     * @param notice 通知
     */
    public Notice publish(Notice notice) {
        notice.setBizMode(NoticeMode.Effective.getValue());
        if (notice.getPublishTime() == null) {
            Date nowTime = Calendar.getInstance().getTime();
            notice.setPublishTime(nowTime);
        }
        this.save(notice);
        List<NoticeReceiveInfo> receiveInfos = Lists.newArrayList();
        List<String> receiveUserIds = Lists.newArrayList();

        if (NoticeReceiveScope.CUSTOM.getValue().equals(notice.getReceiveScope())) {
            List<String> _receiveUserIds = NoticeUtils.findNoticeReceiveUserIds(notice.getId());
            List<String> receiveOrganIds = NoticeUtils.findNoticeReceiveOrganIds(notice.getId());
            List<String> userIds = userService.findUserIdsByOrganIds(receiveOrganIds);
            List<String> receiveContactGroupIds = NoticeUtils.findNoticeReceivContactGroupIds(notice.getId());
            if(Collections3.isNotEmpty(receiveContactGroupIds)){
                List<String> finalReceiveUserIds = receiveUserIds;
                receiveContactGroupIds.forEach(v-> finalReceiveUserIds.addAll(contactGroupService.findContactGroupUsers(v).stream().map(BaseEntity::getId).collect(Collectors.toList())));
            }
            if (Collections3.isNotEmpty(_receiveUserIds)) {
                receiveUserIds.addAll(_receiveUserIds);
            }
            if (Collections3.isNotEmpty(userIds)) {
                receiveUserIds.addAll(userIds);
            }
        } else if (NoticeReceiveScope.ALL.getValue().equals(notice.getReceiveScope())) {
            receiveUserIds = userService.findAllNormalUserIds();
        } else if (NoticeReceiveScope.COMPANY_AND_CHILD.getValue().equals(notice.getReceiveScope())) {
            receiveUserIds = userService.findOwnerAndChildsUserIds(UserUtils.getCompanyId(notice.getUserId()));
        } else if (NoticeReceiveScope.COMPANY.getValue().equals(notice.getReceiveScope())) {
            receiveUserIds = userService.findUserIdsByCompanyId(UserUtils.getCompanyId(notice.getUserId()));
        } else if (NoticeReceiveScope.OFFICE_AND_CHILD.getValue().equals(notice.getReceiveScope())) {
            receiveUserIds = userService.findOwnerAndChildsUserIds(UserUtils.getDefaultOrganId(notice.getUserId()));
        } else if (NoticeReceiveScope.OFFICE.getValue().equals(notice.getReceiveScope())) {
            receiveUserIds = userService.findUserIdsByOrganId(UserUtils.getDefaultOrganId(notice.getUserId()));
        }
        if (Collections3.isNotEmpty(receiveUserIds)) {
            Sets.newHashSet(receiveUserIds).forEach(v->{
                NoticeReceiveInfo receiveInfo = new NoticeReceiveInfo(v, notice.getId());
//                receiveInfo.setIsSend(YesOrNo.YES.getValue());
                if(YesOrNo.YES.getValue().equals(notice.getIsReply())){
                    receiveInfo.setIsReply(YesOrNo.NO.getValue());
                }
                checkReceiveInfoAdd(receiveInfos, receiveInfo);
            });
        }
        noticeReceiveInfoService.deleteByNoticeId(notice.getId());
//        receiveInfos.forEach(n->noticeReceiveInfoService.save(n));
        noticeReceiveInfoService.insertAutoBatch(receiveInfos);
        return notice;
    }

    /**
     * 推送（仅限推送,由切面实现）
     *
     * @param noticeId 公告ID
     */
    public Notice push(String noticeId) {
        return get(noticeId);
    }

    /**
     * 发布公告
     *
     * @param type
     * @param title
     * @param content
     * @param sendTime
     * @param userId
     * @param organId
     * @param organIds
     * @param messageChannels
     */
    public void sendToOrganNotice(String appId, String type, String title, String content, Date sendTime, String userId, String organId, List<String> organIds,List<MessageChannel> messageChannels) {
        //保存到notice表
        Notice notice = new Notice();
        notice.setId(Identities.uuid2());
        notice.setAppId(appId);
        notice.setType(type);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPublishTime(null != sendTime ? sendTime:Calendar.getInstance().getTime());
        notice.setReceiveScope(NoticeReceiveScope.CUSTOM.getValue());
        notice.setUserId(userId);
        notice.setOrganId(organId);
        if(Collections3.isEmpty(messageChannels)){
//            notice.setTipMessage(MessageChannel.Message.getValue()+","+MessageChannel.QYWeixin.getValue()+","+MessageChannel.APP.getValue());
            notice.setTipMessage(NoticeConstants.getNoticeDefaultTipChannel());
        }else{
            notice.setTipMessage(Collections3.extractToString(messageChannels,"value",","));
        }

        notice.setCreateTime(Calendar.getInstance().getTime());
        dao.insert(notice);

        if (Collections3.isNotEmpty(organIds)) {
            //去重
            for (String _organId : organIds) {
                //保存到notice send表
                NoticeSendInfo noticeSendInfo = new NoticeSendInfo();
                noticeSendInfo.setNoticeId(notice.getId());
                noticeSendInfo.setReceiveObjectType(ReceiveObjectType.Organ.getValue());
                noticeSendInfo.setReceiveObjectId(_organId);
                noticeSendInfoService.save(noticeSendInfo);
            }
            //发布
            publish(notice);
        }

    }


    /**
     * 去除重复
     *
     * @param receiveInfos
     * @param receiveInfo
     */
    private void checkReceiveInfoAdd(List<NoticeReceiveInfo> receiveInfos, NoticeReceiveInfo receiveInfo) {
        boolean flag = false;
        for (NoticeReceiveInfo r : receiveInfos) {
            if (r.getUserId().equals(receiveInfo.getUserId())) {
                flag = true;
                break;
            }

        }
        if (!flag) {
            receiveInfo.prePersist();
            receiveInfos.add(receiveInfo);
        }

    }


    /**
     * 标记为已读
     *
     * @param userId    所属用户ID
     * @param noticeIds 通知ID集合
     */
    public void markReaded(String userId, List<String> noticeIds) {
        if (Collections3.isNotEmpty(noticeIds)) {
            for (String id : noticeIds) {
                NoticeReceiveInfo noticeReceiveInfo = noticeReceiveInfoService.getUserNotice(userId, id);
                noticeReceiveInfo.setIsRead(NoticeReadMode.readed.getValue());
                noticeReceiveInfoService.save(noticeReceiveInfo);
            }
        } else {
            logger.warn("参数[entitys]为空.");
        }

    }

    /**
     * 插入通知附件关联信息
     *
     * @param id  通知ID
     * @param ids 文件IDS
     */
    public void insertNoticeFiles(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertNoticeFiles(parameter);
        }
    }

    /**
     * 刪除通知附件关联信息
     *
     * @param id  通知ID
     * @param ids 文件IDS
     */
    public void deleteNoticeFiles(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        dao.deleteNoticeFiles(parameter);
    }


    /**
     * 保存通知附件关联信息
     * 保存之前先删除原有
     *
     * @param id  通知ID
     * @param ids 文件IDS
     */
    public void saveNoticeFiles(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        dao.deleteNoticeFiles(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertNoticeFiles(parameter);
        }
    }


    /**
     * 查找通知附件ID
     *
     * @param noticeId
     * @return
     */
    public List<String> findFileIdsByNoticeId(String noticeId) {
        return dao.findFileIdsByNoticeId(noticeId);
    }


    /**
     * 轮询通知 定时发布、到时失效、取消置顶
     */
    public void pollNotice() {
        // 查询到今天为止所有未删除的通知
        Date nowTime = Calendar.getInstance().getTime();
        Notice notice = new Notice();
        notice.setStatus(StatusState.NORMAL.getValue());
        notice.setBizMode(NoticeMode.Invalidated.getValue());
        List<Notice> noticeList = dao.findList(notice);
        if (Collections3.isNotEmpty(noticeList)) {
            for (Notice n : noticeList) {
                if (NoticeMode.UnPublish.getValue().equals(n.getBizMode())
                        && n.getEffectTime() != null
                        && nowTime.compareTo(n.getEffectTime()) != -1) {//定时发布
                    this.publish(n);
                } else if (NoticeMode.Effective.getValue().equals(n.getBizMode())
                        && n.getInvalidTime() != null
                        && nowTime.compareTo(n.getInvalidTime()) != -1) {//到时失效
                    n.setBizMode(NoticeMode.Invalidated.getValue());
                    this.save(n);
                }
                //取消置顶
                if (IsTop.Yes.getValue().equals(n.getIsTop())
                        && n.getEndTopDay() != null && n.getEndTopDay() > 0) {
                    Date publishTime = (n.getPublishTime() == null) ? nowTime : n.getPublishTime();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(publishTime);
                    cal.add(Calendar.DATE, n.getEndTopDay());
                    if (nowTime.compareTo(cal.getTime()) != -1) {
                        n.setIsTop(IsTop.No.getValue());
                        this.save(n);
                    }
                }
            }
        }
    }
}
