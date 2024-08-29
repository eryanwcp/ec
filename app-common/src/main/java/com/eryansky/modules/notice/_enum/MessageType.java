/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 消息类型
 */
public enum MessageType implements IGenericEnum<MessageType> {

    Text("text", "文本"),
    TextCard("textcard", "文本卡片");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    MessageType(String value, String description) {
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

    public static MessageType getByValue(String value) {
        return GenericEnumUtils.getByValue(MessageType.class,value);
    }

    public static MessageType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(MessageType.class,description);
    }
}