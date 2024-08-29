/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
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
    /**
     * 父级ID
     */

    private String parentId;
    private String parentCode;

    private String parentBizCode;
    private String parentName;
    /**
     * 所属公司ID
     */
    private String companyId;
    private String companyCode;

    private String companyBizCode;
    private String companyName;

    /**
     * 管理单元ID
     */
    private String homeCompanyId;
    private String homeCompanyCode;

    private String homeCompanyBizCode;
    private String homeCompanyName;
    /**
     * 行政区划编码
     */
    private String areaCode;
    /**
     * 行政区划信息分类编码
     */
    private String areaBizCode;
    /**
     * 行政区划名称
     */
    private String areaName;

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

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getParentBizCode() {
        return parentBizCode;
    }

    public void setParentBizCode(String parentBizCode) {
        this.parentBizCode = parentBizCode;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
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

    public String getCompanyBizCode() {
        return companyBizCode;
    }

    public void setCompanyBizCode(String companyBizCode) {
        this.companyBizCode = companyBizCode;
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

    public String getHomeCompanyBizCode() {
        return homeCompanyBizCode;
    }

    public void setHomeCompanyBizCode(String homeCompanyBizCode) {
        this.homeCompanyBizCode = homeCompanyBizCode;
    }

    public String getHomeCompanyName() {
        return homeCompanyName;
    }

    public void setHomeCompanyName(String homeCompanyName) {
        this.homeCompanyName = homeCompanyName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaBizCode() {
        return areaBizCode;
    }

    public void setAreaBizCode(String areaBizCode) {
        this.areaBizCode = areaBizCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
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
