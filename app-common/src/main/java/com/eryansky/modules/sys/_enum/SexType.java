/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 性别标识 枚举类型.
 *
 * @author Eryan
 * @date 2012-8-11 下午10:48:23
 */
public enum SexType implements IGenericEnum<SexType> {
    /**
     * 女(0)
     */
    girl("0", "女"),
    /**
     * 男(1)
     */
    boy("1", "男"),
    /**
     * 保密(2)
     */
    secrecy("2", "保密");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    SexType(String value, String description) {
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

    public static SexType getByValue(String value) {
        return GenericEnumUtils.getByValue(SexType.class,value);
    }

    public static SexType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(SexType.class,description);
    }

}