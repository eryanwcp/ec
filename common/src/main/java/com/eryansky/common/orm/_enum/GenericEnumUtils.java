package com.eryansky.common.orm._enum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 泛型枚举工具类
 * @author Eryan
 * @version 2020-02-12
 */
public class GenericEnumUtils {

    /**
     * 是否是有效的枚举类型
     *
     * @param enumType
     * @param value
     * @param <T>
     * @return
     */
    public static <T extends Enum<T> & IGenericEnum<T>> boolean isValidValue(Class<T> enumType, String value) {
        for (IGenericEnum<T> enumOption : enumType.getEnumConstants()) {
            if (enumOption.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 枚举类Value拼接
     *
     * @param enumType
     * @param delimiter
     * @param <T>
     * @return
     */
    public static <T extends Enum<T> & IGenericEnum<T>> String getValueStrings(Class<T> enumType, String delimiter) {
        StringBuilder ret = new StringBuilder();
        delimiter = delimiter == null ? " " : delimiter;

        int i = 0;
        for (IGenericEnum<T> enumOption : enumType.getEnumConstants()) {
            if (i == 0) {
                ret = new StringBuilder(enumOption.getValue());
            } else {
                ret.append(delimiter).append(enumOption.getValue());
            }
            i++;
        }

        return ret.toString();
    }

    /**
     * 枚举类List
     *
     * @param enumType
     * @param <T>
     * @return
     */
    public static <T extends Enum<T> & IGenericEnum<T>> Collection<IGenericEnum<T>> getList(Class<T> enumType) {
        List<IGenericEnum<T>> list = new ArrayList<>();
        for (IGenericEnum<T> enumOption : enumType.getEnumConstants()) {
            list.add(enumOption);
        }
        return list;
    }

    /**
     * 枚举类Value List
     *
     * @param enumType
     * @param <T>
     * @return
     */
    public static <T extends Enum<T> & IGenericEnum<T>> Collection<String> getValueList(Class<T> enumType) {
        List<String> list = new ArrayList<String>();
        for (IGenericEnum<T> enumOption : enumType.getEnumConstants()) {
            list.add(enumOption.getValue());
        }
        return list;
    }

    /**
     * 根据value查找
     *
     * @param enumType
     * @param value
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T> & IGenericEnum<T>> T getByValue(Class<T> enumType, String value) {
        if (null == value)
            return null;
        for (IGenericEnum<T> _enum : enumType.getEnumConstants()) {
            if (value.equals(_enum.getValue()))
                return (T) _enum;
        }
        return null;
    }

    /**
     * 根据description查找
     *
     * @param enumType
     * @param description
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T> & IGenericEnum<T>> T getByDescription(Class<T> enumType, String description) {
        if (null == description)
            return null;
        for (IGenericEnum<T> _enum : enumType.getEnumConstants()) {
            if (description.equals(_enum.getDescription()))
                return (T) _enum;
        }
        return null;
    }


    /**
     * 根据value转换为description
     * @param enumType
     * @param value
     * @param defaultDescription
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T> & IGenericEnum<T>> String getDescriptionByValue(Class<T> enumType, String value,String defaultDescription) {
        if (null == value)
            return defaultDescription;
        for (IGenericEnum<T> _enum : enumType.getEnumConstants()) {
            if (value.equals(_enum.getValue()))
                return _enum.getDescription();
        }
        return defaultDescription;
    }
}