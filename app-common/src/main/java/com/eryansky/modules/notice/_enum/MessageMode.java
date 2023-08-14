/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 消息状态
 */
public enum MessageMode implements IGenericEnum<MessageMode> {

    Draft("00", "草稿"),
    Publishing("02", "正在发布"),
    Published("01", "已发布");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    MessageMode(String value, String description) {
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

    public static MessageMode getByValue(String value) {
        return GenericEnumUtils.getByValue(MessageMode.class,value);
    }

    public static MessageMode getByDescription(String description) {
        return GenericEnumUtils.getByDescription(MessageMode.class,description);
    }
}
