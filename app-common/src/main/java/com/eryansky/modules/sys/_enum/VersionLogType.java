/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 日志类型
 */
public enum VersionLogType implements IGenericEnum<VersionLogType> {
    /**
     * Server(0)
     */
    Server("0", "服务器应用"),
    /**
     * Android(1)
     */
    Android("1", "Android应用"),
    /**
     * iPhone(2)
     */
    iPhoneAPP("2", "iPhone应用"),
    /**
     * iPhone(3)
     */
    iPhone("3", "iPhone下载");
    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    VersionLogType(String value, String description) {
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

    public static VersionLogType getByValue(String value) {
        return GenericEnumUtils.getByValue(VersionLogType.class,value);
    }

    public static VersionLogType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(VersionLogType.class,description);
    }

}