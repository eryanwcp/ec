/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Result;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.modules.sys._enum.UserPasswordUpdateType;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserPasswordService;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.modules.sys.vo.PasswordTip;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 主页管理
 *
 * @author Eryan
 * @date 2014-09-16 10:30
 */
@Controller
@RequestMapping(value = "${adminPath}")
public class IndexController extends SimpleController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserPasswordService userPasswordService;

    @GetMapping(value = {""})
    public ModelAndView admin(HttpServletRequest request) {
        ModelAndView modelAnView = new ModelAndView("login");
        if (SecurityUtils.getCurrentSessionInfo() != null) {
            return index(request);
        }
        return modelAnView;
    }

    @GetMapping(value = {"index"})
    public ModelAndView index(HttpServletRequest request) {
        User sessionUser = SecurityUtils.getCurrentUser();
        String userPhoto = null;
        if (sessionUser != null && StringUtils.isNotBlank(sessionUser.getPhoto())) {
            userPhoto = sessionUser.getPhotoUrl();
        }

        if (StringUtils.isBlank(userPhoto)) {
            userPhoto = SpringMVCHolder.getRequest().getContextPath() + "/static/img/icon_boy.png";
        }
        ModelAndView modelAnView = new ModelAndView("layout/index.html");
        modelAnView.addObject("userPhoto", userPhoto);
        modelAnView.addObject("isMobile", UserAgentUtils.isMobile(request));
        return modelAnView;
    }

    /**
     * 用户密码初始化或修改页面
     * @param request
     * @param type {@link com.eryansky.modules.sys._enum.UserPasswordUpdateType}
     * @param fromLogin 从登录页跳转
     * @param fromExtend 从外部APP跳转
     * @param extendUrl 外部APPURL
     * @param token 安全Token
     * @return
     */
    @RequiresUser(required = false)
    @GetMapping(value = {"index/password"})
    public ModelAndView password(HttpServletRequest request,
                                 String type,
                                 @RequestParam(name = "fromLogin",defaultValue = "false") Boolean fromLogin,
                                 @RequestParam(name = "fromExtend",defaultValue = "false") Boolean fromExtend,
                                 @RequestParam(name = "extendUrl",required = false) String extendUrl,
                                 @RequestParam(name = "token",required = false) String token) {
        ModelAndView modelAnView = new ModelAndView("modules/sys/password.html");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        User model = null;
        if (fromLogin && StringUtils.isNotBlank(token)) {
            String tokenLoginName = SecurityUtils.getLoginNameByToken(token);
            model = UserUtils.getUserByLoginName(tokenLoginName);
        }else{
            if (null == sessionInfo) {
                throw new ActionException("非法请求！");
            }
            model = SecurityUtils.getCurrentUser();
        }

        if (null == model) {
            logger.error("{} {} {} {}",fromLogin,fromExtend,extendUrl,token);
            throw new ActionException("非法请求！");
        }

        String _type = type;
        if(StringUtils.isBlank(type)){
            PasswordTip passwordTip = userPasswordService.checkPassword(model.getId());
            _type = String.valueOf(passwordTip.getCode());
        }
        modelAnView.addObject("type",_type);
        modelAnView.addObject("model", model);
        modelAnView.addObject("isCheckPasswordPolicy", AppConstants.isCheckPasswordPolicy());
        modelAnView.addObject("homeUrl",fromExtend ? extendUrl: AppUtils.getClientAppURL());
        modelAnView.addObject("fromLogin", fromLogin);
        modelAnView.addObject("fromExtend", fromExtend);
        modelAnView.addObject("token", token);
        return modelAnView;
    }

    /**
     * 用户信息维护
     * @param request
     * @return
     */
    @GetMapping(value = {"index/userinfo"})
    public ModelAndView password(HttpServletRequest request) {
        ModelAndView modelAnView = new ModelAndView("modules/sys/userinfo.html");
        User sessionUser = SecurityUtils.getCurrentUser();
        modelAnView.addObject("model", sessionUser);
        return modelAnView;
    }

}
