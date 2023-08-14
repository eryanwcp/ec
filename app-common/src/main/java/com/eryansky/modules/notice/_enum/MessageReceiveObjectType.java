/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 消息接收对象类型
 */
public enum MessageReceiveObjectType implements IGenericEnum<MessageReceiveObjectType> {

    Organ("organ", "机构"),
    Member("member", "会员"),
    User("user", "用户");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    MessageReceiveObjectType(String value, String description) {
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

    public static MessageReceiveObjectType getByValue(String value) {
        return GenericEnumUtils.getByValue(MessageReceiveObjectType.class,value);
    }

    public static MessageReceiveObjectType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(MessageReceiveObjectType.class,description);
    }
}
