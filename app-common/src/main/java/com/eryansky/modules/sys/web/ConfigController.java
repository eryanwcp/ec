/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.mapper.Config;
import com.eryansky.modules.sys.service.ConfigService;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author Eryan
 * @date 2015-05-14
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/config")
public class ConfigController extends SimpleController {
    @Autowired
    private ConfigService configService;


    @RequiresPermissions("sys:config:view")
    @Logging(value = "属性配置", logType = LogType.access)
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {""})
    public String list() {
        return "modules/sys/config";
    }

    @ModelAttribute("model")
    public Config get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return configService.get(id);
        } else {
            return new Config();
        }
    }

    @GetMapping(value = {"input"})
    public String input() {
        return "modules/sys/config-input";
    }

    @PostMapping(value = {"datagrid"})
    @ResponseBody
    public Datagrid<Config> datagrid(Config model, HttpServletRequest request, HttpServletResponse response,
                                     String query) {
        Page<Config> page = new Page<>(request);
        page = configService.findPage(page, query);
        return new Datagrid(page.getTotalCount(), page.getResult());
    }

    @RequiresPermissions("sys:config:edit")
    @Logging(value = "属性配置-保存配置", logType = LogType.access)
    @PostMapping(value = "save",produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result save(@ModelAttribute("model") Config model) {
        Result result;
        // 属性名重复校验
        Config checkConfig = configService.getConfigByCode(model.getCode());
        if (checkConfig != null && !checkConfig.getId().equals(model.getId())) {
            result = new Result(Result.WARN, "属性名为[" + model.getCode() + "]已存在,请修正!", "code");
            logger.debug(result.toString());
            return result;
        }

        configService.save(model);
        result = Result.successResult();
        return result;
    }

    /**
     * 从配置文件同步
     *
     * @param overrideFromProperties
     * @return
     */
    @RequiresPermissions("sys:config:edit")
    @Logging(value = "属性配置-配置文件同步", logType = LogType.access)
    @PostMapping(value = {"syncFromProperties"})
    @ResponseBody
    public Result syncFromProperties(Boolean overrideFromProperties) {
        Result result;
        configService.syncFromProperties(overrideFromProperties);
        result = Result.successResult();
        return result;
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @RequiresPermissions("sys:config:edit")
    @Logging(value = "属性配置-删除配置", logType = LogType.access)
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        configService.deleteByIds(ids);
        return Result.successResult();
    }



    private static final String[] CONFIGS = {"app.version",
            "app.name",
            "app.fullName",
            "app.shortName",
            "app.productName",
            "app.productURL",
            "app.productContact",
            "security.on",
            "security.password.checkLogin",
            "security.password.checkPolicy",
            "security.password.updateCycle",
            "security.password.repeatCount",
            "security.password.loginAgainSize",
            "security.sessionUser.UserSessionSize",
            "security.sessionUser.MaxSize",
            "system.logKeepTime",
            "system.security.limit.user.whitelist",
            "system.security.limit.ip.enable",
            "system.security.limit.ip.whiteEnable",
            "system.security.limit.ip.whitelist",
            "system.security.limit.ip.blacklist",
            "system.security.proxy.enable",
            "system.security.proxy.whitelist",
            "system.rest.enable",
            "system.rest.defaultApiKey",
            "system.rest.limit.ip.enable",
            "system.rest.limit.ip.whitelist",
            "system.rest.limit.url.enable",
    };

    /**
     * 部分系统参数配置 表单
     * @param uiModel
     * @return
     */
    @Logging(value = "参数配置",logType = LogType.access)
    @RequiresPermissions("sys:config:view")
    @GetMapping(value = {"paramForm"})
    public String paramForm(Model uiModel) {
        Map<String,Object> data = Maps.newHashMap();
        for(String configCode:CONFIGS){
            Config config = configService.getConfigByCode(configCode);
//            data.put(configCode,config != null ? config.getValue():null);
            data.put(configCode,config != null ? config.getValue():AppConstants.getConfig(configCode,null));
        }
        uiModel.addAttribute("data",data);
        return "modules/sys/config-paramForm";
    }


    /**
     * 保存
     * @param request
     * @param redirectAttributes
     * @param uiModel
     * @return
     */
    @Logging(value = "参数配置-保存", logType = LogType.access)
    @RequiresPermissions("sys:config:edit")
    @PostMapping(value = {"saveParam"})
    public String saveParam(HttpServletRequest request, RedirectAttributes redirectAttributes, Model uiModel) {
        if (AppConstants.isdevMode()) {
            addMessage(uiModel,"系统处于开发模式，无效操作！");
            return paramForm(uiModel);
        }
        for (String configCode : CONFIGS) {
            String configValue = request.getParameter(configCode);
            Config config = configService.getConfigByCode(configCode);
            if (config == null) {
                config = new Config();
            }
            config.setCode(configCode);
            config.setValue(configValue);
            configService.save(config);
            addMessage(redirectAttributes, "操作成功！");
        }
        return "redirect:" + AppConstants.getAdminPath() + "/sys/config/paramForm?repage";
    }

}