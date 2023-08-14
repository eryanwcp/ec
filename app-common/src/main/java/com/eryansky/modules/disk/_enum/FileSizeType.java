package com.eryansky.modules.disk._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

public enum FileSizeType implements IGenericEnum<FileSizeType> {

    MIN("0", "10M以下"),
    MIDDEN("1", "10M~100M"),
    MAX("2", "100M以上");

    private final String value;
    private final String description;

    FileSizeType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static FileSizeType getByValue(String value) {
        return GenericEnumUtils.getByValue(FileSizeType.class,value);
    }

    public static FileSizeType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(FileSizeType.class,description);
    }
}
