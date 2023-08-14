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
public enum LogType implements IGenericEnum<LogType> {
    /**
     * 安全日志(0)
     */
    security("0", "安全"),
    /**
     * 操作日志(1)
     */
    operate("1", "操作"),
    /**
     * 访问日志(2)
     */
    access("2", "访问"),
    /**
     * 异常(3)
     */
    exception("3", "异常"),
    /**
     * REST调用
     */
    REST("R", "REST调用"),
    /**
     * RPC API调用
     */
    API("A", "API调用");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    LogType(String value, String description) {
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

    public static LogType getByValue(String value) {
        return GenericEnumUtils.getByValue(LogType.class,value);
    }

    public static LogType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(LogType.class,description);
    }

}