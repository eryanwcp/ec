/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.annotation.MobileValue;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.mapper.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 在线用户管理
 *
 * @author Eryan
 * @date 2015-05-18
 */
@PrepareOauth2(enable = false)
@Controller
@RequestMapping(value = "${adminPath}/sys/session")
public class SessionController extends SimpleController {

    @RequiresPermissions("sys:session:view")
    @Logging(value = "在线用户", logType = LogType.access)
    @GetMapping(value = {""})
    @Mobile(value = MobileValue.ALL)
    public ModelAndView list() {
        return new ModelAndView("modules/sys/session");
    }

    /**
     * 在线用户
     *
     * @return
     */
    @PostMapping(value = {"onLineSessions"})
    public String onlineDatagrid(HttpServletRequest request, HttpServletResponse response, String query) {
        Page<SessionInfo> page = new Page<>(request);
        page = SecurityUtils.findSessionInfoPage(page,null,query);
        Datagrid dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        String json = SecurityUtils.isCurrentUserAdmin() ? JsonMapper.getInstance().toJson(dg):JsonMapper.getInstance().toJsonWithExcludeProperties(dg, SessionInfo.class, new String[]{"token","refreshToken"});
        return renderString(response, json, WebUtils.JSON_TYPE);
    }

    /**
     * 在线用户
     *
     * @return
     */
    @PostMapping(value = {"winthPermissionsOnLineSessions"})
    public String winthPermissionsOnLineSessions(HttpServletRequest request, HttpServletResponse response, String query) {
        Page<SessionInfo> page = new Page<>(request);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        page = SecurityUtils.findSessionInfoPage(page,(sessionInfo.isSuperUser() || SecurityUtils.isPermittedMaxRoleDataScope()) ? null:sessionInfo.getLoginCompanyId(),query);
        Datagrid dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        String json = SecurityUtils.isCurrentUserAdmin() ? JsonMapper.getInstance().toJson(dg):JsonMapper.getInstance().toJsonWithExcludeProperties(dg, SessionInfo.class, new String[]{"token","refreshToken"});
        return renderString(response, json, WebUtils.JSON_TYPE);
    }
    /**
     * 强制用户下线
     *
     * @param sessionIds sessionID集合
     * @return
     */
    @Logging(value = "在线用户-强制用户下线",data = "#JsonMapper.toJson(#sessionIds)", logType = LogType.access)
    @RequiresPermissions("sys:session:edit")
    @PostMapping(value = {"offline"})
    @ResponseBody
    public Result offline(@RequestParam(value = "sessionIds") List<String> sessionIds) {
        SecurityUtils.offLine(sessionIds);
        return Result.successResult();
    }

    @RequiresPermissions("sys:session:edit")
    @PostMapping(value = {"offlineAll"})
    @ResponseBody
    public Result offlineAll() {
        if (SecurityUtils.isCurrentUserAdmin()) {
            SecurityUtils.offLineAll();
        } else {
            throw new ActionException("未授权.");
        }

        return Result.successResult();
    }


    /**
     * 详细信息
     *
     * @param sessionId
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    public String detail(HttpServletResponse response,String sessionId) {
        SessionInfo sessionInfo = SecurityUtils.getSessionInfo(sessionId);
        String json = SecurityUtils.isCurrentUserAdmin() ? JsonMapper.getInstance().toJson(sessionInfo):JsonMapper.getInstance().toJsonWithExcludeProperties(sessionInfo, SessionInfo.class, new String[]{"token","refreshToken"});
        return renderString(response, json, WebUtils.JSON_TYPE);
    }


}