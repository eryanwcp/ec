package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 角色权限类型
 */
public enum RoleType implements IGenericEnum<RoleType> {

    USER("user", "普通角色"),
    SECURITY_ROLE("security-role", "管理角色");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    RoleType(String value, String description) {
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

    public static RoleType getByValue(String value) {
        return GenericEnumUtils.getByValue(RoleType.class,value);
    }

    public static RoleType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(RoleType.class,description);
    }
}
