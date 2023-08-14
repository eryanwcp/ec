package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 序列号重置类型
 */
public enum ResetType implements IGenericEnum<ResetType> {

    Day("D", "每天重置"),
    Month("M", "月度重置"),
    //    Quarter("Q", "季度重置"),
    Year("Y", "年度重置");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    ResetType(String value, String description) {
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

    public static ResetType getByValue(String value) {
        return GenericEnumUtils.getByValue(ResetType.class,value);
    }

    public static ResetType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(ResetType.class,description);
    }
}
