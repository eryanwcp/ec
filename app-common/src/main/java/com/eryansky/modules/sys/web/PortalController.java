/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.web.annotation.MobileValue;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserPasswordService;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.vo.PasswordTip;
import com.eryansky.utils.AppUtils;
import com.google.common.collect.Maps;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.notice.service.NoticeReceiveInfoService;
import com.eryansky.modules.notice.service.NoticeService;
import com.eryansky.utils.AppConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * Portal主页门户管理
 *
 * @author Eryan
 * @date 2014-07-31 12:30
 */
@Controller
@RequestMapping(value = "${adminPath}/portal")
public class PortalController extends SimpleController {

    @Autowired
    private UserService userService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private NoticeReceiveInfoService noticeReceiveInfoService;
    @Autowired
    private UserPasswordService userPasswordService;


    @GetMapping(value = "")
    public ModelAndView portal() {
        ModelAndView modelAnView = new ModelAndView("layout/portal");
        return modelAnView;
    }

    /**
     * 个人消息中心
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "mymessages")
    @ResponseBody
    public Result mymessages(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WebUtils.setNoCacheHeader(response);
        Result result = null;
        Map<String, Object> map = Maps.newHashMap();
        // 当前登录用户
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        long noticeReceiveInfos = 0;
        Page<NoticeReceiveInfo> page = new Page<>(request);
        page = noticeReceiveInfoService.findUserUnreadNotices(page, sessionInfo.getLoginName());
        if (Collections3.isNotEmpty(page.getResult())) {
            noticeReceiveInfos = page.getTotalCount();
        }
        map.put("noticeReceiveInfos", noticeReceiveInfos);

        if (AppConstants.getIsSecurityOn() && AppConstants.isCheckLoginPassword()) {
            PasswordTip passwordTip = userPasswordService.checkPassword(sessionInfo.getUserId());
            if(passwordTip.isTip()){
                map.put("passwordTip", passwordTip);
                passwordTip.setUrl(AppUtils.createSecurityUpdatePasswordUrl(request));
            }
        }

        result = Result.successResult().setObj(map);
        return result;
    }


    /**
     * 个人消息中心
     *
     * @return
     * @throws Exception
     */
    @PostMapping(value = "checkPassword")
    @ResponseBody
    public Result checkPassword(HttpServletRequest request,HttpServletResponse response) throws Exception {
        Result result = null;
        Map<String, Object> map = Maps.newHashMap();
        // 当前登录用户
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (AppConstants.getIsSecurityOn() && AppConstants.isCheckLoginPassword()) {
            PasswordTip passwordTip = userPasswordService.checkPassword(sessionInfo.getUserId());
            passwordTip.setUrl(AppUtils.createSecurityUpdatePasswordUrl(request));
            map.put("passwordTip", passwordTip);
        }

        result = Result.successResult().setObj(map);
        return result;
    }

    /**
     * 我的通知
     *
     * @return
     */
    @Mobile(value = MobileValue.PC)
    @GetMapping(value = "notice")
    public ModelAndView notice(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAnView = new ModelAndView("layout/portal-notice");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            Page<NoticeReceiveInfo> page = new Page<>(SpringMVCHolder.getRequest());
            page = noticeReceiveInfoService.findReadNoticePage(page, new NoticeReceiveInfo(), sessionInfo.getUserId(), null);
            modelAnView.addObject("page", page);

        }
        return modelAnView;
    }


}
