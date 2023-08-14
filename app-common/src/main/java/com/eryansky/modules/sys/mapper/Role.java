/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;


import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.modules.sys._enum.DataScope;
import com.eryansky.modules.sys._enum.RoleType;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 角色表
 *
 * @author Eryan
 * @date 2018-05-08
 */
@JsonFilter(" ")
public class Role extends DataEntity<Role> {

    /**
     * 名称
     */
    private String name;
    /**
     * 编码
     */
    private String code;
    /**
     * 所属机构ID
     */
    private String organId;
    /**
     * 是否系统角色 系统角色：只有超级管理员可修改 （1/0 是/否）
     */
    private String isSystem;
    /**
     * 是否有效 （1/0 是 否）
     */
    private String isActivity;
    /**
     * 权限类型 {@link RoleType}
     */
    private String roleType;
    /**
     * 数据范围 {@link DataScope}
     */
    private String dataScope;
    /**
     * 备注
     */
    private String remark;

    /**
     * 按明细设置数据范围 授权机构
     */
    private Set<String> organIds;

    /**
     * 关键字
     */
    private String query;

    public Role() {
        this.isSystem = YesOrNo.NO.getValue();
        this.isActivity = YesOrNo.YES.getValue();
        this.organIds = Sets.newHashSet();
    }

    public Role(String id) {
        super(id);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setOrganId(String organId) {
        this.organId = organId;
    }

    public String getOrganId() {
        return this.organId;
    }

    public void setIsSystem(String isSystem) {
        this.isSystem = isSystem;
    }

    public String getIsSystem() {
        return this.isSystem;
    }

    public void setIsActivity(String isActivity) {
        this.isActivity = isActivity;
    }

    public String getIsActivity() {
        return this.isActivity;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getRoleType() {
        return this.roleType;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public String getDataScope() {
        return this.dataScope;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return this.remark;
    }


    public String getOrganName() {
        return OrganUtils.getOrganName(organId);
    }

    public String getDataScopeView() {
        return GenericEnumUtils.getDescriptionByValue(DataScope.class,dataScope,dataScope);
    }

    public String getRoleTypeView() {
        return GenericEnumUtils.getDescriptionByValue(RoleType.class,roleType,roleType);
    }

    public String getIsSystemView() {
        return GenericEnumUtils.getDescriptionByValue(YesOrNo.class,isSystem,isSystem);
    }

    public Set<String> getOrganIds() {
        return organIds;
    }

    public void setOrganIds(Set<String> organIds) {
        this.organIds = organIds;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
