/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
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
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.notice._enum.*;
import com.eryansky.modules.notice.dao.NoticeDao;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.notice.mapper.NoticeSendInfo;
import com.eryansky.modules.notice.utils.NoticeConstants;
import com.eryansky.modules.notice.utils.NoticeUtils;
import com.eryansky.modules.notice.vo.NoticeQueryVo;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知管理
 */
@Service
public class NoticeService extends CrudService<NoticeDao, Notice> {

    @Resource
    private NoticeSendInfoService noticeSendInfoService;
    @Resource
    private NoticeReceiveInfoService noticeReceiveInfoService;
    @Resource
    private UserService userService;
    @Resource
    private ContactGroupService contactGroupService;

    /**
     * 保存通知和文件
     *
     * @param entity           通知实体
     * @param isPub            是否立即发布
     * @param userIds          用户ID列表
     * @param organIds         机构ID列表
     * @param contactGroupIds  联系人组ID列表
     * @param fileIds          附件ID列表
     * @return Notice
     */
    public Notice saveNoticeAndFiles(Notice entity, Boolean isPub, Collection<String> userIds,
                                     Collection<String> organIds, Collection<String> contactGroupIds,
                                     List<String> fileIds) {
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

        // 清理并重新插入发送目标关联表
        noticeSendInfoService.deleteByNoticeId(entity.getId());
        saveNoticeSendInfos(userIds, entity.getId(), ReceiveObjectType.User.getValue());
        saveNoticeSendInfos(organIds, entity.getId(), ReceiveObjectType.Organ.getValue());
        saveNoticeSendInfos(contactGroupIds, entity.getId(), ReceiveObjectType.ContactGroup.getValue());

        if (Boolean.TRUE.equals(isPub)) {
            publish(entity);
        }
        return entity;
    }

    private void saveNoticeSendInfos(Collection<String> ids, String noticeId, String receiveObjectType) {
        if (Collections3.isEmpty(ids)) {
            return;
        }
        // 过滤空串与去重后统一保存
        Set<String> uniqueIds = ids.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        for (String id : uniqueIds) {
            NoticeSendInfo noticeSendInfo = new NoticeSendInfo();
            noticeSendInfo.setReceiveObjectType(receiveObjectType);
            noticeSendInfo.setNoticeId(noticeId);
            noticeSendInfo.setReceiveObjectId(id);
            noticeSendInfoService.save(noticeSendInfo);
        }
    }

    /**
     * 根据ID批量删除通知
     *
     * @param ids 通知ID集合
     */
    public void deleteByIds(List<String> ids) {
        if (Collections3.isNotEmpty(ids)) {
            for (String id : ids) {
                this.delete(new Notice(id));
            }
        }
    }

    /**
     * 属性过滤器查找得到分页数据
     *
     * @param page          分页对象
     * @param notice        通知实体
     * @param userId        发布人ID，查询所有则传 null
     * @param noticeQueryVo 查询条件扩展VO
     * @return Page<Notice>
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

        page.autoResult(dao.findQueryList(parameter));
        return page;
    }

    /**
     * 发布公告
     *
     * @param noticeId 公告ID
     * @return Notice
     */
    public Notice publish(String noticeId) {
        Notice notice = this.get(noticeId);
        if (notice == null) {
            throw new ServiceException("公告[" + noticeId + "]不存在.");
        }
        return publish(notice);
    }

    /**
     * 发布公告 (包含接收人员去重计算与批量发件明细生成)
     *
     * @param notice 通知实体
     * @return Notice
     */
    public Notice publish(Notice notice) {
        notice.setBizMode(NoticeMode.Effective.getValue());
        if (notice.getPublishTime() == null) {
            notice.setPublishTime(new Date());
        }
        this.save(notice);

        // 使用 Set 直接在内存中完成去重，避免重复追加与 List 转 Set
        Set<String> receiveUserIds = Sets.newHashSet();
        String receiveScope = notice.getReceiveScope();

        if (NoticeReceiveScope.CUSTOM.getValue().equals(receiveScope)) {
            List<String> _receiveUserIds = NoticeUtils.findNoticeReceiveUserIds(notice.getId());
            List<String> receiveOrganIds = NoticeUtils.findNoticeReceiveOrganIds(notice.getId());
            List<String> userIds = userService.findUserIdsByOrganIds(receiveOrganIds);
            List<String> receiveContactGroupIds = NoticeUtils.findNoticeReceivContactGroupIds(notice.getId());

            if (Collections3.isNotEmpty(_receiveUserIds)) {
                receiveUserIds.addAll(_receiveUserIds);
            }
            if (Collections3.isNotEmpty(userIds)) {
                receiveUserIds.addAll(userIds);
            }
            if (Collections3.isNotEmpty(receiveContactGroupIds)) {
                for (String groupId : receiveContactGroupIds) {
                    List<String> groupUserIds = contactGroupService.findContactGroupUsers(groupId).stream()
                            .map(BaseEntity::getId)
                            .collect(Collectors.toList());
                    receiveUserIds.addAll(groupUserIds);
                }
            }
        } else if (NoticeReceiveScope.ALL.getValue().equals(receiveScope)) {
            receiveUserIds.addAll(userService.findAllNormalUserIds());
        } else if (NoticeReceiveScope.COMPANY_AND_CHILD.getValue().equals(receiveScope)) {
            receiveUserIds.addAll(userService.findOwnerAndChildsUserIds(UserUtils.getCompanyId(notice.getUserId())));
        } else if (NoticeReceiveScope.COMPANY.getValue().equals(receiveScope)) {
            receiveUserIds.addAll(userService.findUserIdsByCompanyId(UserUtils.getCompanyId(notice.getUserId())));
        } else if (NoticeReceiveScope.OFFICE_AND_CHILD.getValue().equals(receiveScope)) {
            receiveUserIds.addAll(userService.findOwnerAndChildsUserIds(UserUtils.getDefaultOrganId(notice.getUserId())));
        } else if (NoticeReceiveScope.OFFICE.getValue().equals(receiveScope)) {
            receiveUserIds.addAll(userService.findUserIdsByOrganId(UserUtils.getDefaultOrganId(notice.getUserId())));
        }

        // 构建并批量插入接收记录
        if (Collections3.isNotEmpty(receiveUserIds)) {
            List<NoticeReceiveInfo> receiveInfos = receiveUserIds.stream().map(userId -> {
                NoticeReceiveInfo receiveInfo = new NoticeReceiveInfo(userId, notice.getId());
                if (YesOrNo.YES.getValue().equals(notice.getIsReply())) {
                    receiveInfo.setIsReply(YesOrNo.NO.getValue());
                }
                receiveInfo.prePersist();
                return receiveInfo;
            }).collect(Collectors.toList());

            noticeReceiveInfoService.deleteByNoticeId(notice.getId());
            noticeReceiveInfoService.insertAutoBatch(receiveInfos);
        }

        return notice;
    }

    /**
     * 推送（仅限推送，切面实现）
     *
     * @param noticeId 公告ID
     */
    public Notice push(String noticeId) {
        return get(noticeId);
    }

    /**
     * 按机构发送公告
     */
    public void sendToOrganNotice(String appId, String type, String title, String content,
                                  Date sendTime, String userId, String organId,
                                  Collection<String> organIds, List<MessageChannel> messageChannels) {
        Notice notice = new Notice();
        notice.setId(Identities.uuid7());
        notice.setAppId(appId);
        notice.setType(type);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPublishTime(sendTime != null ? sendTime : new Date());
        notice.setReceiveScope(NoticeReceiveScope.CUSTOM.getValue());
        notice.setUserId(userId);
        notice.setOrganId(organId);

        if (Collections3.isEmpty(messageChannels)) {
            notice.setTipMessage(NoticeConstants.getNoticeDefaultTipChannel());
        } else {
            notice.setTipMessage(Collections3.extractToString(messageChannels, "value", ","));
        }
        notice.setCreateTime(new Date());
        dao.insert(notice);

        if (Collections3.isNotEmpty(organIds)) {
            // 对传入的 organIds 进行 HashSet 去重，避免重复保存
            Set<String> uniqueOrganIds = Sets.newHashSet(organIds);
            for (String _organId : uniqueOrganIds) {
                if (StringUtils.isBlank(_organId)) {
                    continue;
                }
                NoticeSendInfo noticeSendInfo = new NoticeSendInfo();
                noticeSendInfo.setNoticeId(notice.getId());
                noticeSendInfo.setReceiveObjectType(ReceiveObjectType.Organ.getValue());
                noticeSendInfo.setReceiveObjectId(_organId);
                noticeSendInfoService.save(noticeSendInfo);
            }
            // 发布
            publish(notice);
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
                if (noticeReceiveInfo != null) {
                    noticeReceiveInfo.setIsRead(NoticeReadMode.readed.getValue());
                    noticeReceiveInfoService.save(noticeReceiveInfo);
                }
            }
        } else {
            logger.warn("参数[noticeIds]为空.");
        }
    }

    /**
     * 插入通知附件关联信息
     *
     * @param id  通知ID
     * @param ids 文件IDS
     */
    public void insertNoticeFiles(String id, Collection<String> ids) {
        if (Collections3.isNotEmpty(ids)) {
            Parameter parameter = Parameter.newParameter();
            parameter.put("id", id);
            parameter.put("ids", ids);
            dao.insertNoticeFiles(parameter);
        }
    }

    /**
     * 删除通知附件关联信息
     *
     * @param id  通知ID
     * @param ids 文件IDS
     */
    public void deleteNoticeFiles(String id, Collection<String> ids) {
        if (Collections3.isNotEmpty(ids)) {
            Parameter parameter = Parameter.newParameter();
            parameter.put("id", id);
            parameter.put("ids", ids);
            dao.deleteNoticeFiles(parameter);
        }
    }

    /**
     * 保存通知附件关联信息（先删后插）
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
     * 查找通知关联的文件ID列表
     *
     * @param noticeId 通知ID
     * @return List<String>
     */
    public List<String> findFileIdsByNoticeId(String noticeId) {
        return dao.findFileIdsByNoticeId(noticeId);
    }

    /**
     * 轮询通知：定时发布、到时失效、取消置顶
     */
    public void pollNotice() {
        Date nowTime = new Date();
        Notice notice = new Notice();
        notice.setStatus(StatusState.NORMAL.getValue());
        List<Notice> noticeList = dao.findList(notice);

        if (Collections3.isEmpty(noticeList)) {
            return;
        }

        for (Notice n : noticeList) {
            // 1. 定时发布
            if (NoticeMode.UnPublish.getValue().equals(n.getBizMode())
                    && n.getEffectTime() != null
                    && !nowTime.before(n.getEffectTime())) {
                this.publish(n);
            }
            // 2. 到时失效
            else if (NoticeMode.Effective.getValue().equals(n.getBizMode())
                    && n.getInvalidTime() != null
                    && !nowTime.before(n.getInvalidTime())) {
                n.setBizMode(NoticeMode.Invalidated.getValue());
                this.save(n);
            }

            // 3. 取消置顶
            if (IsTop.Yes.getValue().equals(n.getIsTop())
                    && n.getEndTopDay() != null
                    && n.getEndTopDay() > 0) {
                Date publishTime = (n.getPublishTime() == null) ? nowTime : n.getPublishTime();
                Calendar cal = Calendar.getInstance();
                cal.setTime(publishTime);
                cal.add(Calendar.DATE, n.getEndTopDay());

                if (!nowTime.before(cal.getTime())) {
                    n.setIsTop(IsTop.No.getValue());
                    this.save(n);
                }
            }
        }
    }
}