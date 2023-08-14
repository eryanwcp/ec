/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.*;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.encode.Encryption;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security._enum.Logical;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.security.annotation.RequiresRoles;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.sys.mapper.*;
import com.eryansky.modules.sys.utils.DictionaryUtils;
import com.eryansky.modules.sys.utils.PostUtils;
import com.google.common.collect.Lists;
import com.eryansky.core.excels.ExcelUtils;
import com.eryansky.core.excels.JsGridReportBase;
import com.eryansky.core.excels.TableData;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.sys._enum.*;
import com.eryansky.modules.sys.service.*;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.SelectType;
import org.apache.commons.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户User管理 Controller层.
 *
 * @author Eryan
 * @date 2013-3-21 上午12:20:13
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "${adminPath}/sys/user")
public class UserController extends SimpleController {


    @Autowired
    private UserService userService;
    @Autowired
    private OrganService organService;
    @Autowired
    private PostService postService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private UserPasswordService userPasswordService;

    @ModelAttribute("model")
    public User get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return userService.get(id);
        } else {
            return new User();
        }
    }

    @RequiresPermissions("sys:user:view")
    @Logging(value = "用户管理", logType = LogType.access)
    @GetMapping(value = {""})
    public String list() {
        return "modules/sys/user";
    }


    /**
     * 自定义查询
     *
     * @return
     */
    @PostMapping(value = {"datagrid"})
    @ResponseBody
    public Datagrid<User> datagrid(String organId, String query, String userType) {
        Page<User> page = new Page<>(SpringMVCHolder.getRequest());
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (StringUtils.isBlank(organId)) {
            organId = sessionInfo.getLoginOrganId();
//            if(sessionInfo.isSuperUser() || SecurityUtils.isPermittedMaxRoleDataScope()){
//                organId = sessionInfo.getLoginHomeCompanyId();
//            }else if(SecurityUtils.isPermittedDataScope(sessionInfo.getUserId(),DataScope.HOME_COMPANY_AND_CHILD.getValue()) || SecurityUtils.isPermittedDataScope(sessionInfo.getUserId(),DataScope.HOME_COMPANY.getValue())){
//                organId = sessionInfo.getLoginHomeCompanyId();
//            }else if(SecurityUtils.isPermittedDataScope(sessionInfo.getUserId(),DataScope.COMPANY_AND_CHILD.getValue()) || SecurityUtils.isPermittedDataScope(sessionInfo.getUserId(),DataScope.COMPANY.getValue())){
//                organId = sessionInfo.getLoginCompanyId();
//            }else{
//                organId = sessionInfo.getLoginOrganId();
//            }
        }

        page = userService.findPage(page, organId, query, userType);
        Datagrid<User> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        return dg;
    }

    /**
     * @param model
     * @return
     */
    @GetMapping(value = {"input"})
    public ModelAndView input(@ModelAttribute("model") User model) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/user-input");
        List<Combobox> userTypes = Lists.newArrayList();
        Arrays.asList(UserType.values()).forEach(v->userTypes.add(new Combobox(v.getValue(),v.getDescription())));
        List<DictionaryItem> dictionaryItems = DictionaryUtils.getDictList(User.DIC_USER_TYPE);
        dictionaryItems.forEach(v-> {
            userTypes.add(new Combobox(v.getCode(), v.getName()));
        });
        modelAndView.addObject("userTypes", userTypes);
        return modelAndView;
    }

    /**
     * 性别下拉框
     *
     */
    @PostMapping(value = {"sexTypeCombobox"})
    @ResponseBody
    public List<Combobox> sexTypeCombobox(String selectType) {
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        SexType[] _enums = SexType.values();
        for (int i = 0; i < _enums.length; i++) {
            SexType e = _enums[i];
            Combobox combobox = new Combobox(e.getValue(), e.getDescription());
            cList.add(combobox);
        }
        return cList;
    }


    /**
     * 保存.
     */
    @RequiresPermissions("sys:user:edit")
    @Logging(value = "用户管理-保存用户", logType = LogType.access)
    @PostMapping(value = {"save"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result save(@ModelAttribute("model") User user) {
        Result result = null;
        // 名称重复校验
        User nameCheckUser = userService.getUserByLoginName(user.getLoginName());
        if (nameCheckUser != null && !nameCheckUser.getId().equals(user.getId())) {
            result = new Result(Result.WARN, "登录名为[" + user.getLoginName() + "]已存在,请修正!", "loginName");
            logger.debug(result.toString());
            return result;
        }

        if (StringUtils.isBlank(user.getId())) {// 新增
            if(!SecurityUtils.isPermitted("sys:user:add")){
                result = new Result(Result.ERROR, "未授权新增账号权限!", null);
                logger.warn(result.toString());
                return result;
            }
            try {
                user.setOriginalPassword(Encryption.encrypt(user.getPassword()));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            user.setPassword(Encrypt.e(user.getPassword()));
        } else {// 修改
            User superUser = userService.getSuperUser();
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            if (superUser.getId().equals(user.getId()) && null != sessionInfo && !sessionInfo.getUserId().equals(superUser.getId())) {
                result = new Result(Result.ERROR, "超级用户信息仅允许自己修改!", null);
                logger.debug(result.toString());
                return result;
            }
        }

        userService.saveUser(user);
        result = Result.successResult();
        logger.debug(result.toString());
        return result;
    }


    @RequiresPermissions("sys:user:edit")
    @Logging(value = "用户管理-删除用户", logType = LogType.access)
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        Result result;
        userService.deleteByIds(ids);
        result = Result.successResult();
        logger.debug(result.toString());
        return result;
    }

    /**
     * 重置用户密码页面.
     */
    @GetMapping(value = {"password"})
    public String password(@ModelAttribute("model") User model) {
        return "modules/sys/user-password";

    }



    /**
     * 修改用户密码 批量、无需输入原密码.
     *
     * @param userIds     用户ID集合
     * @param newPassword 新密码
     * @return
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:user:edit","sys:user:password:edit"})
    @Logging(value = "用户管理-重置密码", logType = LogType.access)
    @PostMapping(value = {"passwordReset"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result passwordReset(@RequestParam(value = "userIds", required = true) List<String> userIds,
                                     @RequestParam(value = "newPassword", required = true) String newPassword){
        UserUtils.updateUserPasswordReset(userIds, newPassword);
        return Result.successResult();
    }


    /**
     * 修改用户角色页面.
     */
    @GetMapping(value = {"role"})
    public ModelAndView role(@ModelAttribute("model") User model) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/user-role");
        modelAndView.addObject("model", model);
        List<String> roleIds = roleService.findRoleIdsByUserId(model.getId());
        modelAndView.addObject("roleIds", roleIds);
        return modelAndView;
    }

    /**
     * 修改用户角色.
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:user:edit","sys:user:role:edit","sys:role:user:edit"})
    @Logging(value = "用户管理-用户角色", logType = LogType.access)
    @PostMapping(value = {"updateUserRole"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result updateUserRole(@RequestParam(value = "userIds", required = false) Set<String> userIds,
                                 @RequestParam(value = "roleIds", required = false) Set<String> roleIds) {
        userService.updateUserRole(userIds, roleIds);
        userService.clearCache();
        return Result.successResult();
    }

    /**
     * 设置组织机构页面.
     */
    @GetMapping(value = {"organ"})
    public String organ(@ModelAttribute("model") User model, Model uiModel) {
        //设置默认组织机构初始值
        List<Combobox> defaultOrganCombobox = Lists.newArrayList();
        if (model.getId() != null) {
            List<Organ> organs = organService.findOrgansByUserId(model.getId());
            defaultOrganCombobox = organs.parallelStream().map(organ -> new Combobox(organ.getId(), organ.getName())).collect(Collectors.toList());
        }
        String defaultOrganComboboxData = JsonMapper.nonDefaultMapper().toJson(defaultOrganCombobox);
        logger.debug(defaultOrganComboboxData);
        uiModel.addAttribute("defaultOrganComboboxData", defaultOrganComboboxData);
//        uiModel.addAttribute("organIds", Collections3.extractToList(defaultOrganCombobox,"value"));
        uiModel.addAttribute("organIds", Collections3.extractToString(defaultOrganCombobox, "value", ","));
        uiModel.addAttribute("model", model);
        return "modules/sys/user-organ";
    }

    /**
     * 设置用户机构 批量更新用户 信息
     *
     * @param userIds        用户Id集合
     * @param organIds       所所机构ID集合
     * @param defaultOrganId 默认机构
     * @return
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:user:edit","sys:organ:user:edit","sys:user:organ:edit"})
    @Logging(value = "用户管理-用户机构", logType = LogType.access)
    @PostMapping(value = {"updateUserOrgan"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result updateUserOrgan(@RequestParam(value = "userIds", required = false) Set<String> userIds,
                                  @RequestParam(value = "organIds", required = false) Set<String> organIds, String defaultOrganId) {
        userService.updateUserOrgan(userIds, organIds, defaultOrganId);
        userService.clearCache();
        return Result.successResult();

    }

    /**
     * 设置用户岗位页面.
     */
    @GetMapping(value = {"post"})
    public String post(@ModelAttribute("model") User model, Model uiModel) {
        uiModel.addAttribute("organId", model.getDefaultOrganId());
        List<String> postIds = postService.findPostIdsByUserId(model.getId(),model.getDefaultOrganId());
        uiModel.addAttribute("postIds", postIds);
        uiModel.addAttribute("model", model);
        return "modules/sys/user-post";
    }

    /**
     * 修改用户岗位.
     *
     * @param model 用户
     * @param postIds 岗位ID集合
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:user:edit","sys:user:post:edit","sys:post:user:edit"})
    @Logging(value = "用户管理-用户岗位", logType = LogType.access)
    @PostMapping(value = {"updateUserPost"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result updateUserPost(@ModelAttribute("model") User model,
                                 @RequestParam(value = "postIds", required = false) Set<String> postIds) {
        userService.updateUserPost(model.getId(),model.getDefaultOrganId(), postIds);
        userService.clearCache();
        return Result.successResult();
    }

    /**
     * 修复用户岗位数据 自动清理用户不在部门的岗位信息 仅保留当前部门岗位信息
     * @return
     */
    @Logging(value = "用户管理-修复用户岗位数据", logType = LogType.access)
    @GetMapping("fixUserPostData")
    @ResponseBody
    public Result fixUserPostData() {
        logger.info("清理用户不在部门的岗位信息...");
        List<User> userList = userService.findAllNormal();
        userList.forEach(v->{
            int d = userService.deleteNotInUserOrgansPostsByUserId(v.getId(), Lists.newArrayList(v.getDefaultOrganId()));
            if(d != 0){
                logger.warn("删除用户不在部门的岗位数据：{} {} {}",d,v.getId(), v.getDefaultOrganId());
            }
        });
        userService.clearCache();
        logger.info("清理用户不在部门的岗位信息结束");
        return Result.successResult();
    }



    /**
     * 修改用户资源页面.
     */
    @GetMapping(value = {"resource"})
    public String resource(@ModelAttribute("model") User model, Model uiModel) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        List<TreeNode> treeNodes = resourceService.findTreeNodeResourcesWithPermissions(sessionInfo.getUserId());
        String resourceComboboxData = JsonMapper.getInstance().toJson(treeNodes);
        uiModel.addAttribute("resourceComboboxData", resourceComboboxData);

        List<String> resourceIds = resourceService.findResourceIdsByUserId(model.getId());
        uiModel.addAttribute("model", model);
        uiModel.addAttribute("resourceIds", resourceIds);
        return "modules/sys/user-resource";
    }

    /**
     * 修改用户资源.
     *
     * @param userIds     用户ID集合
     * @param resourceIds 资源ID集合
     * @return
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:user:edit","sys:resource:user:edit","sys:user:resource:edit"})
    @Logging(value = "用户管理-用户资源", logType = LogType.access)
    @PostMapping(value = {"updateUserResource"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result updateUserResource(@RequestParam(value = "userIds", required = false) Set<String> userIds,
                                     @RequestParam(value = "resourceIds", required = false) Set<String> resourceIds) {
        userService.updateUserResource(userIds, resourceIds);
        userService.clearCache();
        return Result.successResult();

    }

    /**
     * 头像 文件上传
     *
     * @param request
     * @param multipartFile
     * @return
     */
    @PostMapping(value = {"upload"})
    @ResponseBody
    public Result upload(HttpServletRequest request,
                         @RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile) {
        Result result = null;
        try {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            File file = DiskUtils.saveSystemFile(User.FOLDER_USER_PHOTO, sessionInfo.getUserId(), multipartFile);
            result = Result.successResult().setObj(file);
        } catch (InvalidExtensionException e) {
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG);
        } catch (FileNameLengthLimitExceededException e) {
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG);
        } catch (IOException e) {
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        }
        return result;
    }


    /**
     * 用户列表
     * @param dataScope {@link DataScope}
     * @param includeUserIds
     * @param excludeUserIds
     * @param query
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = {"userList"})
    @ResponseBody
    public String userList(String dataScope,
                           @RequestParam(value = "includeUserIds", required = false) List<String> includeUserIds,
                           @RequestParam(value = "excludeUserIds", required = false) List<String> excludeUserIds,
                           String query,
                           HttpServletRequest request, HttpServletResponse response) {
        List<User> list = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (StringUtils.isBlank(dataScope)) {
            list = userService.findWithInclude(includeUserIds, query);
        } else if ((StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.ALL.getValue()))) {
            list = userService.findAllNormalWithExclude(excludeUserIds);
        } else if ((StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.HOME_COMPANY_AND_CHILD.getValue()))) {
            String organId = sessionInfo.getLoginHomeCompanyId();
            list = userService.findOwnerAndChildsUsers(organId,null, excludeUserIds);
        } else if ((StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.HOME_COMPANY.getValue()))) {
            list = userService.findUsersByCompanyId(sessionInfo.getLoginHomeCompanyId(), excludeUserIds);
        } else if ((StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.COMPANY_AND_CHILD.getValue()))) {
            String organId = sessionInfo.getLoginCompanyId();
            list = userService.findOwnerAndChildsUsers(organId,null, excludeUserIds);
        } else if ((StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.COMPANY.getValue()))) {
            list = userService.findUsersByCompanyId(sessionInfo.getLoginCompanyId(), excludeUserIds);
        } else if ((StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.OFFICE_AND_CHILD.getValue()))) {
            String organId = sessionInfo.getLoginOrganId();
            list = userService.findOwnerAndChildsUsers(organId,null, excludeUserIds);
        } else {
            String organId = sessionInfo.getLoginOrganId();
            list = userService.findOwnerAndChildsUsers(organId,null, excludeUserIds);
        }


        String json = JsonMapper.getInstance().toJson(list, User.class,
                new String[]{"id", "name", "defaultOrganName"});
        return json;
    }

    /**
     * 自定义查询用户列表
     *
     * @param dataScope
     * @param request
     * @param response
     * @param query
     * @return
     */
    //@RequiresPermissions(logical = Logical.OR,value = {"sys:user:edit","sys:user:view","sys:user:password:edit","sys:user:dataView"})
    @PostMapping(value = {"customUserList"})
    @ResponseBody
    public String userList(String dataScope,
                           HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(value = "includeUserIds", required = false) List<String> includeUserIds,
                           String query) {
        List<User> list = userService.findWithInclude(includeUserIds, query);
        String json = JsonMapper.getInstance().toJson(list, User.class,
                new String[]{"id", "name", "defaultOrganName"});
        return json;
    }

    /**
     * 获取机构用户
     *
     * @param organId 机构ID
     * @return
     */
    @PostMapping(value = {"combogridOrganUser"})
    @ResponseBody
    public String combogridOrganUser(@RequestParam(value = "organId", required = true) String organId) {
        List<User> users = userService.findOrganUsers(organId);
        Datagrid dg = new Datagrid(users.size(), users);
        return JsonMapper.getInstance().toJson(dg, User.class,
                new String[]{"id", "loginName", "name", "sexView", "defaultOrganName"});
    }


    /**
     * @param q 查询关键字
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"autoComplete"})
    @ResponseBody
    public List<String> autoComplete(String q) {
        List<String> cList = Lists.newArrayList();
        Page<User> page = new Page<>(SpringMVCHolder.getRequest());
        User entity = new User();
        entity.setQuery(q);
        page = userService.findPage(page, entity);
        for (User user : page.getResult()) {
            cList.add(user.getName());
        }
        return cList;
    }

    /**
     * 排序最大值.
     */
    @PostMapping(value = {"maxSort"})
    @ResponseBody
    public Result maxSort() {
        Result result;
        Integer maxSort = userService.getMaxSort();
        result = new Result(Result.SUCCESS, null, maxSort);
        return result;
    }

    /**
     * 机构用户树
     *
     * @param parentId
     * @param postCode 岗位编码
     * @param checkedUserIds 选中的用户ID集合
     * @param checkbox
     * @param cascade
     * @return
     */
    @GetMapping(value = {"organUserTreePage"})
    public ModelAndView organUserTreePage(String parentId,
                                          String postCode,
                                          @RequestParam(value = "checkedUserIds", required = false) List<String> checkedUserIds,
                                          @RequestParam(value = "checkbox", defaultValue = "true") Boolean checkbox,
                                          @RequestParam(value = "cascade", defaultValue = "true") Boolean cascade) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/user-tree");
//        List<TreeNode> treeNodes = organManager.findOrganUserTree(parentId, checkedUserIds,false);
        modelAndView.addObject("postCode", postCode);
        modelAndView.addObject("checkedUserIds", checkedUserIds);
        modelAndView.addObject("checkbox", checkbox);
        modelAndView.addObject("cascade", cascade);
//        modelAndView.addObject("organUserTreeData",JsonMapper.getInstance().toJson(treeNodes));
        return modelAndView;
    }


    /**
     * 查找用户机构树
     * @param parentId
     * @param postCode 岗位编码
     * @param checkedUserIds
     * @param checkbox
     * @param cascade
     * @return
     */
    //@RequiresPermissions(logical = Logical.OR,value = {"sys:user:edit","sys:user:view","sys:user:password:edit","sys:user:dataView"})
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"organUserTree"})
    @ResponseBody
    public List<TreeNode> organUserTree(String parentId,
                                        String postCode,
                                        @RequestParam(value = "checkedUserIds", required = false) List<String> checkedUserIds,
                                        @RequestParam(value = "checkbox", defaultValue = "true") Boolean checkbox,
                                        @RequestParam(value = "cascade", defaultValue = "true") Boolean cascade,
                                        @RequestParam(value = "shortName", defaultValue = "false") Boolean shortName) {
        List<TreeNode> treeNodes = shortName ? organService.findOrganUserTree(parentId, null, true, checkedUserIds, cascade, shortName) : organService.findOrganUserTree(parentId, checkedUserIds, cascade);
        Post post = null;
        if(StringUtils.isNotBlank(postCode) && StringUtils.isNotBlank(parentId) && !cascade){
            post = PostUtils.getByCode(postCode);
            if(null != post){
                List<String> postOrganIds = organService.findAssociationOrganIdsByPostId(post.getId());
                List<String> postUserIds = userService.findUserIdsByPostCode(postCode);
                return treeNodes.parallelStream().filter(v->{
                    String nType = v.getAttribute("nType");
                    if("o".equals(nType)){
                        return postOrganIds.contains(v.getId());
                    }else if("u".equals(nType)){
                        return postUserIds.contains(v.getId());
                    }
                    return false;
                }).collect(Collectors.toList());
            }
        }
        return treeNodes;
    }

    /**
     * 查找本级机构及以下用户机构树
     * @param parentId
     * @param checkedUserIds
     * @param checkbox
     * @param cascade
     * @param response
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "ownerAndChildsOrganUserTree")
    @ResponseBody
    public List<TreeNode> ownerAndChildsOrganUserTree(String parentId,
                                   @RequestParam(value = "checkedUserIds", required = false)List<String> checkedUserIds,
                                   @RequestParam(value = "checkbox",defaultValue = "true")Boolean checkbox,
                                   @RequestParam(value = "cascade",defaultValue = "true")Boolean cascade,HttpServletResponse response) {

        String _parentId = parentId;
        if(StringUtils.isBlank(_parentId)){
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            _parentId = SecurityUtils.isPermittedMaxRoleDataScope() ? null:sessionInfo.getLoginCompanyId();
        }
        List<TreeNode> treeNodes = organService.findOrganUserTree(_parentId, checkedUserIds,cascade);
        return treeNodes;
    }

    /**
     * 查找本级机构及以下用户机构树
     * @param parentId
     * @param organCode
     * @param checkedUserIds
     * @param checkbox
     * @param cascade
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = { "organUserTreeByOrganCode" })
    @ResponseBody
    public List<TreeNode> organUserTreeByOrganCode(String parentId,String organCode,
                                                   @RequestParam(value = "checkedUserIds", required = false)List<String> checkedUserIds,
                                                   @RequestParam(value = "checkbox",defaultValue = "true")Boolean checkbox,
                                                   @RequestParam(value = "cascade",defaultValue = "true")Boolean cascade) {
        String _organCode = organCode;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if(StringUtils.isBlank(_organCode)){
            _organCode = sessionInfo.getLoginHomeCompanyCode();
        }
        Organ o = organService.getByCode(_organCode);
        if (o != null) {
            parentId = o.getId();
        }

        List<TreeNode> treeNodes = organService.findOrganUserTree(parentId, checkedUserIds,cascade);
        return treeNodes;
    }

    /**
     * 排序调整
     *
     * @param upUserId
     * @param downUserId
     * @param moveUp
     * @return
     */
    @RequiresPermissions("sys:user:edit")
    @Logging(value = "用户管理-排序调整", logType = LogType.access)
    @PostMapping(value = {"changeOrderNo"})
    @ResponseBody
    public Result changeOrderNo(@RequestParam(required = true) String upUserId,
                                @RequestParam(required = true) String downUserId,
                                boolean moveUp) {
        userService.changeOrderNo(upUserId, downUserId, moveUp);
        return Result.successResult();
    }

    /**
     * 锁定用户 批量
     *
     * @param userIds
     * @param status  {@link com.eryansky.common.orm._enum.StatusState}
     * @return
     */
    @RequiresPermissions("sys:user:edit")
    @Logging(value = "用户管理-锁定用户", logType = LogType.access)
    @PostMapping(value = {"lock"})
    @ResponseBody
    public Result lock(@RequestParam(value = "userIds", required = false) List<String> userIds,
                       @RequestParam(required = false, defaultValue = User.STATUS_DELETE) String status) {
        userService.lockUsers(userIds, status);
        return Result.successResult();
    }

    /**
     * 查看用户密码
     *
     * @param loginName
     * @return
     */
    @RequiresRoles(AppConstants.ROLE_SYSTEM_MANAGER)
    @Logging(value = "用户管理-查看密码", logType = LogType.access)
    @PostMapping(value = "viewUserPassword")
    @ResponseBody
    public Result viewUserPassword(String loginName) throws Exception {
        Result result = Result.successResult();
        User user = userService.getUserByLoginName(loginName);
        if (user != null && user.getOriginalPassword() != null) {
            result.setObj(Encryption.decrypt(user.getOriginalPassword().trim()));
        }
        return result;
    }


    /**
     * @param userIds        已选择的用户ID
     * @param excludeUserIds 排除用户ID
     * @param dataScope      {@link DataScope}
     * @param multiple
     * @return
     */
    @GetMapping(value = {"select"})
    public ModelAndView selectPage(@RequestParam(value = "userIds", required = false) List<String> userIds,
                                   @RequestParam(value = "excludeUserIds", required = false) List<String> excludeUserIds,
                                   String dataScope, Boolean multiple, @RequestParam(value = "cascade", required = false, defaultValue = "false") Boolean cascade) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/user-select");
        List<User> users = Lists.newArrayList();
        if (Collections3.isNotEmpty(userIds)) {
            users = userService.findUsersByIds(userIds);

        }
        modelAndView.addObject("users", users);
        modelAndView.addObject("excludeUserIds", excludeUserIds);
        if (Collections3.isNotEmpty(excludeUserIds)) {
            modelAndView.addObject("excludeUserIdStrs", Collections3.convertToString(excludeUserIds, ","));
        }
        modelAndView.addObject("dataScope", dataScope);
        modelAndView.addObject("multiple", multiple);
        modelAndView.addObject("cascade", cascade);
        modelAndView.addObject("userDatagridData",
                JsonMapper.getInstance().toJson(new Datagrid(users.size(), users), User.class,
                        new String[]{"id", "name", "sexView", "defaultOrganName"}));
        return modelAndView;
    }


    /**
     * 根据机构查询用户信息
     *
     * @param organId        机构ID
     * @param roleId         角色
     * @param query          关键字
     * @param excludeUserIds 排除的用户IDS
     * @return
     */
    @PostMapping(value = {"datagridSelectUser"})
    @ResponseBody
    public String datagridSelectUser(String organId,
                                     String roleId,
                                     String query,
                                     @RequestParam(value = "excludeUserIds", required = false) List<String> excludeUserIds) {
        Page<User> page = new Page<>(SpringMVCHolder.getRequest());
        page = userService.findUserPageByOrgan(page, organId, query, excludeUserIds);
        Datagrid<User> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        return JsonMapper.getInstance().toJson(dg, User.class, new String[]{"id", "loginName", "name", "sexView", "sort","defaultOrganName","companyName"});
    }


    /**
     * Excel导出，获取的数据格式是List<Object[]>
     *
     * @return
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:user:view","sys:user:edit"})
    @GetMapping(value = "export")
    public void export(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/msexcel;charset=UTF-8");
        List<User> users = userService.findAllNormal();

        List<Object[]> list = new ArrayList<>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            list.add(new Object[]{UserUtils.getCompanyName(user.getId()), UserUtils.getDefaultOrganName(user.getId()), user.getLoginName(), user.getName(), user.getSexView(),
                    user.getTel(), user.getMobile(), user.getEmail()});
        }

        List<TableData> tds = new ArrayList<>();

        //Sheet
        String[] hearders = new String[]{"单位", "部门", "账号", "姓名", "性别", "电话", "手机号码", "邮箱"};//表头数组
        TableData td = ExcelUtils.createTableData(list, ExcelUtils.createTableHeader(hearders, 0), null);
        td.setSheetTitle("普通表头示例");
        tds.add(td);

        String title = "用户信息导出示例";
        JsGridReportBase report = new JsGridReportBase(request, response);
        report.exportToExcel(title, SecurityUtils.getCurrentUserName(), tds);

    }


    /**
     * 查看用户资源权限
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"viewUserResources"})
    public String viewUserResources(String userId, Model uiModel,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        User model = UserUtils.getUser(userId);
        if(WebUtils.isAjaxRequest(request)){
            List<Resource> list = resourceService.findResourcesWithPermissions(userId);
            //结构树展示优化
            list.forEach(v->{
                if(null == list.parallelStream().filter(r->r.getId().equals(v.get_parentId())).findAny().orElse(null)){
                    v.setParent(null);
                }
            });
            return renderString(response, new Datagrid<>(list.size(), list));
        }
        uiModel.addAttribute("model", model);
        return "modules/sys/user-resource-view";
    }

    /**
     * 用户权限树
     */
    @PostMapping(value = {"userResourcesData"})
    @ResponseBody
    public Result userResourcesData(String userId) {
        String _userId = StringUtils.isNotBlank(userId) ? userId:SecurityUtils.getCurrentUserId();
        List<TreeNode>  list = resourceService.findTreeNodeResourcesWithPermissions(_userId);
        return Result.successResult().setData(list);
    }

    /**
     * 详细信息
     *
     * @param model
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    @RestApi()
    public Result detail(@ModelAttribute("model") User model) {
        return Result.successResult().setObj(model);
    }
}
