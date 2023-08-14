/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.web;

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
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import com.eryansky.modules.notice._enum.ContactGroupType;
import com.eryansky.modules.notice.mapper.ContactGroup;
import com.eryansky.modules.notice.mapper.MailContact;
import com.eryansky.modules.notice.service.ContactGroupService;
import com.eryansky.modules.notice.service.MailContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Eryan
 * @date 2014-11-07
 */
@Controller
@RequestMapping(value = "${adminPath}/notice/contactGroup")
public class ContactGroupController extends SimpleController {

    @Autowired
    private ContactGroupService contactGroupService;
    @Autowired
    private MailContactService mailContactService;

    @ModelAttribute("model")
    public ContactGroup get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return contactGroupService.get(id);
        } else {
            return new ContactGroup();
        }
    }

    @GetMapping(value = {""})
    public String list() {
        return "modules/notice/contactGroup";
    }

    @GetMapping(value = {"input"})
    public String input(@ModelAttribute("model") ContactGroup model) {
        return "modules/notice/contactGroup-input";
    }

    /**
     * 邮件联系人编辑
     */
    @GetMapping(value = {"inputContactGroupMail"})
    public String inputContactGroupMail(@ModelAttribute("model") MailContact model) {
        return "modules/notice/contactGroupMail-input";
    }

    /**
     * 联系人组 保存
     * @param model
     * @return
     */
    @PostMapping(value = {"save"})
    @ResponseBody
    public Result save(@ModelAttribute("model") ContactGroup model) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Result result = Result.errorResult();
        if (contactGroupService.checkExist(sessionInfo.getUserId(), model.getContactGroupType(), model.getName(), model.getId()) == null) {
            model.setUserId(sessionInfo.getUserId());
            contactGroupService.save(model);
            return Result.successResult();
        } else {
            result.setMsg("用户组[" + model.getName() + "]已存在");
        }
        return result;
    }

    /**
     * 个人 联系人组树形菜单 查询用
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"groupTree"})
    @ResponseBody
    public List<TreeNode> groupTree(String contactGroupType) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        List<TreeNode> treeNodes = Lists.newArrayList();
        TreeNode rootNode = new TreeNode("", "全部群组");
        treeNodes.add(rootNode);
        List<ContactGroup> list = contactGroupService.findUserContactGroups(sessionInfo.getUserId(), contactGroupType);
        for (ContactGroup contactGroup : list) {
            TreeNode treeNode = new TreeNode(contactGroup.getId(), contactGroup.getName());
            if (contactGroupType == null) {
                treeNode.addAttribute("contactGroupType", contactGroup.getContactGroupType());
            }
            rootNode.addChild(treeNode);
        }
        return treeNodes;
    }


    /**
     * 个人 联系人组树形菜单
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"tree"})
    @ResponseBody
    public List<TreeNode> tree(String contactGroupType) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        List<TreeNode> treeNodes = Lists.newArrayList();
        List<ContactGroup> list = contactGroupService.findUserContactGroups(sessionInfo.getUserId(), contactGroupType);
        for (ContactGroup contactGroup : list) {
            TreeNode treeNode = new TreeNode(contactGroup.getId(), contactGroup.getName());
            treeNodes.add(treeNode);
        }
        return treeNodes;
    }

    /**
     * 联系人组 删除
     * @param ids
     * @return
     */
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        ids.forEach(v -> {
            contactGroupService.delete(new ContactGroup(v));
        });
        return Result.successResult();
    }

    /**
     *
     * @param contactGroupId 角色ID
     * @return
     */
    @GetMapping(value = {"select"})
    public ModelAndView selectPage(String contactGroupId) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/user-select");
        List<User> users = null;
        ContactGroup contactGroup = contactGroupService.get(contactGroupId);
        List<String> excludeUserIds = contactGroup.getObjectIds();
        modelAndView.addObject("users", users);
        modelAndView.addObject("excludeUserIds", excludeUserIds);
        if (Collections3.isNotEmpty(excludeUserIds)) {
            modelAndView.addObject("excludeUserIdStrs", Collections3.convertToString(excludeUserIds, ","));
        }
        modelAndView.addObject("dataScope", "2");//不分级授权
        modelAndView.addObject("cascade", "true");//不分级授权
        modelAndView.addObject("multiple", "");
        modelAndView.addObject("userDatagridData", JsonMapper.getInstance().toJson(new Datagrid()));
        return modelAndView;
    }

    /**
     * 添加联系人表单页面
     * @param contactGroupId 联系人组ID
     * @return
     */
    @GetMapping(value = {"contactGroupUser"})
    @ResponseBody
    public ModelAndView contactGroupUser(String contactGroupId) {
        ModelAndView modelAndView = new ModelAndView("modules/notice/contactGroup-user");
        return modelAndView;
    }

    /**
     * 添加联系人
     * @param addObjectIds 新增联系人 ID集合
     * @return
     */
    @PostMapping(value = {"addContactGroupUser"})
    @ResponseBody
    public Result addContactGroupUser(@ModelAttribute("model") ContactGroup model,
                                      @RequestParam(value = "addObjectIds", required = false) List<String> addObjectIds) {
        contactGroupService.deleteContactGroupObjects(model.getId(), addObjectIds);
        contactGroupService.insertContactGroupObjects(model.getId(), addObjectIds);
        return Result.successResult();
    }

    /**
     * 移除联系人
     * @param removeObjectIds 需要移除的联系人 ID集合
     * @return
     */
    @PostMapping(value = {"removeContactGroupUser"})
    @ResponseBody
    public Result removeContactGroupUser(@ModelAttribute("model") ContactGroup model,
                                         @RequestParam(value = "removeObjectIds", required = true) List<String> removeObjectIds) {
        contactGroupService.deleteContactGroupObjects(model.getId(), removeObjectIds);
        return Result.successResult();
    }

    /**
     * 联系人列表
     * @param id 联系人组ID
     * @param query 用户登录名或姓名
     * @return
     */
    @PostMapping(value = {"contactGroupUserDatagrid"})
    @ResponseBody
    public String contactGroupUserDatagrid(String id, String query) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String json = "[]";
        if (StringUtils.isNotBlank(id)) {
            Page<User> page = new Page<>(SpringMVCHolder.getRequest());
            page = contactGroupService.findContactGroupUsers(page, id, query);
            Datagrid<User> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
            json = JsonMapper.getInstance().toJson(dg, User.class,
                    new String[]{"id", "loginName", "name", "sexView", "defaultOrganName", "email", "mobile", "tel"});
        }
        return json;
    }

    /**
     * 联系人列表
     * @param contactGroupId 联系人组ID
     * @param loginNameOrName 用户登录名或姓名
     * @return
     */
    @PostMapping(value = {"selectContactGroupUserDatagrid"})
    @ResponseBody
    public String selectContactGroupUserDatagrid(String contactGroupId, String loginNameOrName) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        ContactGroup contactGroup = contactGroupService.get(contactGroupId);
        List<User> users = Lists.newArrayList();
        if (contactGroup != null) {
            users = contactGroupService.findContactGroupUsers(contactGroupId, loginNameOrName);
        }
        String json = JsonMapper.getInstance().toJson(users, User.class,
                new String[]{"id", "loginName", "name", "sexView", "organNames"});
        return json;
    }

    /**
     * 类型下拉列表.
     */
    @PostMapping(value = {"contactGroupTypeCombobox"})
    @ResponseBody
    public List<Combobox> contactGroupTypeCombobox(String selectType) throws Exception {
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }

        ContactGroupType[] lts = ContactGroupType.values();
        for (int i = 0; i < lts.length; i++) {
            Combobox combobox = new Combobox();
            combobox.setValue(lts[i].getValue().toString());
            combobox.setText(lts[i].getDescription());
            cList.add(combobox);
        }
        return cList;
    }

    /**
     * 排序最大值.
     */
    @PostMapping(value = {"maxSort"})
    @ResponseBody
    public Result maxSort() throws Exception {
        Result result;
        Integer maxSort = contactGroupService.getMaxSort();
        result = new Result(Result.SUCCESS, null, maxSort);
        logger.debug(result.toString());
        return result;
    }
}