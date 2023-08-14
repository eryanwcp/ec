package com.eryansky.modules.notice._enum;

/**
 * 联系人组类型
 */
public enum ContactGroupType {
    /**
     * 系统(0)
     */
    System("0", "系统"),
    /**
     * 邮件(1)
     */
    Mail("1", "邮件");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    ContactGroupType(String value, String description) {
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

    public static ContactGroupType getByValue(String value) {
        if (null == value)
            return null;
        for (ContactGroupType _enum : ContactGroupType.values()) {
            if (value.equals(_enum.getValue()))
                return _enum;
        }
        return null;
    }

    public static ContactGroupType getByDescription(String description) {
        if (null == description)
            return null;
        for (ContactGroupType _enum : ContactGroupType.values()) {
            if (description.equals(_enum.getDescription()))
                return _enum;
        }
        return null;
    }
}