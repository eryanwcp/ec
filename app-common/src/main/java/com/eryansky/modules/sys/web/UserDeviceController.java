/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.excels.ExcelUtils;
import com.eryansky.core.excels.JsGridReportBase;
import com.eryansky.core.excels.TableData;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys._enum.DeviceType;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.mapper.UserDevice;
import com.eryansky.modules.sys.service.UserDeviceService;
import com.eryansky.utils.AppConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户设备管理 Controller
 *
 * @author eryan
 * @date 2026-09-16
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/userDevice")
public class UserDeviceController extends SimpleController {

    @Resource
    private UserDeviceService userDeviceService;

    /**
     * 用户设备列表
     *
     * @param export
     * @param model
     * @param uiModel
     * @param request
     * @param response
     * @return
     */
    @Logging(value = "用户设备", logType = LogType.access)
    @RequiresPermissions("sys:userDevice:view")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"list", ""})
    public String list(@RequestParam(value = "export", defaultValue = "false") Boolean export,
                       UserDevice model, Model uiModel,
                       Date beginLastLoginTime,
                       Date endLastLoginTime,
                       HttpServletRequest request, HttpServletResponse response) {
        Page<UserDevice> page = new Page<>(request, response);
        if (beginLastLoginTime == null) {
            beginLastLoginTime = Calendar.getInstance().getTime();
        }
        if (WebUtils.isAjaxRequest(request) || export) {
            if (export) {
                page.setPageSize(Page.PAGESIZE_ALL);
            }
            model.setStatus(DataEntity.STATUS_NORMAL);
            page = userDeviceService.findPage(page, model, beginLastLoginTime, endLastLoginTime);
            if (export) {
                String title = "用户设备列表";
                String[] hearders = new String[]{"用户ID", "用户名称", "用户类型", "设备标识",
                        "设备名称", "设备类型", "登录次数", "是否为常用设备",
                        "最近登录位置", "登录IP", "首次登录时间", "最后登录时间"};// 表头数组
                String[] fields = new String[]{"userId", "userName", "userTypeView", "deviceId",
                        "deviceName", "deviceTypeView", "loginCount", "isCommonView",
                        "location", "ips", "firstLoginTime", "lastLoginTime"};// 对象属性数组
                TableData td = ExcelUtils.createTableData(page.getResult(),
                        ExcelUtils.createTableHeader(hearders, 0), fields);
                try {
                    JsGridReportBase report = new JsGridReportBase(request, response);
                    report.exportToExcel(title, SecurityUtils.getCurrentUserName(), td);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
            return renderString(response, page);
        }
        uiModel.addAttribute("page", page);
        uiModel.addAttribute("model", model);
        uiModel.addAttribute("beginLastLoginTime", DateUtils.formatDate(beginLastLoginTime));
        uiModel.addAttribute("endLastLoginTime", endLastLoginTime != null ? DateUtils.formatDate(endLastLoginTime) : null);
        uiModel.addAttribute("deviceTypes", DeviceType.values());
        return "modules/sys/userDeviceList.html";
    }

    @Logging(value = "用户设备-删除", logType = LogType.operate)
    @RequiresPermissions("sys:area:edit")
    @GetMapping(value = "delete")
    public String delete(@ModelAttribute("model") UserDevice model, RedirectAttributes redirectAttributes) {
        userDeviceService.delete(model);
        addMessage(redirectAttributes, "操作成功！");
        return "redirect:" + AppConstants.getAdminPath() + "/sys/userDevice";
    }


}