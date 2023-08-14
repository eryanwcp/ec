/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.mapper;

import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * 邮件联系人
 *
 * @author Eryan
 * @date 2015-08-13
 */
public class MailContact extends DataEntity<MailContact> {

    /**
     * 所属用户
     */
    private String userId;

    /**
     * 显示名称
     */
    private String name;
    /**
     * 邮件地址
     */
    private String email;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 备注
     */
    private String remark;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    @JsonIgnore
    public MailContact copy(String contactGroupId){
        MailContact mailContact = new MailContact();
        mailContact.setName(this.name);
        mailContact.setEmail(this.email);
        mailContact.setRemark(this.remark);
        mailContact.setUserId(this.getUserId());
        return mailContact;
    }
}