/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 消息提醒通道
 */
public enum MessageChannel implements IGenericEnum<MessageChannel> {

    Message("Message", "系统"),
    Mail("Mail", "邮件"),
    SMS("SMS", "短信"),
    Weixin("Weixin", "微信"),
    QYWeixin("QYWeixin", "企业微信"),
    Dingtalk("Dingtalk", "钉钉"),
    APP("APP", "APP");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    MessageChannel(String value, String description) {
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

    public static MessageChannel getByValue(String value) {
        return GenericEnumUtils.getByValue(MessageChannel.class,value);
    }

    public static MessageChannel getByDescription(String description) {
        return GenericEnumUtils.getByDescription(MessageChannel.class,description);
    }
}