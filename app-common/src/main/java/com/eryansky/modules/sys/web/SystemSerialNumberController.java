/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.excels.CsvUtils;
import com.eryansky.core.excels.ExcelUtils;
import com.eryansky.core.excels.JsGridReportBase;
import com.eryansky.core.excels.TableData;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys._enum.ResetType;
import com.eryansky.modules.sys.mapper.SystemSerialNumber;
import com.eryansky.modules.sys.service.SystemSerialNumberService;
import com.eryansky.modules.sys.sn.MaxSerial;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Eryan
 * @date 2016-07-18
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/systemSerialNumber")
public class SystemSerialNumberController extends SimpleController {

    @Autowired
    private SystemSerialNumberService systemSerialNumberService;

    @ModelAttribute("model")
    public SystemSerialNumber get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return systemSerialNumberService.get(id);
        } else {
            return new SystemSerialNumber();
        }
    }


    @RequiresPermissions("sys:systemSerialNumber:view")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"list", ""})
    public String list(SystemSerialNumber model,
                       @RequestParam(value = "export", defaultValue = "false") Boolean export,
                       @RequestParam(value = "exportType", defaultValue = "xls") String exportType,
                       HttpServletRequest request, HttpServletResponse response, Model uiModel) {
        Page<SystemSerialNumber> page = new Page<SystemSerialNumber>(request, response);
        if (WebUtils.isAjaxRequest(request) || export) {
            if (export) {
                page.setPageSize(Page.PAGESIZE_ALL);
            }
            page = systemSerialNumberService.findPage(page, model);

            if (export) {
                String title = "序列号_" + DateUtils.getCurrentDate();
                String[] hearders = new String[]{"APP标识", "模块名称", "模块编码", "配置模板", "最大值", "重置类型", "是否自动增长", "预生成流水号数量", "备注", "时间"};//表头数组
                String[] fields = new String[]{"app", "modlueName", "modlueCode", "configTemplate", "remark", "updateTime"};
                TableData td = ExcelUtils.createTableData(page.getResult(), ExcelUtils.createTableHeader(hearders, 0), fields);

                if (page.getResult().size() < 65531) {
                    //导出Excel
                    try {
                        JsGridReportBase report = new JsGridReportBase(request, response);
                        report.exportToExcel(title, SecurityUtils.getCurrentUserName(), td);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    //导出CSV
                    try {
                        List<Object[]> data = Lists.newArrayList();
                        page.getResult().forEach(o -> data.add(new Object[]{o.getApp(), o.getModuleName(), o.getModuleCode(), o.getConfigTemplate(), o.getMaxSerial(), o.getResetType(), o.getIsAutoIncrement(), o.getPreMaxNum(), o.getRemark(), DateUtils.formatDateTime(o.getUpdateTime())}));
                        CsvUtils.exportToCsv(title, hearders, data, request, response);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                return null;
            }

            return renderString(response, page);
        }
        uiModel.addAttribute("page", page);
        return "modules/sys/systemSerialNumberList";
    }


    @RequiresPermissions("sys:systemSerialNumber:view")
    @GetMapping(value = "form")
    public String form(@ModelAttribute("model") SystemSerialNumber model, Model uiModel) {
        uiModel.addAttribute("model", model);
        uiModel.addAttribute("resetTypes", ResetType.values());
        return "modules/sys/systemSerialNumberForm";
    }


    @Override
    protected void initBinder(WebDataBinder binder) {
        super.defaultWebDataBinder(binder);
        super.setAllowedFields(binder);
    }

    @RequiresPermissions("sys:systemSerialNumber:edit")
    @PostMapping(value = "save")
    public String save(@ModelAttribute("model") SystemSerialNumber model,
                       String _maxSerial, Model uiModel, RedirectAttributes redirectAttributes) {
        if (!beanValidator(uiModel, model)) {
            return form(model, uiModel);
        }
        MaxSerial maxSerial = (MaxSerial) JsonMapper.fromJsonString(HtmlUtils.htmlUnescape(_maxSerial), MaxSerial.class);
        model.setMaxSerial(maxSerial);
        systemSerialNumberService.save(model);
        addMessage(redirectAttributes, "保存'" + model.getModuleName() + "'成功");
        return "redirect:" + AppConstants.getAdminPath() + "/sys/systemSerialNumber";
    }

    @RequiresPermissions("sys:systemSerialNumber:edit")
    @GetMapping(value = "delete")
    public String delete(@ModelAttribute("model") SystemSerialNumber model, RedirectAttributes redirectAttributes) {
        systemSerialNumberService.delete(model);
        addMessage(redirectAttributes, "删除成功");
        return "redirect:" + AppConstants.getAdminPath() + "/sys/systemSerialNumber";
    }


    /**
     * 重置序列号
     *
     * @return
     * @throws Exception
     */
    @RequiresPermissions("sys:systemSerialNumber:edit")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"resetSerialNumber"})
    @ResponseBody
    public Result resetSerialNumber(String id) {
        if(StringUtils.isNotBlank(id)){
            systemSerialNumberService.resetSerialNumber(id);
        }else{
            systemSerialNumberService.resetSerialNumber();
        }
        return Result.successResult();
    }

    /**
     * 详细信息
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") SystemSerialNumber model) {
        return Result.successResult().setObj(model);
    }

}