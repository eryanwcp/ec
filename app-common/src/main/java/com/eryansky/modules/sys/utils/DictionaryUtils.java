/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.modules.sys.mapper.DictionaryItem;
import com.eryansky.modules.sys.service.DictionaryItemService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 数据字典工具类
 *
 * @author Eryan
 * @date : 2014-05-17 21:22
 */
public class DictionaryUtils {

    private DictionaryUtils(){}

    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    public static final class Static {
        private static final DictionaryItemService dictionaryItemService = SpringContextHolder.getBean(DictionaryItemService.class);
        private Static(){}
    }

    /**
     * @param dictionaryCode 字典类型编码
     * @param code           字典项编码
     * @param defaultValue   默认数据项值
     * @return
     */
    public static String getDictionaryNameByDC(String dictionaryCode, String code, String defaultValue) {
        DictionaryItem dictionary = Static.dictionaryItemService.getDictionaryItemByDC(dictionaryCode, code);
        if (dictionary != null) {
            return dictionary.getName();
        }
        return defaultValue;
    }

    /**
     * @param dictionaryCode 字典类型编码
     * @param value          字典项值
     * @param defaultValue   默认数据项值
     * @return
     */
    public static String getDictionaryNameByDV(String dictionaryCode, String value, String defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        DictionaryItem dictionaryItem = Static.dictionaryItemService.getDictionaryItemByDV(dictionaryCode, value);
        if (dictionaryItem != null) {
            defaultValue = dictionaryItem.getName();
        }
        return defaultValue;
    }

    /**
     * 根据字典类型编码获取字典项列表
     *
     * @param dictionaryCode 类型编码
     * @return
     */
    public static List<DictionaryItem> getDictList(String dictionaryCode) {
        return Static.dictionaryItemService.findListByDictionaryCode(dictionaryCode);
    }


    /**
     * 获取字典对应的值
     *
     * @param dictionaryName 字典项显示名称
     * @param dictionaryCode 类型编码
     * @param defaultValue    默认值
     * @return
     */
    public static String getDictionaryValue(String dictionaryName, String dictionaryCode, String defaultValue) {
        if (StringUtils.isNotBlank(dictionaryCode) && StringUtils.isNotBlank(dictionaryName)) {
            for (DictionaryItem item : getDictList(dictionaryCode)) {
                if (dictionaryName.equals(item.getName())) {
                    return item.getValue();
                }
            }
        }
        return defaultValue;
    }

}