/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 通知接收范围
 */
public enum NoticeReceiveScope implements IGenericEnum<NoticeReceiveScope> {

    COMPANY_AND_CHILD("2", "所在单位及以下"),
    COMPANY("3", "所在单位"),
    OFFICE_AND_CHILD("4", "所在部门及以下"),
    OFFICE("5", "所在部门"),
    ALL("10", "所有"),
    CUSTOM("0", "自定义");
    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    NoticeReceiveScope(String value, String description) {
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

    public static NoticeReceiveScope getByValue(String value) {
        return GenericEnumUtils.getByValue(NoticeReceiveScope.class,value);
    }

    public static NoticeReceiveScope getByDescription(String description) {
        return GenericEnumUtils.getByDescription(NoticeReceiveScope.class,description);
    }
}