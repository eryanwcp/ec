/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;


import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.core.orm.mybatis.entity.TreeEntity;
import com.eryansky.modules.sys._enum.OrganType;
import com.eryansky.modules.sys.utils.DictionaryUtils;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.client.common.vo.ExtendAttr;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 机构
 *
 * @author Eryan
 * @date 2018-05-08
 */
@JsonFilter(" ")
public class Organ extends TreeEntity<Organ> {

    public static final String DIC_ORGAN_TYPE = "SYS_ORGAN_TYPE_EXTEND";//扩展自定义机构类型数据字典编码

    /**
     * 简称
     */
    private String shortName;
    /**
     * 机构类型 {@link OrganType} 以及 {@link Organ#DIC_ORGAN_TYPE}
     */
    private String type;
    /**
     * 机构编码
     */
    private String code;
    /**
     * 分类编码
     */
    private String bizCode;
    /**
     * 机构系统编码
     */
    private String sysCode;
    /**
     * 地址
     */
    private String address;
    /**
     * 电话号码
     */
    private String mobile;
    /**
     * 电话号码
     */
    private String phone;
    /**
     * 传真
     */
    private String fax;
    /**
     * 机构负责人ID
     */
    private String managerUserId;
    /**
     * 副主管
     */
    private String deputyManagerUserId;
    /**
     * 分管领导
     */
    private String superManagerUserId;
    /**
     * 区域ID
     */
    private String areaId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 自定义扩展数据
     */
    private ExtendAttr extendAttr;

    public Organ() {
    }

    public Organ(String id) {
        super(id);
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    @Override
    public Organ getParent() {
        return parent;
    }

    @Override
    public void setParent(Organ parent) {
        this.parent = parent;
    }

    public String getName() {
        return this.name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getSysCode() {
        return this.sysCode;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getFax() {
        return this.fax;
    }

    public void setManagerUserId(String managerUserId) {
        this.managerUserId = managerUserId;
    }

    public String getManagerUserId() {
        return this.managerUserId;
    }

    public void setDeputyManagerUserId(String deputyManagerUserId) {
        this.deputyManagerUserId = deputyManagerUserId;
    }

    public String getDeputyManagerUserId() {
        return this.deputyManagerUserId;
    }

    public void setSuperManagerUserId(String superManagerUserId) {
        this.superManagerUserId = superManagerUserId;
    }

    public String getSuperManagerUserId() {
        return this.superManagerUserId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getAreaId() {
        return this.areaId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public ExtendAttr getExtendAttr() {
        return extendAttr;
    }

    public void setExtendAttr(ExtendAttr extendAttr) {
        this.extendAttr = extendAttr;
    }

    @Override
    public String getParentId() {
        return super.getParentId();
    }

    @JsonProperty(value = "_parentId")
    public String get_parentId() {
        String id = null;
        if (parent != null) {
            id = parent.getId().equals("0") ? null : parent.getId();
        }
        return id;
    }

    /**
     * 主管名称
     *
     * @return
     */
    public String getManagerUserName() {
        return UserUtils.getUserName(managerUserId);
    }


    /**
     * 分管领导名称
     *
     * @return
     */
    public String getSuperManagerUserName() {
        return UserUtils.getUserName(superManagerUserId);
    }


    /**
     * Treegrid 关闭状态设置
     *
     * @return
     */
    public String getState() {
        return OrganUtils.hasChild(id) ? TreeNode.STATE_CLOASED : TreeNode.STATE_OPEN;
    }


    /**
     * 机构类型显示.
     */
    public String getTypeView() {
        String typeView = GenericEnumUtils.getDescriptionByValue(OrganType.class,type,null);
        if(null == typeView){
            typeView = DictionaryUtils.getDictionaryNameByDC(DIC_ORGAN_TYPE,type,type);
        }
        return typeView;
    }

    public String getAttr(String key) {
        return null != this.extendAttr ? (String) this.extendAttr.get(key):null;
    }

    public Organ setAttr(String key,Object value) {
        if(null != extendAttr){
            this.extendAttr.put(key,value);
        }
        return this;
    }
}
