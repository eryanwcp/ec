/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.web.annotation.MobileValue;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.notice.utils.NoticeConstants;
import com.eryansky.modules.sys.service.UserService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.notice._enum.IsTop;
import com.eryansky.modules.notice._enum.NoticeMode;
import com.eryansky.modules.notice._enum.NoticeReceiveScope;
import com.eryansky.modules.notice.mapper.Notice;
import com.eryansky.modules.notice.mapper.NoticeReceiveInfo;
import com.eryansky.modules.notice.service.NoticeReceiveInfoService;
import com.eryansky.modules.notice.service.NoticeSendInfoService;
import com.eryansky.modules.notice.service.NoticeService;
import com.eryansky.modules.notice.vo.NoticeQueryVo;
import com.eryansky.modules.sys._enum.DataScope;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.utils.SelectType;
import org.apache.commons.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 通知管理
 */
@Controller
@RequestMapping(value = "${adminPath}/notice")
public class NoticeController extends SimpleController {

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private NoticeReceiveInfoService noticeReceiveInfoService;
    @Autowired
    private NoticeSendInfoService noticeSendInfoService;
    @Autowired
    private UserService userService;

    /**
     * 操作类型
     */
    public enum OperateType {
        Save, Publish, RePublish, Repeat//保存、发送、重新发布、转发
    }

    @ModelAttribute("model")
    public Notice get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return noticeService.get(id);
        } else {
            return new Notice();
        }
    }

    /**
     * 通知发布（通知管理）
     *
     * @param noticeId 通知ID
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {""})
    public ModelAndView list(String noticeId,
                             @RequestParam(value = "objectType", required = false) String objectType,
                             @RequestParam(value = "objectId", required = false) String objectId,
                             @RequestParam(value = "title", required = false) String title,
                             @RequestParam(value = "content", required = false) String content,
                             @RequestParam(value = "fileIds", required = false) List<String> fileIds,
                             @RequestParam(value = "receiveUserIds", required = false) List<String> receiveUserIds,
                             @RequestParam(value = "receiveOrganIds", required = false) List<String> receiveOrganIds,
                             @RequestParam(value = "receiveContactGroupIds", required = false) List<String> receiveContactGroupIds) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/notice");
        modelAndView.addObject("noticeId", noticeId);
        modelAndView.addObject("objectType", objectType);
        modelAndView.addObject("objectId", objectId);
        modelAndView.addObject("title", title);
        modelAndView.addObject("content", content);
        modelAndView.addObject("fileIds", Collections3.isNotEmpty(fileIds) ? Collections3.convertToString(fileIds, ",") : "");
        modelAndView.addObject("receiveUserIds", Collections3.isNotEmpty(receiveUserIds) ? Collections3.convertToString(receiveUserIds, ",") : "");
        modelAndView.addObject("receiveOrganIds", Collections3.isNotEmpty(receiveOrganIds) ? Collections3.convertToString(receiveOrganIds, ",") : "");
        modelAndView.addObject("receiveContactGroupIds", Collections3.isNotEmpty(receiveContactGroupIds) ? Collections3.convertToString(receiveContactGroupIds, ",") : "");
        return modelAndView;
    }

    /**
     * 通知查看
     *
     * @param noticeId 通知ID
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"receive"})
    public ModelAndView receive(String noticeId,
                                @RequestParam(value = "objectType", required = false) String objectType,
                                @RequestParam(value = "objectId", required = false) String objectId,
                                @RequestParam(value = "title", required = false) String title,
                                @RequestParam(value = "content", required = false) String content,
                                @RequestParam(value = "fileIds", required = false) List<String> fileIds,
                                @RequestParam(value = "receiveUserIds", required = false) List<String> receiveUserIds,
                                @RequestParam(value = "receiveOrganIds", required = false) List<String> receiveOrganIds,
                                @RequestParam(value = "receiveContactGroupIds", required = false) List<String> receiveContactGroupIds) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/notice-receive");
        modelAndView.addObject("noticeId", noticeId);
        modelAndView.addObject("objectType", objectType);
        modelAndView.addObject("objectId", objectId);
        modelAndView.addObject("title", title);
        modelAndView.addObject("content", content);
        modelAndView.addObject("fileIds", Collections3.isNotEmpty(fileIds) ? Collections3.convertToString(fileIds, ",") : "");
        modelAndView.addObject("receiveUserIds", Collections3.isNotEmpty(receiveUserIds) ? Collections3.convertToString(receiveUserIds, ",") : "");
        modelAndView.addObject("receiveOrganIds", Collections3.isNotEmpty(receiveOrganIds) ? Collections3.convertToString(receiveOrganIds, ",") : "");
        modelAndView.addObject("receiveContactGroupIds", Collections3.isNotEmpty(receiveContactGroupIds) ? Collections3.convertToString(receiveContactGroupIds, ",") : "");
        return modelAndView;
    }

    /**
     * 发布通知列表
     *
     * @param noticeQueryVo {@link com.eryansky.modules.notice.vo.NoticeQueryVo} 查询条件
     * @return
     */
    @PostMapping(value = {"datagrid"})
    @ResponseBody
    public String datagrid(NoticeQueryVo noticeQueryVo) {
        Page<Notice> page = new Page<Notice>(SpringMVCHolder.getRequest());
//        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
//        String userId = sessionInfo.getUserId();// 发布人ID
//        if (NoticeUtils.isNoticeAdmin(userId)) {
//            userId = null;// 管理员 查询所有
//        }
        noticeQueryVo.syncEndTime();
        page = noticeService.findPage(page, new Notice(), null, noticeQueryVo);
        Datagrid<Notice> dg = new Datagrid<Notice>(page.getTotalCount(), page.getResult());
        String json = JsonMapper.getInstance().toJson(dg);
        return json;
    }


    /**
     * 查看通知读取情况
     *
     * @param id 通知ID
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"readInfo/{id}"})
    public ModelAndView readInfo(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/notice-readInfo");
        modelAndView.addObject("noticeId", id);
        return modelAndView;
    }


    /**
     * 查看通知
     *
     * @param id 通知ID
     * @return
     * @throws Exception
     */
    @Mobile(value = MobileValue.ALL)
    @GetMapping(value = {"view/{id}"})
    public ModelAndView view(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/notice-view");
        List<File> files = DiskUtils.findFilesByIds(noticeService.findFileIdsByNoticeId(id));
        Notice model = noticeService.get(id);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            NoticeReceiveInfo receiveInfo = noticeReceiveInfoService.getUserNotice(sessionInfo.getUserId(), id);
            if (receiveInfo != null) {
                noticeReceiveInfoService.updateReadById(receiveInfo.getId());
            }
        }
        modelAndView.addObject("files", files);
        modelAndView.addObject("model", model);
        return modelAndView;
    }


    /**
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"input"})
    public ModelAndView input(@ModelAttribute("model") Notice model,
                              @RequestParam(value = "receiveUserIds", required = false) List<String> fileIds,
                              @RequestParam(value = "receiveUserIds", required = false) List<String> receiveUserIds,
                              @RequestParam(value = "receiveOrganIds", required = false) List<String> receiveOrganIds,
                              @RequestParam(value = "receiveContactGroupIds", required = false) List<String> receiveContactGroupIds,
                              OperateType operateType) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/notice-input");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String loginUserId = sessionInfo.getUserId();
        List<File> files = Collections.emptyList();
        List<String> _fileIds = Collections.emptyList();
        List<String> _receiveUserIds = Collections.emptyList();
        List<String> _receiveOrganIds = Collections.emptyList();
        List<String> _receiveContactGroupIds = Collections.emptyList();
        if (OperateType.Repeat.equals(operateType)) {// 转发
            List<String> sourceFileIds = noticeService.findFileIdsByNoticeId(model.getId());
            files = DiskUtils.copyFiles(loginUserId, Notice.FOLDER_NOTICE, sourceFileIds);
            model = model.repeat();
            fileIds = DiskUtils.toFileIds(files);
            model.setFileIds(fileIds);
        } else if (!model.getIsNewRecord()) {//修改
            fileIds = noticeService.findFileIdsByNoticeId(model.getId());
            model.setFileIds(fileIds);
            files = DiskUtils.findFilesByIds(fileIds);

            _receiveUserIds = noticeSendInfoService.findUserIdsByNoticeId(model.getId());
            _receiveOrganIds = noticeSendInfoService.findOrganIdsByNoticeId(model.getId());
            _receiveContactGroupIds = noticeSendInfoService.findContactGroupIdsByNoticeId(model.getId());

        } else if (model.getIsNewRecord()) {//新增
            _fileIds = fileIds;
            model.setFileIds(fileIds);
            files = DiskUtils.findFilesByIds(fileIds);
            _receiveUserIds = receiveUserIds;
            _receiveOrganIds = receiveOrganIds;
            _receiveContactGroupIds = receiveContactGroupIds;
            if (StringUtils.isNotBlank(model.getObjectId())) {
                model.setReceiveScope(NoticeReceiveScope.CUSTOM.getValue());
            }

        }
        modelAndView.addObject("files", files);
        modelAndView.addObject("fileIds", _fileIds);
        modelAndView.addObject("receiveUserIds", _receiveUserIds);
        modelAndView.addObject("receiveOrganIds", _receiveOrganIds);
        modelAndView.addObject("receiveContactGroupIds", _receiveContactGroupIds);
        modelAndView.addObject("operateType", operateType);
        modelAndView.addObject("model", model);
//        modelAndView.addObject("noticeTipChannels", MessageChannel.values());
        modelAndView.addObject("noticeTipChannels", NoticeConstants.getNoticeTipChannels());
        return modelAndView;
    }


    /**
     * @param query      关键字
     * @param includeIds 包含的ID
     * @param dataScope  {@link DataScope}
     * @return
     */
    @PostMapping(value = {"multiSelectPrefix"})
    @ResponseBody
    public String multiSelectPrefix(String query, String dataScope,
                                    @RequestParam(value = "includeIds", required = false) List<String> includeIds) {
        List<User> list = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if ((StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.COMPANY.getValue()))) {
            list = userService.findUsersByCompanyId(sessionInfo.getLoginCompanyId());
            if (Collections3.isNotEmpty(includeIds)) {
                List<User> includeUsers = userService.findByIds(includeIds);
                list = Collections3.aggregate(list, includeUsers);
            }
        } else {
            list = userService.findWithInclude(includeIds, query);
        }
        return JsonMapper.getInstance().toJson(list, User.class, new String[]{"id", "name", "defaultOrganName"});
    }

    @Override
    protected void initBinder(WebDataBinder binder) {
        super.defaultWebDataBinder(binder);
    }

    /**
     * 保存
     *
     * @param model
     * @param operateType    {@link OperateType}
     * @param noticeUserIds
     * @param noticeOrganIds
     * @param fileIds        页面文件ID集合
     * @return
     */
    @RequiresPermissions("notice:publish")
    @PostMapping(value = {"save"})
    @ResponseBody
    public Result save(@ModelAttribute("model") Notice model, OperateType operateType,
                       @RequestParam(value = "_noticeUserIds", required = false) List<String> noticeUserIds,
                       @RequestParam(value = "_noticeOrganIds", required = false) List<String> noticeOrganIds,
                       @RequestParam(value = "_noticeContactGroupIds", required = false) List<String> noticeContactGroupIds,
                       @RequestParam(value = "fileIds", required = false) List<String> fileIds) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Result result;
        if (StringUtils.isBlank(model.getUserId())) {
            model.setUserId(sessionInfo.getUserId());
            model.setOrganId(sessionInfo.getLoginOrganId());
        }
        boolean isPub = OperateType.Publish.equals(operateType);
        noticeService.saveNoticeAndFiles(model, null, noticeUserIds, noticeOrganIds, noticeContactGroupIds, fileIds);
        if (isPub) {
            noticeService.publish(model);
        }
        result = Result.successResult();
        return result;
    }


    /**
     * 推送（仅限推送,由切面实现）
     *
     * @param model
     * @return
     */
    @RequiresPermissions("notice:publish")
    @PostMapping(value = {"push"})
    @ResponseBody
    public Result save(@ModelAttribute("model") Notice model) {
        noticeService.push(model.getId());
        return Result.successResult();
    }


    /**
     * 标记为已读
     *
     * @param ids
     * @return
     */
    @PostMapping(value = {"markReaded"})
    @ResponseBody
    public Result markReaded(@RequestParam(value = "ids", required = false) List<String> ids) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        noticeReceiveInfoService.markUserNoticeReaded(sessionInfo.getUserId(), ids);
        return Result.successResult();
    }

    /**
     * 标记为已读(全部)
     *
     * @return
     */
    @PostMapping(value = {"markReadedAll"})
    @ResponseBody
    public Result markReadedAll() {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        noticeReceiveInfoService.markUserNoticeReaded(sessionInfo.getUserId());
        return Result.successResult();
    }

    /**
     * 发布通知
     *
     * @param id 通知ID
     * @return
     */
    @RequiresPermissions("notice:publish")
    @PostMapping(value = {"publish/{id}"})
    @ResponseBody
    public Result publish(@PathVariable String id) {
        noticeService.publish(id);
        return Result.successResult();
    }

    /**
     * 终止通知
     *
     * @param id 通知ID
     * @return
     */
    @RequiresPermissions("notice:publish")
    @PostMapping(value = {"invalid/{id}"})
    @ResponseBody
    public Result invalid(@PathVariable String id) {
        Notice notice = noticeService.get(id);
        notice.setBizMode(NoticeMode.Invalidated.getValue());
        notice.setInvalidTime(Calendar.getInstance().getTime());
        noticeService.save(notice);
        return Result.successResult();
    }

    /**
     * @param ids
     * @return
     */
    @RequiresPermissions("notice:publish")
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        noticeService.deleteByIds(ids);
        return Result.successResult();
    }

    /**
     * 文件上传
     */
    @PostMapping(value = {"upload"})
    @ResponseBody
    public Result upload(@RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile, String jsessionid) {
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getSessionInfo(jsessionid);
        sessionInfo = null != sessionInfo ? sessionInfo : SecurityUtils.getCurrentSessionInfo();
        Exception exception = null;
        File file = null;
        try {
            file = DiskUtils.saveSystemFile(Notice.FOLDER_NOTICE, sessionInfo.getUserId(), multipartFile);
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
                logger.error(exception.getMessage(), exception);
                if (file != null) {
                    DiskUtils.deleteFile(file.getId());
                }
            }
        }
        return result;

    }


    /**
     * 删除附件
     *
     * @param model
     * @param fileId
     * @return
     */
    @PostMapping(value = {"delUpload"})
    @ResponseBody
    public Result delUpload(@ModelAttribute("model") Notice model, @RequestParam String fileId) {
        List<String> fileIds = new ArrayList<>(1);
        fileIds.add(fileId);
        noticeService.deleteNoticeFiles(model.getId(), fileIds);
        DiskUtils.deleteFile(fileId);
        return Result.successResult();
    }

    /**
     * 是否置顶 下拉列表
     *
     * @param selectType
     * @return
     */
    @PostMapping(value = {"isTopCombobox"})
    @ResponseBody
    public List<Combobox> isTopCombobox(String selectType) {
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        IsTop[] _emums = IsTop.values();
        for (IsTop column : _emums) {
            Combobox combobox = new Combobox(column.getValue(), column.getDescription());
            cList.add(combobox);
        }
        return cList;
    }


    /**
     * 我的通知
     *
     * @return
     */
    @GetMapping(value = {"read"})
    public ModelAndView readList() {
        ModelAndView modelAndView = new ModelAndView("modules/notice/notice-read");
        return modelAndView;
    }


    /**
     * 通知数量
     *
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"myMessage"})
    @ResponseBody
    public Result myMessage(HttpServletRequest request, HttpServletResponse response) {
        WebUtils.setNoCacheHeader(response);
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        long noticeScopes = 0;
        Page<NoticeReceiveInfo> page = new Page<>(request);
        page = noticeReceiveInfoService.findUserUnreadNotices(page, sessionInfo.getUserId());
        if (Collections3.isNotEmpty(page.getResult())) {
            noticeScopes = page.getTotalCount();
        }
        Map<String, Long> map = Maps.newHashMap();
        map.put("noticeScopes", noticeScopes);
        result = Result.successResult().setObj(map);
        return result;
    }

    /**
     * 未读通知列表
     *
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"myUnreadNotice"})
    @ResponseBody
    public String myUnreadNotice(HttpServletRequest request, HttpServletResponse response) {
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Page<NoticeReceiveInfo> page = new Page<>(request);
        page = noticeReceiveInfoService.findUserUnreadNotices(page, sessionInfo.getUserId());
        result = Result.successResult().setObj(page.getResult());
        String json = JsonMapper.getInstance().toJson(result, NoticeReceiveInfo.class,
                new String[]{"noticeId", "title"});
        return json;
    }

    /**
     * 接收范围选择
     *
     * @param selectType
     * @return
     * @throws Exception
     */
    @PostMapping(value = {"receiveScopeCombobox"})
    @ResponseBody
    public List<Combobox> receiveScopeCombobox(String selectType) throws Exception {
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        NoticeReceiveScope[] _emums = NoticeReceiveScope.values();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();
        for (NoticeReceiveScope column : _emums) {
            if (!isAdmin && column.getValue().equals(NoticeReceiveScope.ALL.getValue())) {
                continue;
            }
            Combobox combobox = new Combobox(column.getValue(), column.getDescription());
            cList.add(combobox);
        }
        return cList;
    }

    /**
     * 明细信息
     *
     * @param model
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") Notice model) {
        return Result.successResult().setObj(model);
    }

}
