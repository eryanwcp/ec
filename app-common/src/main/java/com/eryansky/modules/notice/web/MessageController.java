/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.web;

import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.task.MessageTask;
import com.eryansky.modules.notice.utils.NoticeConstants;
import com.google.common.collect.Lists;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.listener.SystemInitListener;
import com.eryansky.modules.notice._enum.MessageReceiveObjectType;
import com.eryansky.modules.notice._enum.MessageChannel;
import com.eryansky.modules.notice.mapper.Message;
import com.eryansky.modules.notice.mapper.MessageReceive;
import com.eryansky.modules.notice.service.MessageReceiveService;
import com.eryansky.modules.notice.service.MessageService;
import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.SelectType;
import com.eryansky.server.result.WSResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息
 *
 * @author Eryan
 * @date 2016-03-24
 */
@Controller
@RequestMapping(value = "${adminPath}/notice/message")
public class MessageController extends SimpleController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageReceiveService messageReceiveService;
    @Autowired
    private MessageTask messageTask;

    @ModelAttribute("model")
    public Message get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return messageService.get(id);
        } else {
            return new Message();
        }
    }


    @RequiresPermissions("notice:message:view")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"list", "", "audit"})
    public ModelAndView list(@ModelAttribute("model") Message model,String appId, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/messageList");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String userId = sessionInfo.getUserId();
        String _appId = StringUtils.isNotBlank(appId) ? appId:Message.DEFAULT_ID;
        if (!sessionInfo.isSuperUser()) {//非管理员
            model.setOrganId(sessionInfo.getLoginCompanyId());
        }else{//管理员
            _appId = StringUtils.isNotBlank(appId) ? appId:null;
            userId = null;
        }
//        Page<Message> page = messageService.findQueryPage(new Page<>(request, response),_appId,userId,model.getStatus(),true);
        Page<Message> page = messageService.findQueryPage(new Page<>(request, response),null,userId,model.getStatus(),null,null,true,null);
        modelAndView.addObject("page", page);
        modelAndView.addObject("model", model);
        return modelAndView;
    }

    @RequiresPermissions("notice:message:view")
    @GetMapping(value = "form")
    public ModelAndView form(@ModelAttribute("model") Message model) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/messageForm");
        if (StringUtils.isBlank(model.getReceiveObjectType())) {
            model.setReceiveObjectType(MessageReceiveObjectType.User.getValue());
        }
        modelAndView.addObject("model", model);
        modelAndView.addObject("messageReceiveObjectTypes", MessageReceiveObjectType.values());
//        modelAndView.addObject("messageChannels", MessageChannel.values());
        modelAndView.addObject("messageChannels", NoticeConstants.getMessageTipChannels());
        return modelAndView;
    }

    /**
     * 保存 发送消息
     *
     * @param model
     * @param uiModel
     * @param redirectAttributes
     * @param receiveObjectType  {@link MessageReceiveObjectType}
     * @param objectIds
     * @return
     */
    @RequiresPermissions("notice:message:edit")
    @PostMapping(value = "save")
    public ModelAndView save(@ModelAttribute("model") Message model, Model uiModel, RedirectAttributes redirectAttributes,
                             String receiveObjectType,
                             @RequestParam(value = "objectIds") List<String> objectIds) {
        if (!beanValidator(uiModel, model)) {
            return form(model);
        }

        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (StringUtils.isBlank(model.getOrganId())) {
            model.setOrganId(sessionInfo.getLoginOrganId());
        }

        if (StringUtils.isBlank(model.getSender())) {
            model.setSender(sessionInfo.getUserId());
        }

        messageService.save(model);

        MessageReceiveObjectType messageReceiveObjectType = MessageReceiveObjectType.getByValue(receiveObjectType);
        if (StringUtils.isNotBlank(receiveObjectType) && messageReceiveObjectType != null) {
            messageTask.saveAndSend(model, messageReceiveObjectType, objectIds);
        }

        addMessage(redirectAttributes, "消息正在发送...请稍候！");
        ModelAndView modelAndView = new ModelAndView("redirect:" + AppConstants.getAdminPath() + "/notice/message");
        return modelAndView;
    }

    @RequiresPermissions("notice:message:edit")
    @GetMapping(value = "delete")
    public ModelAndView delete(@ModelAttribute("model") Message model, @RequestParam(required = false) Boolean isRe, RedirectAttributes redirectAttributes) {
        messageService.delete(model, isRe);
        addMessage(redirectAttributes, (isRe != null && isRe ? "恢复" : "") + "删除消息成功");
        ModelAndView modelAndView = new ModelAndView("redirect:" + AppConstants.getAdminPath() + "/notice/message");
        return modelAndView;
    }

    /**
     * 推送（仅限推送,由切面实现）
     *
     * @param model
     * @return
     */
    @RequiresPermissions("notice:message:edit")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"push"})
    @ResponseBody
    public ModelAndView push(@ModelAttribute("model") Message model, RedirectAttributes redirectAttributes) {
        messageService.push(model.getId());
        addMessage(redirectAttributes, "提交推送消息请求成功");
        ModelAndView modelAndView = new ModelAndView("redirect:" + AppConstants.getAdminPath() + "/notice/message");
        return modelAndView;
    }


    @RequiresPermissions("notice:message:view")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "info")
    public ModelAndView info(@ModelAttribute("model") Message model, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/messageInfo");
        modelAndView.addObject("model", model);
        //接收信息
        Page<MessageReceive> page = new Page<>(request, response);
        MessageReceive messageReceive = new MessageReceive(model.getId());
        page = messageReceiveService.findPage(page, messageReceive);
        modelAndView.addObject("page", page);
        return modelAndView;
    }




    @GetMapping(value = "view")
    public ModelAndView view(@ModelAttribute("model") Message model, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/messageView");
        modelAndView.addObject("model", model);
        return modelAndView;
    }


    /**
     * 发送消息 （可供外部调用）
     *
     * @param content           必选 消息内容
     * @param linkUrl           消息URL链接地址
     * @param receiveObjectType 必选 接收类型 {@link MessageReceiveObjectType}
     * @param receiveObjectIds  必选 接收对象ID集合 多个之间以”,“分割
     * @return
     */
    @PostMapping(value = "sendMessage")
    @ResponseBody
    public Result sendMessage(@RequestParam(value = "content") String content,
                              String linkUrl,
                              @RequestParam(value = "receiveObjectType") String receiveObjectType,
                              @RequestParam(value = "receiveObjectIds") List<String> receiveObjectIds) {
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        MessageUtils.sendMessage(null, sessionInfo.getUserId(), content, linkUrl, receiveObjectType, receiveObjectIds,null);
        result = Result.successResult().setMsg("消息正在发送...请稍候！");
        return result;
    }


    /**
     * 发送消息给管理员 （可供外部调用）
     *
     * @param content 必选 消息内容
     * @param linkUrl 消息URL链接地址
     * @return
     */
    @PostMapping(value = "sendToManager")
    @ResponseBody
    public Result sendToManager(@RequestParam(value = "content") String content,
                                String linkUrl) {
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        User superUser = UserUtils.getSuperUser();
        List<String> receiveObjectIds = new ArrayList<>(1);
        receiveObjectIds.add(superUser.getId());
        MessageUtils.sendMessage(null, null != sessionInfo ? sessionInfo.getUserId():null, content, linkUrl, MessageReceiveObjectType.User.getValue(), receiveObjectIds,null);
        result = Result.successResult().setMsg("消息正在发送...请稍候！");
        return result;
    }

    /**
     * 消息提醒 下拉列表
     *
     * @param selectType
     * @return
     * @throws Exception
     */
    @PostMapping(value = {"tipMessageCombobox"})
    @ResponseBody
    public List<Combobox> tipMessageCombobox(String selectType) {
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        MessageChannel[] _enums = MessageChannel.values();
        for (MessageChannel column : _enums) {
            Combobox combobox = new Combobox(column.getValue(), column.getDescription());
            cList.add(combobox);
        }
        return cList;
    }

    /**
     * 外部消息接口
     *
     * @param paramJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = {"api/sendMessage"})
    @ResponseBody
    public WSResult sendMessage(String paramJson) {
        if (SystemInitListener.Static.apiWebService != null) {
            return SystemInitListener.Static.apiWebService.sendMessage(paramJson);
        }
        return WSResult.buildDefaultErrorResult(WSResult.class);
    }

    /**
     * 明细信息
     * @param model
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") Message model) {
        return Result.successResult().setObj(model);
    }


}