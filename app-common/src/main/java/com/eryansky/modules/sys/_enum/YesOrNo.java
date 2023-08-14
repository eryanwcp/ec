package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 是否 枚举类型
 */
public enum YesOrNo implements IGenericEnum<YesOrNo> {

    YES("1", "是"),
    NO("0", "否");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    YesOrNo(String value, String description) {
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

    public static YesOrNo getByValue(String value) {
        return GenericEnumUtils.getByValue(YesOrNo.class,value);
    }

    public static YesOrNo getByDescription(String description) {
        return GenericEnumUtils.getByDescription(YesOrNo.class,description);
    }
}
