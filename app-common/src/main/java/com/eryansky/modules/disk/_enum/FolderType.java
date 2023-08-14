package com.eryansky.modules.disk._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 * 文件夹类型
 */
public enum FolderType implements IGenericEnum<FolderType> {

    NORMAL("0", "正常"),
    HIDE("1", "隐藏");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    FolderType(String value, String description) {
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


    public static FolderType getByValue(String value) {
        return GenericEnumUtils.getByValue(FolderType.class,value);
    }

    public static FolderType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(FolderType.class,description);
    }
}