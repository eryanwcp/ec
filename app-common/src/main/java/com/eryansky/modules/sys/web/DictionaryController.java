/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
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
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.mapper.Dictionary;
import com.eryansky.modules.sys.service.DictionaryItemService;
import com.eryansky.modules.sys.service.DictionaryService;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 数据字典Dictionary管理 Controller层.
 *
 * @author Eryan
 * @date 2012-10-11 下午4:36:24
 */
@SuppressWarnings("serial")
@Controller
@RequestMapping(value = "${adminPath}/sys/dictionary")
public class DictionaryController extends SimpleController {


    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private DictionaryItemService dictionaryItemService;

    @ModelAttribute
    public Dictionary get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return dictionaryService.get(id);
        } else {
            return new Dictionary();
        }
    }

    @RequiresPermissions("sys:dictionary:view")
    @Logging(value = "字典管理", logType = LogType.access)
    @GetMapping(value = {""})
    public String list() {
        return "modules/sys/dictionary";
    }



    @GetMapping(value = {"input"})
    public ModelAndView input(@ModelAttribute Dictionary model) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/dictionary-input");
        modelAndView.addObject("model", model);
        return modelAndView;
    }

    @RequiresPermissions("sys:dictionary:edit")
    @Logging(value = "字典管理-保存字典", logType = LogType.access)
    @PostMapping(value = {"save"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result save(@ModelAttribute("model") Dictionary dictionary) {
        Result result = null;
        // 编码是否重复校验
        Dictionary checkCodeDictionary = dictionaryService.getByCode(dictionary.getCode());
        if (checkCodeDictionary != null
                && !checkCodeDictionary.getId().equals(dictionary.getId())) {
            result = new Result(Result.WARN, "编码为["
                    + dictionary.getCode() + "]已存在,请修正!", "code");
            logger.debug(result.toString());
            return result;
        }
        //修改操作 避免自关联数据的产生
        if (StringUtils.isNotBlank(dictionary.getId())) {
            if (dictionary.getId().equals(dictionary.getGroupId())) {
                result = new Result(Result.ERROR, "不允许发生自关联.",
                        null);
                logger.debug(result.toString());
                return result;
            }
        }

        dictionaryService.save(dictionary);
        result = Result.successResult();
        logger.debug(result.toString());
        return result;
    }


    /**
     * 下拉列表
     */
    @PostMapping(value = {"comboboxGroup"})
    @ResponseBody
    public List<Combobox> combobox(String selectType) {
        List<Dictionary> list = dictionaryService.findParents();
        List<Combobox> cList = Lists.newArrayList();

        Combobox selectCombobox = SelectType.combobox(selectType);
        if (selectCombobox != null) {
            cList.add(selectCombobox);
        }
        for (Dictionary d : list) {
            List<Dictionary> subDictionaries = dictionaryService.findChilds(d.getId());
            for (Dictionary subDictionary : subDictionaries) {
                Combobox combobox = new Combobox(subDictionary.getId(), subDictionary.getName(), d.getName());
                combobox.setData(subDictionary.getCode());
                cList.add(combobox);
            }

        }
        return cList;
    }


    /**
     * 下拉列表
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"tree"})
    @ResponseBody
    public List<TreeNode> tree(String selectType) {
        List<Dictionary> list = dictionaryService.findParents();
        List<TreeNode> tList = Lists.newArrayList();

        TreeNode selectTreeNode = SelectType.treeNode(selectType);
        if (selectTreeNode != null) {
            tList.add(selectTreeNode);
        }
        for (Dictionary d : list) {
            TreeNode treeNode = new TreeNode(d.getId(), d.getName(), null);
            tList.add(treeNode);
            List<Dictionary> childDictionaries = dictionaryService.findChilds(d.getId());
            for (Dictionary childDictionarie : childDictionaries) {
                TreeNode childTreeNode = new TreeNode(childDictionarie.getId(), childDictionarie.getName(), null);
                childTreeNode.getAttributes().put("groupId", childDictionarie.getGroupId());
                treeNode.addChild(childTreeNode);
            }

        }
        return tList;
    }

    /**
     * 分组下拉列表
     */
    @PostMapping(value = {"groupCombobox"})
    @ResponseBody
    public List<Combobox> groupCombobox(String selectType) {
        List<Dictionary> list = dictionaryService.findParents();
        List<Combobox> cList = Lists.newArrayList();

        Combobox selectCombobox = SelectType.combobox(selectType);
        if (selectCombobox != null) {
            cList.add(selectCombobox);
        }
        for (Dictionary d : list) {
            Combobox combobox = new Combobox(d.getId(), d.getName());
            cList.add(combobox);
        }
        return cList;
    }


    /**
     * 排序最大值.
     */
    @PostMapping(value = {"maxSort"})
    @ResponseBody
    public Result maxSort() {
        Integer maxSort = dictionaryService.getMaxSort();
        return new Result(Result.SUCCESS, null, maxSort);
    }

    /**
     * 数据列表. 无分页.
     *
     * @return
     */
    @PostMapping(value = {"treegrid"})
    @ResponseBody
    public Datagrid<Dictionary> treegrid(Dictionary Dictionary, HttpServletRequest request, HttpServletResponse response) {
        Page<Dictionary> page = new Page<>(request);
        page = dictionaryService.findPage(page, Dictionary);
        return new Datagrid<>(page.getTotalCount(), page.getResult());
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @RequiresPermissions("sys:dictionary:edit")
    @Logging(value = "字典管理-删除字典", logType = LogType.access)
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        dictionaryService.deleteByIds(ids);
        return Result.successResult();
    }

    /* 外部接口*/

    /**
     * combobox下拉列表框数据
     *
     * @param selectType     {@link SelectType}
     * @param dictionaryCode 数据字典编码
     * @return
     */
    @SuppressWarnings("unchecked")
    @PostMapping(value = {"combobox"})
    @ResponseBody
    public List<Combobox> combobox(String selectType, String dictionaryCode) {
        List<Combobox> titleList = Lists.newArrayList();
        Combobox selectCombobox = SelectType.combobox(selectType);
        if (selectCombobox != null) {
            titleList.add(selectCombobox);
        }

        List<Combobox> cList = dictionaryItemService.findTreeNodeByDictionaryCode(dictionaryCode);
        return ListUtils.union(titleList, cList);
    }

    /**
     * combotree下拉列表数据
     *
     * @param selectType     {@link SelectType}
     * @param dictionaryCode 数据字典编码
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"combotree"})
    @ResponseBody
    public List<TreeNode> combotree(String selectType, String dictionaryCode) {
        List<TreeNode> titleList = Lists.newArrayList();
        TreeNode selectTreeNode = SelectType.treeNode(selectType);
        if (selectTreeNode != null) {
            titleList.add(selectTreeNode);
        }
        List<TreeNode> treeNodes = dictionaryItemService.findTreeNodeByDictionaryCode(dictionaryCode, true);
        return ListUtils.union(titleList, treeNodes);
    }

    /*外部接口*/

}
