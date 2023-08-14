/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.web.mobile;

import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.notice.service.NoticeReceiveInfoService;
import com.eryansky.modules.notice.service.NoticeService;
import com.eryansky.modules.notice.vo.NoticeQueryVo;
import com.eryansky.modules.sys._enum.LogType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eryan
 * @date 2015-09-01
 */
@Mobile
@Controller
@RequestMapping(value = "${mobilePath}/notice")
public class NoticeMobileController extends SimpleController {

    @Autowired
    private NoticeReceiveInfoService noticeReceiveInfoService;
    @Autowired
    private NoticeService noticeService;

    @ModelAttribute("model")
    public Notice get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return noticeService.get(id);
        } else {
            return new Notice();
        }
    }

    @Logging(logType = LogType.access, value = "我的通知")
    @GetMapping(value = {""})
    public String list() {
        return "modules/notice/notice";
    }


    /**
     * @return
     */
    @PostMapping(value = "noticePage")
    @ResponseBody
    public String noticePage() {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Page<NoticeReceiveInfo> page = new Page<>(SpringMVCHolder.getRequest());
        if (sessionInfo != null) {
            page = noticeReceiveInfoService.findReadNoticePage(page, new NoticeReceiveInfo(), sessionInfo.getUserId(), null);
        }
        Datagrid dg = new Datagrid(page.getTotalCount(), page.getResult());
        String json = JsonMapper.getInstance().toJson(dg, NoticeReceiveInfo.class,
                new String[]{"id", "noticeId","title", "type", "typeView", "isTop", "headImageUrl", "isRead",
                        "isReadView", "publishTime"});
        return json;
    }

    /**
     * @return
     */
    @PostMapping(value = "noticeData")
    @ResponseBody
    public String noticeData(HttpServletRequest request, HttpServletResponse response,
                             NoticeQueryVo noticeQueryVo) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Page<NoticeReceiveInfo> page = new Page<>(request,response);
        if (sessionInfo != null) {
            page = noticeReceiveInfoService.findReadNoticePage(page, new NoticeReceiveInfo(), sessionInfo.getUserId(), noticeQueryVo);
        }
        String json = JsonMapper.getInstance().toJson(Result.successResult().setObj(page), NoticeReceiveInfo.class,
                new String[]{"id", "noticeId","title", "type", "typeView", "isTop", "headImageUrl", "isRead",
                        "isReadView", "publishTime"});
        return renderString(response,json, WebUtils.JSON_TYPE);
    }



    /**
     * 明细信息
     * @param model
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") Notice model) {
        return Result.successResult().setObj(model);
    }
}