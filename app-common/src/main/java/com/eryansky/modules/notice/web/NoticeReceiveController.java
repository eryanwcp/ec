/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.annotation.MobileValue;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.notice.mapper.MessageReceive;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.notice.service.MessageReceiveService;
import com.eryansky.modules.notice.service.NoticeReceiveInfoService;
import com.eryansky.modules.notice.task.MessageTask;
import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.notice.vo.NoticeQueryVo;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.google.common.collect.Lists;
import org.apache.commons.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 消息接收 个人消息中心
 *
 * @author Eryan
 * @date 2016-04-14
 */
@Controller
@RequestMapping(value = "${adminPath}/notice/noticeReceiveInfo")
public class NoticeReceiveController extends SimpleController {

    @Autowired
    private NoticeReceiveInfoService noticeReceiveInfoService;

    @ModelAttribute("model")
    public NoticeReceiveInfo get(@RequestParam(required = false) String id, String noticeId) {
        if (StringUtils.isNotBlank(id)) {
            return noticeReceiveInfoService.get(id);
        } else if (StringUtils.isNotBlank(noticeId)) {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            return noticeReceiveInfoService.getUserNotice(sessionInfo.getUserId(), noticeId);
        } else {
            return new NoticeReceiveInfo();
        }
    }

    /**
     * 个人消息列表
     *
     * @param model
     * @param request
     * @param response
     * @return
     */
    @Mobile(value = MobileValue.ALL)
    @Logging(logType = LogType.access, value = "消息中心")
    @GetMapping(value = {"", "list"})
    public String list(NoticeReceiveInfo model, HttpServletRequest request, HttpServletResponse response, Model uiModel) {
        Page<NoticeReceiveInfo> page = new Page<>(request, response);
        if (WebUtils.isAjaxRequest(request)) {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            return renderString(response, Result.successResult().setObj(page));
        }

        uiModel.addAttribute("page", page);
        uiModel.addAttribute("model", model);
        return "modules/notice/noticeReceiveList";
    }


    /**
     * 我的通知
     *
     * @param noticeQueryVo 查询条件
     * @return
     */
    @PostMapping(value = {"readInfoDatagrid"})
    @ResponseBody
    public String noticeReadDatagrid(NoticeQueryVo noticeQueryVo) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Page<NoticeReceiveInfo> page = new Page<>(SpringMVCHolder.getRequest());
        noticeQueryVo.syncEndTime();
        page = noticeReceiveInfoService.findReadNoticePage(page, new NoticeReceiveInfo(), sessionInfo.getUserId(), noticeQueryVo);
        Datagrid<NoticeReceiveInfo> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        String json = JsonMapper.getInstance().toJson(dg, Notice.class,
                new String[]{"id", "noticeId", "title", "type", "typeView", "publishUserName", "publishTime", "isReadView", "isNeedReply", "isNeedReplyView", "isReply", "isReplyView"});
        return json;
    }

    /**
     * 通知阅读情况
     *
     * @param id 通知ID
     * @return
     */
    @PostMapping(value = {"readInfoDatagrid/{id}"})
    @ResponseBody
    public String readInfoDatagrid(@PathVariable String id) {
        Page<NoticeReceiveInfo> page = new Page<>(SpringMVCHolder.getRequest());
        page = noticeReceiveInfoService.findNoticeReceiveInfosByNoticeId(page, id);
        Datagrid<NoticeReceiveInfo> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        String json = JsonMapper.getInstance().toJson(dg, NoticeReceiveInfo.class,
                new String[]{"id", "userName", "organName", "isRead", "isReadView", "readTime", "isReply", "isReplyView", "replyTime", "replyContent", "replyFileIds"});
        return json;
    }


    /**
     * 个人消息详情
     *
     * @param model
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "info")
    public ModelAndView info(@ModelAttribute("model") NoticeReceiveInfo model, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/noticeReceiveInfo");
        noticeReceiveInfoService.updateReadById(model.getId());
        modelAndView.addObject("model", model);
        return modelAndView;
    }

    /**
     * 文件上传
     */
    @PostMapping(value = {"upload"})
    @ResponseBody
    public Result upload(@RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile, String jsessionid) {
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getSessionInfo(jsessionid);
        sessionInfo = null != sessionInfo ? sessionInfo : SecurityUtils.getCurrentSessionInfo();
        Exception exception = null;
        File file = null;
        try {
            file = DiskUtils.saveSystemFile(NoticeReceiveInfo.FOLDER_NOTICE_RECEIVE, sessionInfo.getUserId(), multipartFile);
            result = Result.successResult().setObj(file).setMsg("文件上传成功！");
        } catch (InvalidExtensionException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG);
        } catch (FileNameLengthLimitExceededException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG);
        } catch (ActionException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } catch (IOException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } finally {
            if (exception != null) {
                logger.error(exception.getMessage(), exception);
                if (file != null) {
                    DiskUtils.deleteFile(file.getId());
                }
            }
        }
        return result;

    }

    /**
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"replyInput"})
    public ModelAndView replyInput(@ModelAttribute("model") NoticeReceiveInfo model) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/notice-reply-input");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String[] fs = StringUtils.split(model.getReplyFileIds(), ",");
        modelAndView.addObject("files", null == fs ? Collections.emptyList() : DiskUtils.findFilesByIds(Lists.newArrayList(fs)));
        modelAndView.addObject("fileIds", null == fs ? Collections.emptyList() : Lists.newArrayList(fs));
        modelAndView.addObject("model", model);
        return modelAndView;
    }


    /**
     * 保存
     *
     * @param model
     * @param fileIds 页面文件ID集合
     * @return
     */
    @PostMapping(value = {"replySave"})
    @ResponseBody
    public Result replySave(@ModelAttribute("model") NoticeReceiveInfo model,
                            @RequestParam(value = "fileIds", required = false) List<String> fileIds) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        model.setIsReply(YesOrNo.YES.getValue());
        model.setReplyTime(Calendar.getInstance().getTime());
        model.setReplyFileIds(Collections3.convertToString(fileIds, ","));
        noticeReceiveInfoService.save(model);
        return Result.successResult();
    }

    /**
     * 设置已读(异步)
     *
     * @param model
     * @return
     */
    @PostMapping(value = "setRead")
    @ResponseBody
    public Result setRead(@ModelAttribute("model") NoticeReceiveInfo model) {
        noticeReceiveInfoService.updateReadById(model.getId());
        return Result.successResult();
    }

    /**
     * 设置全部已读
     *
     * @return
     */
    @PostMapping(value = "setReadAll")
    @ResponseBody
    public Result setReadAll(String appId) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        noticeReceiveInfoService.markUserNoticeReaded(sessionInfo.getUserId());
        return Result.successResult();
    }

    /**
     * 明细信息
     *
     * @param model
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") NoticeReceiveInfo model) {
        return Result.successResult().setObj(model);
    }
}