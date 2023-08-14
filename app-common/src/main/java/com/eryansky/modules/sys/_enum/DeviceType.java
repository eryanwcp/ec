package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 设备类型
 */
public enum DeviceType implements IGenericEnum<DeviceType> {

    UNKNOWN("0", "UNKNOWN"),
    iPhone("1", "iPhone"),
    iPad("2", "iPad"),
    Android("3", "Android"),
    WinPhone("4", "WinPhone"),
    PC("5", "PC");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    DeviceType(String value, String description) {
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

    public static DeviceType getByValue(String value) {
        return GenericEnumUtils.getByValue(DeviceType.class,value);
    }

    public static DeviceType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(DeviceType.class,description);
    }

}