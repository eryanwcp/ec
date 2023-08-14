/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;


import com.eryansky.common.model.TreeNode;
import com.eryansky.modules.sys.utils.OrganUtils;

/**
 * 机构扩展
 *
 * @author Eryan
 * @date 2018-05-08
 */
public class OrganExtend extends Organ {


    private Integer treeLevel;

    private String parentId;

    private String companyId;
    private String companyCode;
    private String companyName;

    private String homeCompanyId;
    private String homeCompanyCode;

    private String isLeaf;

    public Integer getTreeLevel() {
        return treeLevel;
    }

    public void setTreeLevel(Integer treeLevel) {
        this.treeLevel = treeLevel;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getHomeCompanyId() {
        return homeCompanyId;
    }

    public void setHomeCompanyId(String homeCompanyId) {
        this.homeCompanyId = homeCompanyId;
    }

    public String getHomeCompanyCode() {
        return homeCompanyCode;
    }

    public void setHomeCompanyCode(String homeCompanyCode) {
        this.homeCompanyCode = homeCompanyCode;
    }

    public String getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(String isLeaf) {
        this.isLeaf = isLeaf;
    }


    /**
     * Treegrid 关闭状态设置
     *
     * @return
     */
    public String getState() {
        return Boolean.parseBoolean(isLeaf) ? TreeNode.STATE_OPEN : TreeNode.STATE_CLOASED;
    }
}
