/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security._enum.Logical;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.security.annotation.RequiresRoles;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.ResourceType;
import com.eryansky.modules.sys.mapper.Resource;
import com.eryansky.modules.sys.mapper.Role;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.ResourceService;
import com.eryansky.modules.sys.service.RoleService;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 资源权限Resource管理 Controller层.
 *
 * @author Eryan
 * @date 2012-10-11 下午4:36:24
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "${adminPath}/sys/resource")
public class ResourceController extends SimpleController {

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;

    @ModelAttribute("model")
    public Resource get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return resourceService.get(id);
        } else {
            return new Resource();
        }
    }

    @RequiresPermissions("sys:resource:view")
    @Logging(value = "资源管理", logType = LogType.access)
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {""})
    public String list() {
        return "modules/sys/resource";
    }

    @PostMapping(value = {"treegrid"})
    @ResponseBody
    public Datagrid<Resource> treegrid(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Resource model = new Resource();
        List<Resource> list = resourceService.findList(model);
        return new Datagrid<>(list.size(), list);
    }


    /**
     * @param model
     * @param parentId 上级ID
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"input"})
    public String input(@ModelAttribute("model") Resource model, String parentId, Model uiModel) throws Exception {
        uiModel.addAttribute("parentId", parentId);
        uiModel.addAttribute("model", model);
        return "modules/sys/resource-input";
    }

    /**
     * 保存.
     */
    @RequiresPermissions("sys:resource:edit")
    @Logging(value = "资源管理-保存资源", logType = LogType.access)
    @PostMapping(value = {"save"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result save(@ModelAttribute("model") Resource resource, String _parentId) {
        Result result = null;
        resource.setParent(null);
        // 设置上级节点
        if (StringUtils.isNotBlank(_parentId)) {
            Resource parentResource = resourceService.get(_parentId);
            if (parentResource == null) {
                logger.error("父级资源[{}]已被删除.", _parentId);
                throw new ActionException("父级资源已被删除.");
            }
            resource.setParent(parentResource);
        }

        if (StringUtils.isNotBlank(resource.getId())) {
            if (resource.getId().equals(resource.getParentId())) {
                result = new Result(Result.ERROR, "[上级资源]不能与[资源名称]相同.",
                        null);
                logger.debug(result.toString());
                return result;
            }
        }
        resourceService.saveResource(resource);
        result = Result.successResult();
        return result;
    }


    /**
     * 删除.
     */
    @RequiresPermissions("sys:resource:edit")
    @Logging(value = "资源管理-删除资源", logType = LogType.access)
    @PostMapping(value = {"delete/{id}"})
    @ResponseBody
    public Result delete(@PathVariable String id) {
//        resourceService.deleteById(id);
        resourceService.deleteOwnerAndChilds(id);
        return Result.successResult();
    }


    /**
     * 资源树.
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"tree"})
    @ResponseBody
    public List<TreeNode> tree(String selectType) throws Exception {
        List<TreeNode> treeNodes = null;
        List<TreeNode> titleList = Lists.newArrayList();
        TreeNode selectTreeNode = SelectType.treeNode(selectType);
        if (selectTreeNode != null) {
            titleList.add(selectTreeNode);
        }
        treeNodes = resourceService.findTreeNodeResources();
        return ListUtils.union(titleList, treeNodes);
    }


    /**
     * 资源树.
     */
    @RequiresRoles(AppConstants.ROLE_SYSTEM_MANAGER)
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"resourceData"})
    @ResponseBody
    public List<TreeNode> resourceData(){
        List<TreeNode>  treeNodes = resourceService.findTreeNodeResources();
        return treeNodes;
    }

    /**
     * 资源树-分级权限
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"resourceDataWithPermission"})
    @ResponseBody
    public List<TreeNode> resourceDataWithPermission(){
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        List<TreeNode>  treeNodes = resourceService.resourcesToTreeNode(resourceService.findResourcesWithPermissions(sessionInfo.getUserId()));
        return treeNodes;
    }


    /**
     * 资源类型下拉列表.
     */
    @PostMapping(value = {"resourceTypeCombobox"})
    @ResponseBody
    public List<Combobox> resourceTypeCombobox(String selectType, String parentId) throws Exception {
        List<Combobox> cList = Lists.newArrayList();
        Combobox selectCombobox = SelectType.combobox(selectType);
        if (selectCombobox != null) {
            cList.add(selectCombobox);
        }

        String parentType = null;
        if (StringUtils.isNotBlank(parentId)) {
            Resource resource = resourceService.get(parentId);
            parentType = resource.getType();
        }

        ResourceType parentResourceType = ResourceType.getByValue(parentType);
        if (parentResourceType != null) {
            if (parentResourceType.equals(ResourceType.app)) {
                Combobox combobox = new Combobox(ResourceType.app.getValue(), ResourceType.app.getDescription());
                cList.add(combobox);
                combobox = new Combobox(ResourceType.menu.getValue(), ResourceType.menu.getDescription());
                cList.add(combobox);
                combobox = new Combobox(ResourceType.function.getValue(), ResourceType.function.getDescription());
                cList.add(combobox);
            } else if (parentResourceType.equals(ResourceType.menu)) {
                Combobox combobox = new Combobox(ResourceType.menu.getValue(), ResourceType.menu.getDescription());
                cList.add(combobox);
                combobox = new Combobox(ResourceType.function.getValue(), ResourceType.function.getDescription());
                cList.add(combobox);
            } else if (parentResourceType.equals(ResourceType.function)) {
                Combobox combobox = new Combobox(ResourceType.function.getValue(), ResourceType.function.getDescription());
                cList.add(combobox);
            }
        } else {
            Combobox combobox = new Combobox(ResourceType.app.getValue(), ResourceType.app.getDescription());
            cList.add(combobox);
            combobox = new Combobox(ResourceType.menu.getValue(), ResourceType.menu.getDescription());
            cList.add(combobox);
            combobox = new Combobox(ResourceType.function.getValue(), ResourceType.function.getDescription());
            cList.add(combobox);
        }

        return cList;
    }

    /**
     * 父级资源下拉列表.
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"parent"})
    @ResponseBody
    public List<TreeNode> parent(@ModelAttribute("model") Resource resource, String selectType) {
        List<TreeNode> treeNodes = null;
        List<TreeNode> titleList = Lists.newArrayList();
        TreeNode selectTreeNode = SelectType.treeNode(selectType);
        if (selectTreeNode != null) {
            titleList.add(selectTreeNode);
        }
        treeNodes = resourceService.findTreeNodeResourcesWithExclude(resource.getId());
        return ListUtils.union(titleList, treeNodes);
    }

    /**
     * 排序最大值.
     */
    @PostMapping(value = {"maxSort"})
    @ResponseBody
    public Result maxSort() throws Exception {
        Result result;
        Integer maxSort = resourceService.getMaxSort();
        result = new Result(Result.SUCCESS, null, maxSort);
        logger.debug(result.toString());
        return result;
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
    public Result detail(@ModelAttribute("model") Resource model) {
        return Result.successResult().setObj(model);
    }



    /**
     * 资源关联角色
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"role/{resourceId}"})
    public String role(@PathVariable String resourceId,Model uiModel,HttpServletRequest request,HttpServletResponse response) {
        uiModel.addAttribute("resourceId",resourceId);
        return "modules/sys/resource-role";
    }


    /**
     * 资源关联角色
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    @PostMapping(value = {"resourceRoleDatagrid/{resourceId}"})
    @ResponseBody
    public Datagrid resourceRoleDatagrid(@PathVariable String resourceId,HttpServletRequest request,HttpServletResponse response) {
        Page<Role> page = new Page<>(request);
        page = roleService.findRolesByReourceId(page,resourceId);
        return new Datagrid(page.getTotalCount(),page.getResult());
    }


    /**
     * 资源关联角色
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:resource:edit","sys:resource:role:edit","sys:role:resource:edit"})
    @PostMapping(value = {"deleteRoles/{resourceId}"})
    @ResponseBody
    public Result deleteRoles(@PathVariable String resourceId,@RequestParam(value = "ids", required = false) List<String> ids,HttpServletRequest request,HttpServletResponse response) {
        if (Collections3.isEmpty(ids)) {
            logger.warn("参数[ids]为空.");
            return Result.warnResult().setMsg("操作失败，参数为空！");
        }
        for (String id : ids) {
            roleService.deleteRoleResourceByResourceIdAndRoleId(id,resourceId);
        }
        return Result.successResult();
    }


    /**
     * 资源关联用户
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"user/{resourceId}"})
    public String user(@PathVariable String resourceId,Model uiModel,HttpServletRequest request,HttpServletResponse response) {
        uiModel.addAttribute("resourceId",resourceId);
        return "modules/sys/resource-user";
    }


    /**
     * 资源关联用户
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    @PostMapping(value = {"resourceUserDatagrid/{resourceId}"})
    @ResponseBody
    public Datagrid resourceUserDatagrid(@PathVariable String resourceId,HttpServletRequest request,HttpServletResponse response) {
        Page<User> page = new Page<>(request);
        page = userService.findUsersByResourceId(page,resourceId);
        return new Datagrid(page.getTotalCount(),page.getResult());
    }


    /**
     * 资源关联用户
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:resource:edit","sys:resource:user:edit","sys:user:resource:edit"})
    @PostMapping(value = {"deleteUsers/{resourceId}"})
    @ResponseBody
    public Result deleteUsers(@PathVariable String resourceId,@RequestParam(value = "ids", required = false) List<String> ids,HttpServletRequest request,HttpServletResponse response) {
        if (Collections3.isEmpty(ids)) {
            logger.warn("参数[ids]为空.");
            return Result.warnResult().setMsg("操作失败，参数为空！");
        }
        for (String id : ids) {
            userService.deleteUserResourceByResourceIdAndUserId(id,resourceId);
        }
        return Result.successResult();
    }

}
