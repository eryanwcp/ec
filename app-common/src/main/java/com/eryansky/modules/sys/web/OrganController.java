/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security._enum.Logical;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys._enum.DataScope;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.OrganType;
import com.eryansky.modules.sys.mapper.*;
import com.eryansky.modules.sys.service.*;
import com.eryansky.modules.sys.utils.DictionaryUtils;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.utils.AppUtils;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 机构Organ管理 Controller层.
 *
 * @author Eryan
 * @date 2013-09-09 下午21:36:24
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "${adminPath}/sys/organ")
public class OrganController extends SimpleController {

    @Autowired
    private OrganService organService;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private SystemService systemService;

    @ModelAttribute("model")
    public Organ get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return organService.get(id);
        } else {
            return new Organ();
        }
    }

    @RequiresPermissions("sys:organ:view")
    @Logging(value = "机构管理", logType = LogType.access)
    @GetMapping(value = {""})
    public String list() {
        return "modules/sys/organ";
    }


    @PostMapping(value = {"treegrid"})
    @ResponseBody
    public String treegrid(String parentId) {
        List<Organ> list = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (StringUtils.isBlank(parentId)) {
            if (sessionInfo.isSuperUser()) {
                list = organService.findDataByParent(null, null);
            } else {
                String organId = sessionInfo.getLoginCompanyId();
                list = new ArrayList<>(1);
                list.add(organService.get(organId));
            }

        } else {
            list = organService.findDataByParent(parentId, null);
        }

        String json = JsonMapper.getInstance().toJson(list);
        return json;
    }

    /**
     * @param model
     * @return
     */
    @GetMapping(value = {"input"})
    public ModelAndView input(@ModelAttribute("model") Organ model, String parentId, Model uiModel){
        ModelAndView modelAndView = new ModelAndView("modules/sys/organ-input");
        modelAndView.addObject("parentId", parentId);
        modelAndView.addObject("model", model);
        return modelAndView;
    }


    /**
     * 排序最大值.
     */
    @PostMapping(value = {"maxSort"})
    @ResponseBody
    public Result maxSort() {
        Result result;
        Integer maxSort = organService.getMaxSort();
        result = new Result(Result.SUCCESS, null, maxSort);
        logger.debug(result.toString());
        return result;
    }


    /**
     * 父级机构下拉列表.
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"parentOrgan"})
    @ResponseBody
    public List<TreeNode> parentOrgan(String selectType, @ModelAttribute("model") Organ organ) {
        List<TreeNode> treeNodes = null;
        List<TreeNode> titleList = Lists.newArrayList();
        TreeNode selectTreeNode = SelectType.treeNode(selectType);
        if (selectTreeNode != null) {
            titleList.add(selectTreeNode);
        }
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String excludeOrganId = organ.getId();
        String organId = null;
        TreeNode parentTreeNode = null;
        if (sessionInfo.isSuperUser()) {
            organId = null;
        } else {
            OrganExtend o = OrganUtils.getOrganExtendByUserId(sessionInfo.getUserId());
            OrganExtend organExtend = OrganUtils.getOrganExtend(organ.getId());
            if (o.getTreeLevel() >= organExtend.getTreeLevel()) {
                parentTreeNode = organService.organToTreeNode(organ.getParent());
                excludeOrganId = null;
            }
        }
        treeNodes = organService.findOrganTree(organId, excludeOrganId);
        if (parentTreeNode != null) {
            treeNodes.add(parentTreeNode.setState(TreeNode.STATE_OPEN));
        }
        return ListUtils.union(titleList, treeNodes);
    }

    /**
     * 保存.
     */
    @RequiresPermissions("sys:organ:edit")
    @Logging(value = "机构管理-保存机构", logType = LogType.access)
    @PostMapping(value = {"save"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result save(@ModelAttribute("model") Organ organ, String _parentId) {
        Result result = null;
        organ.setParent(null);
        // 设置上级节点
        if (StringUtils.isNotBlank(_parentId) && !"0".equals(_parentId)) {
            Organ parentOrgan = organService.get(_parentId);
            if (parentOrgan == null) {
                logger.error("父级机构[{}]已被删除.", _parentId);
                throw new ActionException("父级机构已被删除.");
            }
            organ.setParent(parentOrgan);
        }
        try {
            organService.saveOrgan(organ);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        result = Result.successResult();
        return result;
    }

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     * @return
     */
    @RequiresPermissions("sys:organ:edit")
    @Logging(value = "机构管理-删除机构", logType = LogType.access)
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"delete/{id}"})
    @ResponseBody
    public Result delete(@PathVariable String id) {
//        organService.deleteById(id);
        organService.deleteOwnerAndChilds(id);
        return Result.successResult();
    }


    /**
     * 设置机构用户 页面
     *
     * @return
     */
    @GetMapping(value = {"user"})
    public String user(@ModelAttribute("model") Organ model, Model uiModel) {
        List<User> organUsers = userService.findOrganUsers(model.getId());
        String organUserCombogridData = JsonMapper.getInstance().toJson(organUsers, User.class,
                new String[]{"id", "name", "sexView", "defaultOrganName"});
        logger.debug(organUserCombogridData);
        uiModel.addAttribute("organUserCombogridData", organUserCombogridData);
        uiModel.addAttribute("model", model);
        return "modules/sys/organ-user";
    }


    /**
     * 设置机构用户
     *
     * @return
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:organ:edit","sys:organ:user:edit","sys:user:organ:edit"})
    @Logging(value = "机构管理-机构用户", logType = LogType.access)
    @PostMapping(value = {"updateOrganUser"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result updateOrganUser(@ModelAttribute("model") Organ organ) {
        Result result;
        organService.saveOrgan(organ);
        result = Result.successResult();
        logger.debug(result.toString());
        return result;
    }

    /**
     * @param parentId
     * @param selectType
     * @param dataScope  {@link DataScope}
     * @param postId  岗位ID
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"tree"})
    @ResponseBody
    public List<TreeNode> tree(String parentId,
                               String selectType,
                               String dataScope,
                               String postId,
                               @RequestParam(value = "cascade", required = false, defaultValue = "false") Boolean cascade) {
        List<TreeNode> titleList = Lists.newArrayList();
        TreeNode selectTreeNode = SelectType.treeNode(selectType);
        if (selectTreeNode != null) {
            titleList.add(selectTreeNode);
        }
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        List<TreeNode> treeNodes;
        //按岗位查找
        if (StringUtils.isNotBlank(postId)) {
            List<Organ> organList = organService.findAssociationOrgansByPostId(postId);
            if (sessionInfo.isSuperUser() || (StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.ALL.getValue()))) {

            } else if(StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.HOME_COMPANY_AND_CHILD.getValue())) {
                organList = organList.parallelStream().filter(v-> Objects.equals(OrganUtils.getHomeCompanyIdByRecursive(v.getId()), sessionInfo.getLoginHomeCompanyId())).collect(Collectors.toList());
            } else if (StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.COMPANY_AND_CHILD.getValue())) {
                organList = organList.parallelStream().filter(v-> Objects.equals(OrganUtils.getCompanyIdByRecursive(v.getId()), sessionInfo.getLoginCompanyId())).collect(Collectors.toList());
            } else if (StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.OFFICE_AND_CHILD.getValue())) {
                organList = organList.parallelStream().filter(v->v.getId().equals(sessionInfo.getLoginOrganId())).collect(Collectors.toList());
            }
            treeNodes = AppUtils.toTreeTreeNodes(organList.parallelStream().map(v ->
                    organService.organToTreeNode(v)
            ).collect(Collectors.toList()));
        }else{
            String _parentId = parentId;
            if (StringUtils.isBlank(parentId)) {
                String organId = sessionInfo != null ? sessionInfo.getLoginOrganId() : null;
                if (sessionInfo.isSuperUser() || (StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.ALL.getValue()))) {
                    organId = null;
                } else if (StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.HOME_COMPANY_AND_CHILD.getValue())) {
                    organId = sessionInfo.getLoginHomeCompanyId();
                } else if (StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.COMPANY_AND_CHILD.getValue())) {
                    organId = sessionInfo.getLoginCompanyId();
                } else if (StringUtils.isNotBlank(dataScope) && dataScope.equals(DataScope.OFFICE_AND_CHILD.getValue())) {
                    organId = sessionInfo.getLoginOrganId();
                }
                _parentId = organId;
            }
            treeNodes = organService.findOrganTree(_parentId, true, cascade);
        }
        return ListUtils.union(titleList, treeNodes);
    }

    /**
     * 机构类型下拉列表.
     *
     * @param parentId 父级机构ID
     */
    @PostMapping(value = {"organTypeCombobox"})
    @ResponseBody
    public List<Combobox> organTypeCombobox(String selectType, String parentId) {
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        String parentType = null;
        if (StringUtils.isNotBlank(parentId)) {
            Organ organ = organService.get(parentId);
            if (organ != null) {
                parentType = organ.getType();
            }
        }

        OrganType enumParentType = OrganType.getByValue(parentType);
        if (enumParentType != null) {
            if (enumParentType.equals(OrganType.organ)) {
                OrganType[] rss = OrganType.values();
                for (int i = 0; i < rss.length; i++) {
                    Combobox combobox = new Combobox();
                    combobox.setValue(rss[i].getValue());
                    combobox.setText(rss[i].getDescription());
                    cList.add(combobox);
                }
            } else if (enumParentType.equals(OrganType.department)) {
                Combobox departmentCombobox = new Combobox(OrganType.department.getValue(), OrganType.department.getDescription());
                Combobox groupCombobox = new Combobox(OrganType.group.getValue(), OrganType.group.getDescription());
                cList.add(departmentCombobox);
                cList.add(groupCombobox);
            } else if (enumParentType.equals(OrganType.group)) {
                Combobox groupCombobox = new Combobox(OrganType.group.getValue(), OrganType.group.getDescription());
                cList.add(groupCombobox);
            }
        } else {
            Combobox groupCombobox = new Combobox(OrganType.organ.getValue(), OrganType.organ.getDescription());
            cList.add(groupCombobox);
        }
        List<DictionaryItem> dictionaryItems = DictionaryUtils.getDictList(Organ.DIC_ORGAN_TYPE);
        dictionaryItems.stream().forEach(v->cList.add(new Combobox(v.getCode(), v.getName())));
        return cList;
    }


    /**
     * 机构树
     *
     * @param extId
     * @param type
     * @param grade
     * @param shortName
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "treeData")
    public List<TreeNode> treeData(@RequestParam(required = false) String extId,
                                   @RequestParam(required = false) Integer type,
                                   @RequestParam(required = false) Integer grade,
                                   Boolean shortName,
                                   HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        return organService.findOrganTree(null, extId,shortName);
    }

    /**
     * 查找所有机构类型机构 {@link OrganType#organ}
     *
     * @param response
     * @param shortName 简称
     * @return
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "treeCompanyData")
    public List<TreeNode> treeCompanyData(
            Boolean shortName,
            HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        return organService.findCompanysTree(shortName);
    }

    /**
     * 查找自己和子机构机构类型机构 {@link OrganType#organ}
     *
     * @param parentId
     * @param shortName 简称
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "ownerAndChildsHomeCompanysData")
    public List<TreeNode> ownerAndChildsHomeCompanysData(String parentId,
                                                         Boolean shortName,
                                                         HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        String _parentId = parentId;
        if (StringUtils.isBlank(_parentId)) {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            _parentId = SecurityUtils.isPermittedMaxRoleDataScope() ? null : sessionInfo.getLoginHomeCompanyId();
        }
        return organService.findOwnerAndChildsCompanysTree(_parentId,shortName);
    }

    /**
     * 查找自己和子机构机构类型机构 {@link OrganType#organ}
     *
     * @param parentId
     * @param shortName
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "ownerAndChildsCompanysData")
    public List<TreeNode> ownerAndChildsCompanysData(String parentId,
                                                     Boolean shortName,
                                                     HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        String _parentId = parentId;
        if (StringUtils.isBlank(_parentId)) {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            _parentId = SecurityUtils.isPermittedMaxRoleDataScope() ? null : sessionInfo.getLoginCompanyId();
        }
        return organService.findOwnerAndChildsCompanysTree(_parentId,shortName);
    }


    /**
     * 省市县区域数据
     *
     * @param shortName
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "provinceCityAreaData")
    public List<TreeNode> provinceCityAreaData(Boolean shortName,HttpServletResponse response) {
        List<TreeNode> treeNodes = Lists.newArrayList();
        List<Area> list = areaService.findAreaUp();
        for (int i = 0; i < list.size(); i++) {
            Area e = list.get(i);
            TreeNode treeNode = new TreeNode(e.getId(), null != shortName && shortName ? e.getShortName():e.getName());
            treeNode.setpId(e.getParentId());
            treeNodes.add(treeNode);
        }
        return AppUtils.toTreeTreeNodes(treeNodes);
    }

    /**
     * 同步机构所有父级ID
     *
     * @return
     */
    @RequiresPermissions("sys:organ:sync")
    @Logging(value = "机构管理-同步机构所有父级ID", logType = LogType.access)
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "syncAllParentIds")
    public Result syncAllParentIds() {
        organService.syncAllParentIds();
        return Result.successResult();
    }

    /**
     * 重新初始化机构系统编码
     *
     * @return
     */
    @RequiresPermissions("sys:organ:sync")
    @Logging(value = "机构管理-重新初始化机构系统编码", logType = LogType.access)
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "initSysCode")
    public Result initSysCode() {
        List<Organ> roots = organService.findRoots();
        for (int i = 0; i < roots.size(); i++) {
            Organ organ = roots.get(i);
            rOrgan(organ, "360" + i, 0);
        }
        return Result.successResult();
    }

    //递归
    private void rOrgan(Organ organ, String prefix, int index) {
        String code = prefix + String.format("%02d", index);
        organ.setSysCode(code);
        organService.saveOrgan(organ);
        List<Organ> childOrgans = organService.findByParent(organ.getId(), DataEntity.STATUS_NORMAL);
        index = 0;
        for (Organ childOrgan : childOrgans) {
            prefix = organ.getSysCode();
            rOrgan(childOrgan, prefix, index++);
        }
    }


    /**
     * 同步到扩展表
     *
     * @return
     */
    @RequiresPermissions("sys:organ:sync")
    @Logging(value = "机构管理-同步到扩展表", logType = LogType.access)
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = "syncToExtend")
    public Result syncToExtend() {
        logger.info("定时任务...开始：同步organ扩展表");
        systemService.syncOrganToExtend();
        logger.info("定时任务...结束：同步organ扩展表");
        return Result.successResult();
    }


    /**
     * 详细信息
     *
     * @param model
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") Organ model) {
        return Result.successResult().setObj(model);
    }

}
