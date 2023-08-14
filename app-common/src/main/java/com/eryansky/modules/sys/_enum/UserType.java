/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 用户类型
 */
public enum UserType implements IGenericEnum<UserType> {

    System("0", "系统"),
    User("1", "员工"),
    Platform("2", "统一平台");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    UserType(String value, String description) {
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

    public static UserType getByValue(String value) {
        return GenericEnumUtils.getByValue(UserType.class,value);
    }

    public static UserType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(UserType.class,description);
    }

}