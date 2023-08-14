/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 机构类型
 *
 * @author Eryan
 * @version 1.0
 * @date 2013-9-9 下午8:09:38
 */
public enum OrganType implements IGenericEnum<OrganType> {
    /**
     * 机构(0)
     */
    organ("0", "机构(法人单位)"),
    /**
     * 部门(1)
     */
    department("1", "部门"),
    /**
     * 小组(2)
     */
    group("2", "小组");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    OrganType(String value, String description) {
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

    public static OrganType getByValue(String value) {
        return GenericEnumUtils.getByValue(OrganType.class,value);
    }

    public static OrganType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(OrganType.class,description);
    }

}