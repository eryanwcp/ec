/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.annotation.MobileValue;
import com.eryansky.modules.sys._enum.LogType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 在线用户管理
 *
 * @author Eryan
 * @date 2015-05-18
 */
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
    @ResponseBody
    public Datagrid<SessionInfo> onlineDatagrid(HttpServletRequest request, String query) {
        Page<SessionInfo> page = new Page<>(request);
        page = SecurityUtils.findSessionInfoPage(page,null,query);
        return new Datagrid<>(page.getTotalCount(), page.getResult());
    }

    /**
     * 在线用户
     *
     * @return
     */
    @PostMapping(value = {"winthPermissionsOnLineSessions"})
    @ResponseBody
    public Datagrid<SessionInfo> winthPermissionsOnLineSessions(HttpServletRequest request, String query) {
        Page<SessionInfo> page = new Page<>(request);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        page = SecurityUtils.findSessionInfoPage(page,(sessionInfo.isSuperUser() || SecurityUtils.isPermittedMaxRoleDataScope()) ? null:sessionInfo.getLoginCompanyId(),query);
        return new Datagrid<>(page.getTotalCount(), page.getResult());
    }
    /**
     * 强制用户下线
     *
     * @param sessionIds sessionID集合
     * @return
     */
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
     * @param id
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(String id) {
        SessionInfo sessionInfo = SecurityUtils.getSessionInfo(id);
        return Result.successResult().setObj(sessionInfo);
    }

    /**
     * 根据token同步sessionid
     *
     * @param token
     * @param sessionId
     * @param request
     * @return
     */
    @Logging(value = "同步Token到Session",logType = LogType.access)
    @PostMapping(value = {"syncTokenToSession"})
    @ResponseBody
    public Result syncTokenToSession(String token,String sessionId,HttpServletRequest request) {
        SessionInfo sessionInfo = SecurityUtils.getSessionInfoByTokenOrRefreshToken(token);
        String _sessionId = StringUtils.isNotBlank(sessionId) ? sessionId:request.getSession().getId();
        //更新真实的SessionID
        if (sessionInfo != null && _sessionId != null && !sessionInfo.getSessionId().equals(_sessionId)) {
            sessionInfo.setId(_sessionId);
            sessionInfo.setSessionId(_sessionId);
            SecurityUtils.refreshSessionInfo(sessionInfo);
        }
        return Result.successResult().setData(sessionInfo);
    }


}