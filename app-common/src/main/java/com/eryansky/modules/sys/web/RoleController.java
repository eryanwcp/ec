/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.excels.CsvUtils;
import com.eryansky.core.excels.ExcelUtils;
import com.eryansky.core.excels.JsGridReportBase;
import com.eryansky.core.excels.TableData;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security._enum.Logical;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys._enum.DataScope;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.RoleType;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.mapper.Resource;
import com.eryansky.modules.sys.mapper.Role;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.*;
import com.eryansky.modules.sys.utils.PostUtils;
import com.eryansky.modules.sys.utils.RoleUtils;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色Role管理 Controller层.
 *
 * @author Eryan
 * @date 2012-10-11 下午4:36:24
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "${adminPath}/sys/role")
public class RoleController extends SimpleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private UserService userService;

    @ModelAttribute("model")
    public Role get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return roleService.get(id);
        } else {
            return new Role();
        }
    }

    @RequiresPermissions("sys:role:view")
    @Logging(value = "角色管理", logType = LogType.access)
    @GetMapping(value = {""})
    public String list(Model uiModel) {
        uiModel.addAttribute("roleTypes", SecurityUtils.isCurrentUserAdmin() ? RoleType.values():Lists.newArrayList(RoleType.USER));
        return "modules/sys/role";
    }

    @PostMapping(value = {"datagrid"})
    @ResponseBody
    public String datagrid(Role model) {
        HttpServletRequest request = SpringMVCHolder.getRequest();
        // 自动构造属性过滤器
        Page<Role> p = new Page<>(request);

        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String organId = sessionInfo.getLoginCompanyId();
        if (!SecurityUtils.isCurrentUserAdmin()) {
            model.setOrganId(organId);
        }
        p = roleService.findPage(p, model);
        Datagrid<Role> datagrid = new Datagrid<>(p.getTotalCount(), p.getResult());
        String json = JsonMapper.getInstance().toJson(datagrid, Role.class,
                new String[]{"id", "name", "code", "isSystem", "isSystemView", "organName","roleType","roleTypeView","dataScope","dataScopeView", "remark","updateTime"});
        return json;
    }

    /**
     * @param model
     * @return
     */
    @GetMapping(value = {"input"})
    public String input(@ModelAttribute("model") Role model, Model uiModel) {
        if (StringUtils.isBlank(model.getId()) && !SecurityUtils.isCurrentUserAdmin()) {
            model.setIsSystem(YesOrNo.NO.getValue());
        }
        uiModel.addAttribute("dataOrganIds", roleService.findRoleDataOrganIds(model.getId()));
        uiModel.addAttribute("organIds", roleService.findRoleOrganIds(model.getId()));
        uiModel.addAttribute("model", model);
        uiModel.addAttribute("roleTypes", SecurityUtils.isCurrentUserAdmin() ? RoleType.values():Lists.newArrayList(RoleType.USER));
        return "modules/sys/role-input";
    }

    /**
     * 保存.
     */
    @RequiresPermissions("sys:role:edit")
    @Logging(value = "角色管理-保存角色", logType = LogType.access)
    @PostMapping(value = {"save"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody()
    public Result save(@ModelAttribute("model") Role role, @RequestParam(value = "dataOrganIds", required = false) List<String> dataOrganIds) {
        Result result;
        // 编码重复校验
        if (StringUtils.isNotBlank(role.getCode())) {
            Role checkRole = roleService.getByCode(role.getCode());
            if (checkRole != null && !checkRole.getId().equals(role.getId())) {
                result = new Result(Result.WARN, "编码为[" + role.getCode() + "]已存在,请修正!", "code");
                logger.debug(result.toString());
                return result;
            }
        }

        roleService.saveRole(role,dataOrganIds);
        result = Result.successResult();
        return result;
    }

    /**
     * 删除.
     */
    @RequiresPermissions("sys:role:edit")
    @Logging(value = "角色管理-删除角色", logType = LogType.access)
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        roleService.deleteByIds(ids);
        Result result = Result.successResult();
        logger.debug(result.toString());
        return result;
    }


    /**
     * 设置资源 页面
     *
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"resource"})
    public String resource(@ModelAttribute("model") Role model, Model uiModel) throws Exception {
        List<TreeNode> treeNodes = resourceService.findTreeNodeResourcesWithPermissions(SecurityUtils.getCurrentUserId());
        String resourceComboboxData = JsonMapper.getInstance().toJson(treeNodes);
        logger.debug(resourceComboboxData);
        uiModel.addAttribute("resourceComboboxData", resourceComboboxData);
        uiModel.addAttribute("model", model);
        List<String> resourceIds = resourceService.findResourceIdsByRoleId(model.getId());
        uiModel.addAttribute("resourceIds", resourceIds);
        return "modules/sys/role-resource";
    }

    /**
     * 设置角色资源
     *
     * @return
     */
    @RequiresPermissions(value = {"sys:role:edit","sys:role:resource:edit","sys:resource:role:edit"},logical = Logical.OR)
    @Logging(value = "角色管理-角色资源", logType = LogType.access)
    @PostMapping(value = {"updateRoleResource"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result updateRoleResource(@RequestParam(value = "resourceIds", required = false) Set<String> resourceIds,
                                     @ModelAttribute("model") Role role) {
        roleService.saveRoleResources(role.getId(), resourceIds);
        return Result.successResult();
    }


    /**
     * 从角色复制资源 页面
     *
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"copy"})
    public String copy(@ModelAttribute("model") Role model, Model uiModel) throws Exception {
        uiModel.addAttribute("model",model);
        return "modules/sys/role-copy";
    }

    /**
     * 从角色复制资源
     *
     * @return
     */
    @RequiresPermissions(value = {"sys:role:edit","sys:role:resource:edit","sys:resource:role:edit"},logical = Logical.OR)
    @Logging(value = "角色管理-从角色复制资源", logType = LogType.access)
    @PostMapping(value = {"copyFromRoles"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result copyFromRoles(@ModelAttribute("model") Role role,
                                     @RequestParam(value = "roleIds", required = false) Collection<String> roleIds) {
        //源有
        List<Resource> resources = resourceService.findResourcesByRoleId(role.getId());
        //新增
        for(String rId:roleIds){
            resources.addAll(resourceService.findResourcesByRoleId(rId));
        }
        //合并
        Set<String> rIds = resources.stream().map(BaseEntity::getId).collect(Collectors.toSet());
        roleService.saveRoleResources(role.getId(), rIds);
        return Result.successResult();
    }

    /**
     * 角色用户 页面
     *
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"user"})
    public String user(@ModelAttribute("model") Role model, Model uiModel) throws Exception {
        List<String> userIds = userService.findUserIdsByRoleId(model.getId());
        uiModel.addAttribute("userIds", userIds);
        return "modules/sys/role-user";
    }

    /**
     * 角色用户数据
     *
     * @param roleId 角色ID
     * @param query 关键字
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"userDatagrid"})
    public String userDatagrid(@RequestParam(value = "roleId", required = true) String roleId,
                               String query,
                               @RequestParam(value = "export", defaultValue = "false") Boolean export,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        Page<User> page = new Page<>(SpringMVCHolder.getRequest());
        if(export){
            page.setPageSize(Page.PAGESIZE_ALL);
        }
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String parentOrganId = SecurityUtils.isPermittedMaxRoleDataScope() ? null:sessionInfo.getLoginCompanyId();
        //无分级授权
        page = userService.findPageRoleUsers(page, roleId,parentOrganId, query);
        if (export) {
            List<Object[]> data = Lists.newArrayList();
            page.getResult().forEach(u -> data.add(new Object[]{u.getName(), u.getLoginName(),u.getMobile(), u.getDefaultOrganName(), u.getCompanyName()}));

            String title = "角色用户信息-" + RoleUtils.getRoleName(roleId);
            //Sheet2
            String[] hearders = new String[]{"姓名", "账号", "手机号", "部门", "单位"};

            if (page.getResult().size() < 65531) {
                //导出Excel
                try {
                    TableData td = ExcelUtils.createTableData(data, ExcelUtils.createTableHeader(hearders, 0), null);
                    td.setSheetTitle(title);
                    JsGridReportBase report = new JsGridReportBase(request, response);
                    report.exportToExcel(title, sessionInfo.getName(), td);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                //导出CSV
                try {
                    CsvUtils.exportToCsv(title, hearders, data, request, response);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return null;
        }
        Datagrid<User> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        return renderString(response, JsonMapper.getInstance().toJson(dg, User.class,
                new String[]{"id", "name", "loginName","mobile","mobileSensitive","sexView", "sort", "defaultOrganName","companyName"}), WebUtils.JSON_TYPE);
    }

    /**
     * @param roleId 角色ID
     * @return
     */
    @GetMapping(value = {"select"})
    public ModelAndView selectPage(String roleId) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/user-select");
        List<User> users = null;
//        List<User> users = userService.findUsersByRoleId(roleId);
        List<String> excludeUserIds = userService.findUserIdsByRoleId(roleId);
        modelAndView.addObject("users", users);
        modelAndView.addObject("excludeUserIds", excludeUserIds);
        if (Collections3.isNotEmpty(excludeUserIds)) {
            modelAndView.addObject("excludeUserIdStrs", Collections3.convertToString(excludeUserIds, ","));
        }
        modelAndView.addObject("dataScope", DataScope.COMPANY_AND_CHILD.getValue());//不分级授权
        modelAndView.addObject("cascade", "true");//不分级授权
        modelAndView.addObject("multiple", "");
        modelAndView.addObject("userDatagridData", JsonMapper.getInstance().toJson(new Datagrid()));
        return modelAndView;
    }

    /**
     * 添加角色关联用户
     *
     * @param model   角色
     * @param userIds 用户ID
     * @return
     */
    @RequiresPermissions(value = {"sys:role:edit","sys:role:user:edit","sys:user:role:edit"},logical = Logical.OR)
    @Logging(value = "角色管理-添加关联用户", logType = LogType.access)
    @PostMapping(value = {"addRoleUser"})
    @ResponseBody
    public Result addRoleUser(@ModelAttribute("model") Role model,
                              @RequestParam(value = "userIds", required = true) Set<String> userIds) {
        roleService.insertRoleUsers(model.getId(), userIds);
        userService.clearCache();
        return Result.successResult();
    }


    /**
     * 移除角色关联用户
     *
     * @param model   角色
     * @param userIds 用户IDS
     * @return
     */
    @RequiresPermissions(value = {"sys:role:edit","sys:role:user:edit","sys:user:role:edit"},logical = Logical.OR)
    @Logging(value = "角色管理-移除关联用户", logType = LogType.access)
    @PostMapping(value = {"removeRoleUser"})
    @ResponseBody
    public Result removeRoleUser(@ModelAttribute("model") Role model,
                                 @RequestParam(value = "userIds", required = true) Set<String> userIds) {
        roleService.deleteRoleUsersByRoleIdANDUserIds(model.getId(), userIds);
        userService.clearCache();
        return Result.successResult();
    }


    /**
     * 设置角色用户
     *
     * @return
     */
    @RequiresPermissions(value = {"sys:role:edit","sys:role:user:edit","sys:user:role:edit"},logical = Logical.OR)
    @Logging(value = "角色管理-保存角色用户", logType = LogType.access)
    @PostMapping(value = {"updateRoleUser"})
    @ResponseBody
    public Result updateRoleUser(@ModelAttribute("model") Role model,
                                 @RequestParam(value = "userIds", required = false) Set<String> userIds) {
        roleService.saveRoleUsers(model.getId(), userIds);
        userService.clearCache();
        return Result.successResult();
    }

    /**
     * 查看角色资源权限
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"viewRoleResources"})
    public String viewRoleResources(String roleId, Model uiModel,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        Role model = RoleUtils.getRole(roleId);
        if(WebUtils.isAjaxRequest(request)){
            List<Resource> list = resourceService.findResourcesByRoleId(roleId);
            //结构树展示优化
            list.forEach(v->{
                if(null == list.parallelStream().filter(r->r.getId().equals(v.get_parentId())).findAny().orElse(null)){
                    v.setParent(null);
                }
            });
            return renderString(response, new Datagrid<>(list.size(), list));
        }
        uiModel.addAttribute("model", model);
        return "modules/sys/role-resource-view";
    }

    /**
     * 数据范围下拉列表
     *
     * @param selectType {@link SelectType}
     * @return
     */
    @PostMapping(value = {"dataScope"})
    @ResponseBody
    public List<Combobox> dataScope(String selectType) {
        DataScope[] list = DataScope.values();
        List<Combobox> cList = Lists.newArrayList();

        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        for (DataScope r : list) {
            Combobox combobox = new Combobox(r.getValue() + "", r.getDescription());
            cList.add(combobox);
        }
        return cList;
    }


    /**
     * 数据范围下拉列表（授权）
     *
     * @param selectType {@link SelectType}
     * @return
     */
    @PostMapping(value = {"dataScopeWithPermission"})
    @ResponseBody
    public List<Combobox> dataScopeWithPermission(String selectType) {
        DataScope[] list = DataScope.values();
        List<Combobox> cList = Lists.newArrayList();

        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        String maxRoleDataScope = SecurityUtils.getUserMaxRoleDataScope();
        for (DataScope r : list) {
            if(Integer.parseInt(r.getValue()) >= Integer.parseInt(maxRoleDataScope) ){
                Combobox combobox = new Combobox(r.getValue() + "", r.getDescription());
                cList.add(combobox);
            }
        }
        return cList;
    }

    /**
     * 角色下拉框列表
     */
    @PostMapping(value = {"combobox"})
    @ResponseBody
    public List<Combobox> combobox(String selectType) {
        List<Role> list = roleService.findAll();
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        for (Role r : list) {
            Combobox combobox = new Combobox(r.getId(), r.getName());
            cList.add(combobox);
        }
        return cList;
    }


    /**
     * 角色下拉框列表（授权）
     */
    @PostMapping(value = {"comboboxWithPermission"})
    @ResponseBody
    public List<Combobox> comboboxWithPermission(String selectType) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String organId = sessionInfo.getLoginCompanyId();

        List<Role> list = SecurityUtils.isCurrentUserAdmin() ? roleService.findAll() : roleService.findOrganRolesAndSystemNormalRoles(organId);

        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        for (Role r : list) {
            Combobox combobox = new Combobox(r.getId(), r.getName());
            cList.add(combobox);
        }
        return cList;
    }


    /**
     * 角色树.
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"tree"})
    @ResponseBody
    public List<TreeNode> tree(String selectType) {
        List<TreeNode> treeNodes = Lists.newArrayList();
        List<Role> list = roleService.findAll();

        TreeNode titleTreeNode = SelectType.treeNode(selectType);
        if (titleTreeNode != null) {
            treeNodes.add(titleTreeNode);
        }

        for (Role r : list) {
            TreeNode treeNode = new TreeNode(r.getId(), r.getName());
            treeNodes.add(treeNode);
        }
        return treeNodes;
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
    public Result detail(@ModelAttribute("model") Role model) {
        return Result.successResult().setObj(model);
    }
}
