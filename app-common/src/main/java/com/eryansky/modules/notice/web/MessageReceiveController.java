/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.annotation.MobileValue;
import com.eryansky.modules.notice.mapper.Message;
import com.eryansky.modules.notice.mapper.MessageReceive;
import com.eryansky.modules.notice.service.MessageReceiveService;
import com.eryansky.modules.notice.task.MessageTask;
import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.YesOrNo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 消息接收 个人消息中心
 *
 * @author Eryan
 * @date 2016-04-14
 */
@Controller
@RequestMapping(value = "${adminPath}/notice/messageReceive")
public class MessageReceiveController extends SimpleController {

    @Autowired
    private MessageReceiveService messageReceiveService;
    @Autowired
    private MessageTask messageTask;

    @ModelAttribute("model")
    public MessageReceive get(@RequestParam(required = false) String id,String messageId) {
        if (StringUtils.isNotBlank(id)) {
            return messageReceiveService.get(id);
        } else if (StringUtils.isNotBlank(messageId)) {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            return messageReceiveService.getUserMessageReceiveByMessageId(sessionInfo.getUserId(),messageId);
        } else {
            return new MessageReceive();
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
    @Logging(logType = LogType.access, value = "消息中心", logging = "!#isAjax")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"", "list"})
    public String list(MessageReceive model, HttpServletRequest request, HttpServletResponse response, Model uiModel) {
        Page<MessageReceive> page = new Page<>(request, response);
        if (WebUtils.isAjaxRequest(request)) {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            page = MessageUtils.findUserMessages(sessionInfo.getUserId(),page.getPageNo(), page.getPageSize(),null, model.getIsRead(),null);
            return renderString(response, Result.successResult().setObj(page));
        }

        uiModel.addAttribute("page", page);
        uiModel.addAttribute("model", model);
        return "modules/notice/messageReceiveList";
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
    public ModelAndView info(@ModelAttribute("model")  MessageReceive model, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/messageReceiveInfo");
        messageReceiveService.setRead(model);
//        messageTask.setRead(model);
        modelAndView.addObject("model", model);
        return modelAndView;
    }

    /**
     * 设置已读(异步)
     *
     * @param model
     * @return
     */
    @PostMapping(value = "setRead")
    @ResponseBody
    public Result setRead(@ModelAttribute("model")  MessageReceive model) {
        messageTask.setRead(model);
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
        messageReceiveService.setReadAll(appId,sessionInfo.getUserId(), YesOrNo.YES.getValue());
        return Result.successResult();
    }

    /**
     * 明细信息
     * @param model
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") MessageReceive model) {
        return Result.successResult().setObj(model);
    }
}