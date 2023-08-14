/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;
import com.eryansky.modules.sys._enum.AreaType;

/**
 * 是否置顶
 */
public enum IsTop implements IGenericEnum<IsTop> {
    /**
     * 不置顶(0)
     */
    No("0", "不置顶"),
    /**
     * 置顶(1)
     */
    Yes("1", "置顶");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    IsTop(String value, String description) {
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

    public static IsTop getByValue(String value) {
        return GenericEnumUtils.getByValue(IsTop.class,value);
    }

    public static IsTop getByDescription(String description) {
        return GenericEnumUtils.getByDescription(IsTop.class,description);
    }
}