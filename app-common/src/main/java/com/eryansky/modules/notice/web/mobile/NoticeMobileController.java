/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.web.mobile;

import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.notice._enum.NoticeReadMode;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.notice.service.NoticeReceiveInfoService;
import com.eryansky.modules.notice.service.NoticeService;
import com.eryansky.modules.notice.utils.NoticeUtils;
import com.eryansky.modules.notice.vo.NoticeQueryVo;
import com.eryansky.modules.notice.vo.NoticeReceiveInfoSimpleVo;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.utils.DictionaryUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Eryan
 * @date 2015-09-01
 */
@Mobile
@Controller
@RequestMapping(value = "${mobilePath}/notice")
public class NoticeMobileController extends SimpleController {

    @Resource
    private NoticeReceiveInfoService noticeReceiveInfoService;
    @Resource
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
            page = noticeReceiveInfoService.findReadNoticePage(page,  sessionInfo.getUserId(), null);
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
    public Page<NoticeReceiveInfoSimpleVo> noticeData(HttpServletRequest request, HttpServletResponse response,
                                                      NoticeQueryVo noticeQueryVo) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Page<NoticeReceiveInfoSimpleVo> page = new Page<>(request, response);
        if (sessionInfo != null) {
            page = noticeReceiveInfoService.findReadNoticePageByUserId(page, sessionInfo.getUserId(), noticeQueryVo);

        }
        return page;
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