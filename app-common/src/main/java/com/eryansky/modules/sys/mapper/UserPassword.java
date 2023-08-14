/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;


import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.modules.sys._enum.SexType;
import com.eryansky.modules.sys._enum.UserPasswordUpdateType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Calendar;
import java.util.Date;

/**
 * 用户密码修改记录
 *
 * @author Eryan
 * @date 2018-05-08
 */
public class UserPassword extends DataEntity<UserPassword> {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 密码修改类型 {@link  UserPasswordUpdateType}
     */
    private String type;
    /**
     * 修改时间
     */
    private Date modifyTime;
    /**
     * 原始密码
     */
    private String originalPassword;
    /**
     * 密码
     */
    private String password;

    public UserPassword() {
        super();
        this.modifyTime = Calendar.getInstance().getTime();
    }

    public UserPassword(String userId, String password) {
        this();
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeView() {
        return GenericEnumUtils.getDescriptionByValue(UserPasswordUpdateType.class,type,type);
    }

    @JsonFormat(pattern = DATE_TIME_SHORT_FORMAT, timezone = TIMEZONE)
    public Date getModifyTime() {
        return this.modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getOriginalPassword() {
        return this.originalPassword;
    }

    public void setOriginalPassword(String originalPassword) {
        this.originalPassword = originalPassword;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
