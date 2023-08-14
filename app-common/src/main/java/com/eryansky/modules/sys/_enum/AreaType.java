package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 区域类型
 */
public enum AreaType implements IGenericEnum<AreaType> {

    COUNTRY("1", "国家"),
    PROVINCE("2", "省份、直辖市"),
    CITY("3", "地市"),
    AREA("4", "区县"),
    STREET("5", "乡镇"),
    VILLAGE("6", "村庄"),
    OTHER("9", "其它");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    AreaType(String value, String description) {
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

    public static AreaType getByValue(String value) {
        return GenericEnumUtils.getByValue(AreaType.class,value);
    }

    public static AreaType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(AreaType.class,description);
    }
}
