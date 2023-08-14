/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.mapper;

import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.modules.notice._enum.ContactGroupType;
import com.eryansky.modules.sys.utils.UserUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 自定义联系人组
 * @author Eryan
 * @date 2014-11-05
 */
public class ContactGroup extends DataEntity<ContactGroup> {

    /**
     * 联系人组类型 {@link ContactGroupType}
     */
    private String contactGroupType = ContactGroupType.System.getValue();
    /**
     * 默认组 每种类型的联系人组仅1个为默认值true
     */
    private Boolean isDefault = Boolean.FALSE;
    /**
     * 联系人组名称
     */
    private String name;
    /**
     * 备注
     */
    private String remark;
    /**
     * 所属用户
     */
    private String userId;
    /**
     * 来源用户 用于标识共享用户组
     */
    private String originUserId;
    /**
     * 系统用户ID MailType.System ${@link com.eryansky.modules.sys.mapper.User}
     * 邮件账户ID MailType.Mail ${@link MailContact}
     *
     */
    private List<String> objectIds = Lists.newArrayList();
    /**
     * 排序号
     */
    private Integer sort;


    public ContactGroup() {}

    public ContactGroup(String id) {
        super(id);
    }

    public String getContactGroupType() {
        return contactGroupType;
    }

    public void setContactGroupType(String contactGroupType) {
        this.contactGroupType = contactGroupType;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOriginUserId() {
        return originUserId;
    }

    public void setOriginUserId(String originUserId) {
        this.originUserId = originUserId;
    }

    public List<String> getObjectIds() {
        return objectIds;
    }

    public void setObjectIds(List<String> objectIds) {
        this.objectIds = objectIds;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getUserName() {
        return UserUtils.getUserName(userId);
    }

    /**
     * 类型描述.
     */
    public String getContactTypeView() {
        ContactGroupType ss = ContactGroupType.getByValue(contactGroupType);
        String str = "";
        if (ss != null) {
            str = ss.getDescription();
        }
        return str;
    }

    @JsonIgnore
    public ContactGroup copy(String userId){
        ContactGroup contactGroup = new ContactGroup();
        contactGroup.setName(this.name);
        contactGroup.setContactGroupType(contactGroupType);
        contactGroup.setOriginUserId(this.userId);
        contactGroup.setObjectIds(this.objectIds);
        contactGroup.setSort(this.sort);
        contactGroup.setRemark(this.remark);
        contactGroup.setUserId(userId);
        return contactGroup;
    }
}
