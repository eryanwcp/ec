/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk._enum.FolderType;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.VersionLogType;
import com.eryansky.modules.sys.mapper.VersionLog;
import com.eryansky.modules.sys.service.VersionLogService;
import com.eryansky.modules.sys.utils.VersionLogUtils;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import org.apache.commons.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * @author Eryan
 * @date 2015-01-09
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/versionLog")
public class VersionLogController extends SimpleController {

    @Autowired
    private VersionLogService versionLogService;


    @RequiresPermissions("sys:versionLog:view")
    @Logging(value = "版本更新", logType = LogType.access)
    @GetMapping(value = {""})
    public String list(Model uiModel) {
        uiModel.addAttribute("versionLogTypes",VersionLogType.values());
        return "modules/sys/versionLog";
    }

    @ModelAttribute("model")
    public VersionLog get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return versionLogService.get(id);
        } else {
            return new VersionLog();
        }
    }

    /**
     * 数据列表
     *
     * @param request
     * @param startTIme 更新时间 - 起始时间
     * @param endTime   更新时间 - 截止时间
     * @return
     */
    @PostMapping(value = {"datagrid"})
    @ResponseBody
    public Datagrid<VersionLog> datagrid(VersionLog model, HttpServletRequest request,
                                         @RequestParam(value = "startTime", required = false) Date startTIme,
                                         @RequestParam(value = "endTime", required = false) Date endTime) {
        Page<VersionLog> page = new Page<>(request);
        Parameter parameter = new Parameter();
        if (startTIme != null) {
            parameter.put("startTime", DateUtils.format(startTIme, DateUtils.DATE_TIME_FORMAT));
        }
        if (endTime != null) {
            parameter.put("endTime", DateUtils.format(endTime, DateUtils.DATE_TIME_FORMAT));
        }

        parameter.put("versionName", model.getVersionName());
        parameter.put("remark", model.getRemark());
        parameter.put("versionLogType", model.getVersionLogType());
        parameter.put("query", model.getQuery());

        page = versionLogService.findPage(page, parameter);
        return new Datagrid<>(page.getTotalCount(), page.getResult());
    }

    /**
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"input"})
    public ModelAndView input(@ModelAttribute("model") VersionLog model) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/versionLog-input");
        File file = null;

        String fileId = model.getFileId();
        if (StringUtils.isNotBlank(fileId)) {
            file = DiskUtils.getFile(model.getFileId());
        }
        modelAndView.addObject("file", file);
        modelAndView.addObject("model", model);
        return modelAndView;
    }


    /**
     * 日志类型下拉列表.
     */
    @PostMapping(value = {"versionLogTypeCombobox"})
    @ResponseBody
    public List<Combobox> versionLogTypeCombobox(String selectType) {
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }

        VersionLogType[] lts = VersionLogType.values();
        for (VersionLogType lt : lts) {
            Combobox combobox = new Combobox();
            combobox.setValue(lt.getValue());
            combobox.setText(lt.getDescription());
            cList.add(combobox);
        }
        return cList;
    }

    /**
     * 文件上传
     */
    @PostMapping(value = {"upload"})
    @ResponseBody
    public static Result upload(@RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (null == sessionInfo) {
            return Result.errorResult().setMsg("未授权！");
        }
        Result result = null;
        Exception exception = null;
        File file = null;
        try {
            file = DiskUtils.saveSystemFile(VersionLog.FOLDER_VERSIONLOG, FolderType.NORMAL.getValue(), sessionInfo.getUserId(), multipartFile.getInputStream(),DiskUtils.getMultipartOriginalFilename(multipartFile));
            result = Result.successResult().setObj(file).setMsg("文件上传成功！");
        } catch (InvalidExtensionException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG);
        } catch (FileNameLengthLimitExceededException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG);
        } catch (ActionException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } catch (IOException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } finally {
            if (exception != null) {
                if (file != null) {
                    DiskUtils.deleteFile(file.getId());
                }
            }
        }
        return result;

    }

    /**
     * 保存
     *
     * @param model
     * @return
     */
    @RequiresPermissions("sys:versionLog:edit")
    @Logging(value = "版本更新-保存版本", logType = LogType.access)
    @PostMapping(value = {"save"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result save(@ModelAttribute("model") VersionLog model) {
        Result result;
        VersionLog checkEntity = versionLogService.getByVersionCode(model.getApp(),model.getVersionLogType(), model.getVersionCode());
        if (checkEntity != null && !checkEntity.getId().equals(model.getId())) {
            result = new Result(Result.WARN, "版本内部编号为[" + model.getVersionCode()
                    + "]已存在,请修正!", "versionCode");
            logger.debug(result.toString());
            return result;
        }
        versionLogService.save(model);
        result = Result.successResult();
        return result;
    }

    /**
     * 清空数据
     *
     * @return
     */
    @RequiresPermissions("sys:versionLog:edit")
    @Logging(value = "版本管理-删除版本", logType = LogType.access)
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        Result result = null;
        if (Collections3.isNotEmpty(ids)) {
            for (String id : ids) {
                versionLogService.delete(new VersionLog(id));
            }
        }
        result = Result.successResult();
        return result;
    }

    /**
     * 清空所有数据
     *
     * @return
     */
    @Logging(value = "版本管理-清空所有数据", logType = LogType.access)
    @PostMapping(value = {"removeAll"})
    @ResponseBody
    public Result removeAll() {
        versionLogService.clearAll();
        return Result.successResult();
    }


    /**
     * 查看通知
     *
     * @param id 通知ID
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"view/{id}"})
    public ModelAndView view(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/versionLog-view");
        File file = null;
        VersionLog model = versionLogService.get(id);
        if (StringUtils.isNotBlank(model.getFileId())) {
            file = DiskUtils.getFile(model.getFileId());
        }
        modelAndView.addObject("file", file);
        modelAndView.addObject("model", model);
        return modelAndView;
    }


    /**
     * APP下载
     * @param response
     * @param app
     * @param versionLogType
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"downloadApp/{versionLogType}"})
    public ModelAndView downloadApp(HttpServletResponse response,String app, @PathVariable String versionLogType) {
        return downloadApp(SpringMVCHolder.getRequest(),response,app,versionLogType);
    }


    /**
     * APP下载
     * @param request
     * @param response
     * @param app
     * @param versionLogType
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"downloadApp"})
    public ModelAndView downloadApp(HttpServletRequest request, HttpServletResponse response,String app, @PathVariable String versionLogType){
        String _versionLogType = versionLogType;
        if(StringUtils.isBlank(versionLogType)){
            VersionLogType vt = VersionLogUtils.getLatestVersionLogType(request);
            _versionLogType = null != vt ? vt.getValue():null;
        }
        if(StringUtils.isBlank(_versionLogType)){
            throw new ActionException("未识别参数[versionLogType]");
        }
        VersionLog model = versionLogService.getLatestVersionLog(app,_versionLogType);
        if (model != null && model.getFileId() != null) {
            File file = DiskUtils.getFile(model.getFileId());
            WebUtils.setDownloadableHeader(request, response, file.getName());
            java.io.File tempFile = file.getDiskFile();
            try(InputStream inputStream = new FileInputStream(tempFile);
                OutputStream outputStream = response.getOutputStream()){
                FileCopyUtils.copy(inputStream,outputStream);
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        } else {
            throw new ActionException("下载文件不存在！");
        }
        return null;
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
    public Result detail(@ModelAttribute("model") VersionLog model) {
        return Result.successResult().setObj(model);
    }
}