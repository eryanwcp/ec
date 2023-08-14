/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 用户密码修改类型
 */
public enum UserPasswordUpdateType implements IGenericEnum<UserPasswordUpdateType> {

    SystemReset("0", "重置"),
    UserInit("1", "初始化"),
    UserUpdate("2", "安全修改");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    UserPasswordUpdateType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 获取值
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取描述信息
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    public static UserPasswordUpdateType getByValue(String value) {
        return GenericEnumUtils.getByValue(UserPasswordUpdateType.class,value);
    }

    public static UserPasswordUpdateType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(UserPasswordUpdateType.class,description);
    }

}