/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.disk.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.PrettyMemoryUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.DownloadUtils;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.disk.mapper.Folder;
import com.eryansky.modules.sys.utils.DownloadFileUtils;
import com.eryansky.utils.AppUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.disk._enum.FileSizeType;
import com.eryansky.modules.disk._enum.FolderAuthorize;
import com.eryansky.modules.disk.service.*;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.utils.SelectType;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 我的云盘 管理 包含：文件夹的管理 文件的管理
 *
 * @author Eryan
 * @date 2014-11-22
 */
@Controller
@RequestMapping(value = "${adminPath}/disk")
public class DiskController extends SimpleController {

    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;

    public static final String NODE_TYPE = "nType";
    public static final String NODE_OPERATE = "operate";
    public static final String NODE_USERNAME = "userName";

    public static final String ICON_FOLDER = "easyui-icon-folder";
    public static final String ICON_DISK = "eu-icon-disk_yunpan";

    /**
     * 磁盘树 节点类型
     */
    public enum NType {
        FolderAuthorize, Folder;
    }

    public enum ModelType {
        Folder, File;
    }

    @ModelAttribute
    public void getModel(ModelType modelType, String id, Model uiModel) {
        if (modelType != null && StringUtils.isNotBlank(id)) {
            if (modelType.equals(ModelType.Folder)) {
                uiModel.addAttribute("model", folderService.get(id));
            } else if (modelType.equals(ModelType.File)) {
                uiModel.addAttribute("model", fileService.get(id));
            }
        }

    }

    /**
     * 我的云盘
     */
    @RequiresPermissions("disk:disk:view")
    @Logging(logType = LogType.access, value = "我的云盘")
    @GetMapping(value = {""})
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("modules/disk/disk");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        modelAndView.addObject("isAdmin",DiskUtils.isDiskAdmin(sessionInfo.getUserId()));
        return modelAndView;
    }


    /**
     * 文件夹树
     *
     * @param folderAuthorize {@link FolderAuthorize}
     * @param excludeFolderId
     * @param selectType
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"folderTree"})
    @ResponseBody
    public List<TreeNode> folderTree(String folderAuthorize, String excludeFolderId, String selectType) {
        List<TreeNode> treeNodes = Lists.newArrayList();
        TreeNode selectTreeNode = SelectType.treeNode(selectType);
        if (selectTreeNode != null) {
            treeNodes.add(selectTreeNode);
        }
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        List<TreeNode> folderTreeNodes = folderService.findNormalTypeFolderTreeNodes(folderAuthorize, sessionInfo.getUserId(), excludeFolderId);
        treeNodes.addAll(folderTreeNodes);
        return treeNodes;
    }

    /**
     * 文件夾授权下拉框
     *
     * @return
     */
    @PostMapping(value = {"folderAuthorizeCombobox"})
    @ResponseBody
    public List<Combobox> folderAuthorizeCombobox(String selectType,
                                                  String requestType) {
        List<Combobox> cList = Lists.newArrayList();

        Combobox selectCombobox = SelectType.combobox(selectType);
        if (selectCombobox != null) {
            cList.add(selectCombobox);
        }

        Combobox combobox = new Combobox(FolderAuthorize.User.getValue(), FolderAuthorize.User.getDescription());
        cList.add(combobox);

        if("search".equals(requestType) || DiskUtils.isDiskAdmin(SecurityUtils.getCurrentUserId())){
            Combobox systemCombobox = new Combobox(FolderAuthorize.SysTem.getValue(), FolderAuthorize.SysTem.getDescription());
            cList.add(systemCombobox);
        }
        return cList;
    }

    /**
     * 文件大小类型下拉框
     *
     * @return
     */
    @PostMapping(value = {"fileSizeTypeCombobox"})
    @ResponseBody
    public List<Combobox> fileSizeTypeCombobox(String selectType) {
        List<Combobox> cList = Lists.newArrayList();

        Combobox selectCombobox = SelectType.combobox(selectType);
        if (selectCombobox != null) {
            cList.add(selectCombobox);
        }
        FileSizeType[] _enums = FileSizeType.values();
        for (int i = 0; i < _enums.length; i++) {
            Combobox combobox = new Combobox(_enums[i].getValue(),
                    _enums[i].getDescription());
            cList.add(combobox);
        }

        return cList;
    }

    /**
     * 保存文件夹
     *
     * @return
     */
    @Logging(logType = LogType.access, value = "我的云盘-文件夹保存")
    @PostMapping(value = {"saveFolder"})
    @ResponseBody
    public Result saveFolder(@ModelAttribute("model") Folder folder) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if(FolderAuthorize.SysTem.getValue().equals(folder.getFolderAuthorize()) && !DiskUtils.isDiskAdmin(sessionInfo.getUserId())){
            return Result.errorResult().setMsg("未授权该操作");
        }
        if (StringUtils.isBlank(folder.getUserId())) {
            folder.setUserId(SecurityUtils.getCurrentUserId());
        }
        folderService.saveFolder(folder);
        return Result.successResult();
    }

    /**
     * 删除文件夹
     *
     * @param folderId 文件夹ID
     * @return
     */
    @Logging(logType = LogType.access, value = "我的云盘-文件夹删除")
    @PostMapping(value = {"folderRemove/{folderId}"})
    @ResponseBody
    public Result folderRemove(@PathVariable String folderId) {
        Folder folder = folderService.get(folderId);
        if(null == folder){
            return Result.warnResult().setMsg("数据不存在,"+ folderId);
        }
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if(FolderAuthorize.SysTem.getValue().equals(folder.getFolderAuthorize()) && !DiskUtils.isDiskAdmin(sessionInfo.getUserId())){
            return Result.errorResult().setMsg("未授权该操作");
        }
        folderService.deleteFolderAndFiles(folderId);
        return Result.successResult();
    }

    /**
     * 递归用户文件夹树
     *
     * @param folder
     */
    public TreeNode folderToTreeNode(Folder folder) {
        TreeNode treeNode = new TreeNode(folder.getId(), folder.getName());
        treeNode.addAttribute(DiskController.NODE_TYPE, DiskController.NType.Folder.toString());
        treeNode.addAttribute("folderAuthorize", folder.getFolderAuthorize());
        treeNode.addAttribute(DiskController.NODE_OPERATE, true);
        treeNode.setIconCls(ICON_FOLDER);
        treeNode.setpId(folder.getParentId());
        if (StringUtils.isBlank(treeNode.getpId())) {
            FolderAuthorize folderAuthorize = FolderAuthorize.getByValue(folder.getFolderAuthorize());
            if (null != folderAuthorize) {
                treeNode.setpId(folderAuthorize.getValue());
            }
//            if (FolderAuthorize.SysTem.equals(folderAuthorize)) {
//                treeNode.setText(treeNode.getText()+"_"+folder.getUserName());
//            }
        }
        return treeNode;
    }


    /**
     * 磁盘树
     *
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"diskTree"})
    @ResponseBody
    public List<TreeNode> diskTree() {
        List<TreeNode> treeNodes = Lists.newArrayList(); // 返回的树节点
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String loginUserId = sessionInfo.getUserId(); // 登录人Id

        TreeNode userOwnerTreeNode = new TreeNode(FolderAuthorize.User.getValue(), FolderAuthorize.User.getDescription());
        userOwnerTreeNode.addAttribute(NODE_TYPE, NType.FolderAuthorize.toString());
        userOwnerTreeNode.addAttribute("folderAuthorize", FolderAuthorize.User.getValue());
        userOwnerTreeNode.setIconCls(ICON_DISK);
        treeNodes.add(userOwnerTreeNode);
        boolean isAdmin = DiskUtils.isDiskAdmin(sessionInfo.getUserId());
        if(isAdmin){
            TreeNode systemTreeNode = new TreeNode(FolderAuthorize.SysTem.getValue(), FolderAuthorize.SysTem.getDescription());
            systemTreeNode.addAttribute(NODE_TYPE, NType.FolderAuthorize.toString());
            systemTreeNode.addAttribute("folderAuthorize", FolderAuthorize.SysTem.getValue());
            systemTreeNode.setIconCls(ICON_DISK);
            treeNodes.add(systemTreeNode);
        }

        List<Folder> userFolders = isAdmin ? folderService.findNormalTypeAndSystemFoldersByUserId(loginUserId) : folderService.findNormalTypeFoldersByUserId(loginUserId);

        List<TreeNode> userFolderTreeNodes = userFolders.parallelStream().map(this::folderToTreeNode).collect(Collectors.toList());
        treeNodes.addAll(userFolderTreeNodes);
        return AppUtils.toTreeTreeNodes(treeNodes);
    }

    /**
     * 文件列表
     *
     * @param folderId        文件夹Id
     * @param folderAuthorize 文件夹隶属云盘类型
     * @param fileName
     * @return
     */
    @PostMapping(value = {"folderFileDatagrid"})
    @ResponseBody
    public String folderFileDatagrid(String folderId, String folderAuthorize, String fileName) {
        String json = null;
        List<Map<String, Object>> footer = Lists.newArrayList();
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String loginUserId = sessionInfo.getUserId(); // 登录人Id

        if (StringUtils.isBlank(folderId) && StringUtils.isBlank(folderAuthorize)) {
            json = JsonMapper.getInstance().toJson(new Datagrid());
        } else {
            Page<File> page = new Page<>(SpringMVCHolder.getRequest());
            Folder _folder;
            File entity = new File();
            if(FolderAuthorize.SysTem.getValue().equals(folderAuthorize)){//系统云盘
                 _folder = StringUtils.isNotBlank(folderId) ? new Folder(folderId) : folderService.initHideFolderAndSaveForSystem(loginUserId);
            }else{//我的云盘
                _folder = StringUtils.isNotBlank(folderId) ? new Folder(folderId) : folderService.initHideFolderAndSaveForUser(loginUserId);
                entity.setUserId(loginUserId);
            }
            entity.setQuery(fileName);
            entity.setFolderId(_folder.getId());
            page = fileService.findPage(page, entity);

            Datagrid<File> dg = new Datagrid<>(page.getTotalCount(),
                    page.getResult());
            long totalSize = page.getResult().parallelStream().mapToLong(File::getFileSize).sum();
            Map<String, Object> map = Maps.newHashMap();
            map.put("name", "总大小");
            map.put("prettyFileSize", PrettyMemoryUtils.prettyByteSize(totalSize));
            footer.add(map);
            dg.setFooter(footer);
            json = JsonMapper.getInstance().toJson(
                    dg,
                    File.class,
                    new String[]{"id", "fileId", "name", "prettyFileSize", "updateTime", "userName"});
        }

        return json;
    }

    /**
     * 文件夹编辑页面
     *
     * @param folderId
     * @param folderAuthorize {@link FolderAuthorize}
     * @param parentFolderId
     * @return
     */
    @GetMapping(value = {"folderInput"})
    public ModelAndView folderInput(String folderId, Integer folderAuthorize,
                                    String parentFolderId) {
        ModelAndView modelAndView = new ModelAndView(
                "modules/disk/disk-folderInput");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Folder model = new Folder();
        if (folderId != null) {
            model = folderService.get(folderId);
        }

        modelAndView.addObject("model", model);
        modelAndView.addObject("folderAuthorize", folderAuthorize);
        if (StringUtils.isNotBlank(parentFolderId)) {// 不允许在别人的文件夹下创建文件夹
            Folder parentFolder = folderService.get(parentFolderId);
            if (!parentFolder.getUserId().equals(sessionInfo.getUserId())) {
                parentFolderId = null;
            }
        }
        modelAndView.addObject("parentFolderId", parentFolderId);
        return modelAndView;
    }

    /**
     * 文件上传页面
     *
     * @param folderId        文件夹Id
     * @param folderAuthorize 云盘类型Id
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"fileInput"})
    public ModelAndView fileInput(String folderId, String folderAuthorize) throws Exception {
        ModelAndView modelAndView = new ModelAndView(
                "modules/disk/disk-fileInput");
        Folder model = null;
        if (FolderAuthorize.User.getValue().equals(folderId) || FolderAuthorize.User.getValue().equals(folderAuthorize)) {
            String loginUserId = SecurityUtils.getCurrentUserId();
            model = folderService.initHideFolderAndSaveForUser(loginUserId);
        }else if (FolderAuthorize.SysTem.getValue().equals(folderId) || FolderAuthorize.SysTem.getValue().equals(folderAuthorize)) {
            String loginUserId = SecurityUtils.getCurrentUserId();
            model = folderService.initHideFolderAndSaveForSystem(loginUserId);
        } else if (StringUtils.isNotBlank(folderId)) { // 选中文件夹
            model = folderService.get(folderId);
        } else {
            Exception e = new ActionException("上传文件异常！请联系管理员。");
            throw e;
        }

        modelAndView.addObject("model", model);
        return modelAndView;
    }

    /**
     * 文件信息修改
     *
     * @return
     */
    @Logging(logType = LogType.access, value = "我的云盘-文件修改")
    @PostMapping(value = {"fileSave"})
    @ResponseBody
    public Result fileSave(@ModelAttribute("model") File file) {
        fileService.save(file);
        return Result.successResult();
    }

    /**
     * 文件删除
     *
     * @param fileIds 文件Id集合
     * @return
     */
    @Logging(logType = LogType.access, value = "我的云盘-文件删除")
    @PostMapping(value = {"delFolderFile"})
    @ResponseBody
    public Result delFolderFile(@RequestParam(value = "fileIds", required = false) List<String> fileIds) {
        fileService.deleteFileByFileIds(fileIds);
        return Result.successResult();
    }

    /**
     * 文件级联删除
     *
     * @param fileCodes 文件code集合
     * @throws Exception
     */
    @Logging(logType = LogType.access, value = "我的云盘-文件级联删除")
    @PostMapping(value = {"cascadeDelFile"})
    @ResponseBody
    public Result cascadeDelFile(@RequestParam(value = "fileCodes", required = false) List<String> fileCodes) {
        fileService.deleteFileByFolderCode(fileCodes);
        return Result.successResult();
    }


    /**
     * 文件上传
     *
     * @param folderId   文件夹
     * @param uploadFile 上传文件
     * @return
     */
    @PostMapping(value = {"fileUpload"})
    @ResponseBody
    public Result fileUpload(
            @RequestParam(value = "folderId", required = false) String folderId,
            @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile)
            throws Exception {
        Result result = Result.errorResult();
        if (StringUtils.isBlank(folderId)) {
            result.setMsg("文件夹Id丢失！");
        } else if (uploadFile == null) {
            result.setMsg("上传文件丢失！");
        } else {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            Folder folder = folderService.get(folderId);
            if (folder != null) {
                File file = fileService.fileUpload(sessionInfo, folder, uploadFile);
                String obj = null;
                if (file != null) {
                    obj = file.getId();
                }
                result = Result.successResult().setObj(obj);
            } else {
                result.setMsg("文件夹不存在，已被删除或移除！");
            }
        }
        return result;
    }

    /**
     * 文件检索
     */
    @RequiresPermissions("disk:disk:search")
    @Logging(logType = LogType.access, value = "我的云盘-文件检索")
    @GetMapping(value = {"search"})
    public ModelAndView searchList() {
        boolean isAdmin = DiskUtils.isDiskAdmin(SecurityUtils.getCurrentUserId());
        ModelAndView modelAndView = new ModelAndView("modules/disk/disk-search");
        modelAndView.addObject("isAdmin", isAdmin);
        return modelAndView;
    }


    /**
     * 文件检索
     *
     * @param query        关键字
     * @param folderAuthorize 云盘类型
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param personIds       上传人Id集合
     * @return
     */
    @PostMapping(value = {"fileSearchDatagrid"})
    @ResponseBody
    public String fileSearchDatagrid(
            String query,
            String folderAuthorize,
            String sizeType,
            Date startTime,
            Date endTime,
            @RequestParam(value = "personIds", required = false) List<String> personIds) {
        String json = JsonMapper.getInstance().toJson(new Datagrid());
        String userId = SecurityUtils.getCurrentUserId(); // 登录人Id
        boolean isAdmin = DiskUtils.isDiskAdmin(userId); // 是否是云盘管理员
        if (isAdmin) {
            userId = null;
        }
        userId = Collections3.isNotEmpty(personIds) ? personIds.get(0) : userId;
        Page<File> page = new Page<>(SpringMVCHolder.getRequest());
        page = fileService.searchFilePage(page, userId, query,
                folderAuthorize, sizeType,isAdmin, startTime, endTime);
        if (page != null) {
            Datagrid<File> dg = new Datagrid<>(page.getTotalCount(),
                    page.getResult());
            json = JsonMapper.getInstance().toJson(
                    dg,
                    File.class,
                    new String[]{"id", "name", "code", "prettyFileSize", "location", "createTime", "updateTime", "userName"});
        }
        return json;

    }


    /**
     * 文件下载
     *
     * @param response
     * @param request
     * @param fileId   文件ID
     */
    @RequiresUser(required = false)
    @Logging(logType = LogType.access, value = "下载文件")
    @GetMapping(value = {"fileDownload/{fileId}"})
    public ModelAndView fileDownload(HttpServletResponse response,
                                     HttpServletRequest request, @PathVariable String fileId) throws Exception {
        File file = fileService.get(fileId);
        try {
            return downloadSingleFileUtil(response, request, file);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            DownloadFileUtils.loggerHTTPHeader(request,response);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
//            throw e;
        }

    }


    /**
     * 文件下载
     *
     * @param response
     * @param request
     * @param fileId   文件ID
     */
    @Logging(logType = LogType.access, value = "下载文件")
    @GetMapping(value = {"innerFileDownload/{fileId}"})
    public ModelAndView innerFileDownload(HttpServletResponse response,
                                     HttpServletRequest request, @PathVariable String fileId) throws Exception {
        File file = fileService.get(fileId);
        try {
            return downloadSingleFileUtil(response, request, file);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            DownloadFileUtils.loggerHTTPHeader(request,response);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
//            throw e;
        }

    }


    private ModelAndView downloadSingleFileUtil(HttpServletResponse response,
                                                HttpServletRequest request, File file) throws Exception {
        ActionException fileNotFoldException = new ActionException("文件不存在，已被删除或移除。");
        if (file == null) {
//            throw fileNotFoldException;
            // 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        java.io.File diskFile = file.getDiskFile();
        if (!diskFile.exists() || !diskFile.canRead()) {
            logger.error("{}:{}",file.getId(),fileNotFoldException.getMessage());
//            throw fileNotFoldException;
            // 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        DownloadFileUtils.downRangeFile(diskFile,file.getName(),response,request);
        return null;
    }

    /**
     * 文件下载
     *
     * @param fileIds 入参Ids拼接字符串
     * @throws Exception
     */
    @Logging(logType = LogType.access, value = "下载文件")
    @GetMapping(value = {"downloadDiskFile"})
    public ModelAndView downloadDiskFile(
            HttpServletResponse response,
            HttpServletRequest request,
            @RequestParam(value = "fileIds", required = false) List<String> fileIds)
            throws Exception {
        if (Collections3.isEmpty(fileIds)) {
            throw new ActionException("下载链接失效。");
        }
        if (fileIds.size() == 1) {
            File file = fileService.get(fileIds.get(0));
            try {
                return downloadSingleFileUtil(response, request, file);
            } catch (Exception e) {
                logger.error("{},{}",fileIds.get(0),e.getMessage());
                throw e;
            }
        }
        List<File> fileList = fileService.findFilesByIds(fileIds);
        if (Collections3.isEmpty(fileList)) {
            throw new ActionException("下载链接失效。");
        }
        return downloadMultiFileUtil(response, request, fileList);
    }

    /**
     * 批量下载文件导出ZIP包
     *
     * @param response
     * @param request
     * @param fileList 文件对象集合
     * @throws Exception
     */
    private ModelAndView downloadMultiFileUtil(HttpServletResponse response,
                                               HttpServletRequest request, List<File> fileList) throws Exception {
        java.io.File tempZipFile = null;
        try {
            // 创建一个临时压缩文件， 文件流全部注入到这个文件中
            tempZipFile = new java.io.File(Identities.uuid() + "_temp.zip");
            DiskUtils.makeZip(fileList, tempZipFile.getAbsolutePath());
            String dName = "【批量下载】" + StringUtils.substringBeforeLast(FilenameUtils.getName(fileList.get(0).getName()),".") + "等.zip";
            DownloadFileUtils.downRangeFile(tempZipFile,dName,response,request);
//            DownloadUtils.download(request, response, new FileInputStream(tempZipFile), dName);
        } catch (Exception e) {
            throw e;
        } finally {
            if (tempZipFile != null && tempZipFile.isFile()) {
                tempZipFile.delete();//删除临时Zip文件
            }
        }
        return null;
    }


    /**
     * 清空缓存目录 正在运行时 慎用
     *
     * @return
     */
    @Logging(logType = LogType.access, value = "我的云盘-清空缓存目录")
    @PostMapping(value = {"clearTempDir"})
    @ResponseBody
    public Result clearTempDir() {
        logger.info("清空缓存目录...");
        DiskUtils.clearTempDir();
        logger.info("清空缓存目录完毕");
        return Result.successResult();
    }


}