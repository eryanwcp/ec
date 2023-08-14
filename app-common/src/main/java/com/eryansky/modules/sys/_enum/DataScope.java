package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 数据范围
 */
public enum DataScope implements IGenericEnum<DataScope> {

    ALL("1", "所有数据"),
    HOME_COMPANY_AND_CHILD("2", "所在母公司及以下数据"),
    HOME_COMPANY("3", "所在母公司数据"),
    COMPANY_AND_CHILD("4", "所在公司及以下数据"),
    COMPANY("5", "所在公司数据"),
    OFFICE_AND_CHILD("6", "所在部门及以下数据"),
    OFFICE("7", "所在部门数据"),
    SELF("8", "仅本人数据"),
    CUSTOM("9", "按明细设置");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    DataScope(String value, String description) {
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

    public static DataScope getByValue(String value) {
        return GenericEnumUtils.getByValue(DataScope.class,value);
    }

    public static DataScope getByDescription(String description) {
        return GenericEnumUtils.getByDescription(DataScope.class,description);
    }
}
