/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.j2cache.CacheChannel;
import com.eryansky.j2cache.lock.DefaultLockCallback;
import com.eryansky.j2cache.lock.LockInsideExecutedException;
import com.eryansky.modules.sys.mapper.SystemSerialNumber;
import com.eryansky.modules.sys.service.SystemSerialNumberService;
import com.eryansky.modules.sys.sn.MaxSerialItem;
import com.eryansky.utils.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Eryan
 * @date 2016-05-12
 */
public class SystemSerialNumberUtils {

    private SystemSerialNumberUtils(){}

    private static final Logger logger = LoggerFactory.getLogger(SystemSerialNumberUtils.class);

    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    public static final class Static {
        private static final SystemSerialNumberService systemSerialNumberService = SpringContextHolder.getBean(SystemSerialNumberService.class);
        private static final CacheChannel cacheChannel = CacheUtils.getCacheChannel();
        private Static(){}
    }

    /**
     * @param app
     * @param moduleCode
     * @return
     */
    public static String getQueueRegion(String app,String moduleCode) {
        String queueRegion = SystemSerialNumber.QUEUE_KEY + "_" + app + "_" + moduleCode;
        return queueRegion;
    }

    /**
     * @param app
     * @param moduleCode
     * @return
     */
    public static String getLockRegion(String app,String moduleCode) {
        String queueRegion = SystemSerialNumber.LOCK_KEY + "_" + app + "_" + moduleCode;
        return queueRegion;
    }

    /**
     * @param app
     * @param moduleCode
     * @return
     */
    public static String getLockItemRegion(String app,String moduleCode) {
        String queueRegion = SystemSerialNumber.LOCK_ITEM_KEY + "_" + app + "_" + moduleCode;
        return queueRegion;
    }

    /**
     * @param id
     * @return
     */
    public static SystemSerialNumber get(String id) {
        if (StringUtils.isNotBlank(id)) {
            return Static.systemSerialNumberService.get(id);
        }
        return null;
    }

    /**
     * @param moduleCode
     * @return
     */
    public static SystemSerialNumber getByModuleCode(String moduleCode) {
        return getByModuleCode(null,moduleCode);
    }

    /**
     * @param app
     * @param moduleCode
     * @return
     */
    public static SystemSerialNumber getByModuleCode(String app,String moduleCode) {
        if (StringUtils.isNotBlank(moduleCode)) {
            return Static.systemSerialNumberService.getByCode(app,moduleCode);
        }
        return null;
    }


    /**
     * 获得当前最大值
     *
     * @param moduleCode
     * @return
     */
    public static Long getMaxSerialByModuleCode(String moduleCode) {
        return getMaxSerialByModuleCode(null,moduleCode,null);
    }

    /**
     * 获得当前最大值
     *
     * @param app
     * @param moduleCode
     * @return
     */
    public static Long getMaxSerialByModuleCode(String app,String moduleCode) {
        return getMaxSerialByModuleCode(app,moduleCode,null);
    }

    /**
     * 获得当前最大值
     *
     * @param app
     * @param moduleCode
     * @param customCategory
     * @return
     */
    public static Long getMaxSerialByModuleCode(String app,String moduleCode,String customCategory) {
        SystemSerialNumber systemSerialNumber = getByModuleCode(app,moduleCode);
        String maxSerialKey = null == customCategory ? SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL:SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL+"_"+customCategory;
        if (systemSerialNumber != null &&  null != systemSerialNumber.getMaxSerial()) {
            MaxSerialItem item = systemSerialNumber.getMaxSerial().getItems().stream().filter(v->v.getKey().equals(maxSerialKey)).findFirst().orElse(new MaxSerialItem());
            return item.getValue();
        }
        return null;
    }

    /**
     * 根据模块code生成序列号
     *
     * @param moduleCode 模块code
     * @return 序列号
     */
    public static String generateSerialNumberByModelCode(String moduleCode) {
        return generateSerialNumberByModelCode(null,moduleCode,null,null,null,null);
    }

    /**
     * 根据模块code生成序列号
     *
     * @param moduleCode 模块code
     * @return 序列号
     */
    public static String generateSerialNumberByModelCode(String moduleCode,String customCategory) {
        return generateSerialNumberByModelCode(null,moduleCode,null,null,customCategory,null);
    }


    /**
     * 根据模块code生成序列号
     *
     * @param moduleCode 模块code
     * @param customCategory 自定义分类编码
     * @param params 自定义参数
     * @return 序列号
     */
    public static String generateSerialNumberByModelCode(String moduleCode,String customCategory, Map<String,String> params) {
        return generateSerialNumberByModelCode(null,moduleCode,null,null,customCategory,params);
    }

    /**
     * 根据模块code生成序列号
     *
     * @param app APP标识
     * @param moduleCode 模块code
     * @param timeoutInSecond 获取锁超时时间 单位：秒
     * @param keyExpireSeconds 锁超时时间（使用redis有效） 单位：秒
     * @param customCategory 自定义分类编码
     * @param params 自定义分类参数
     * @return 序列号
     */
    public static String generateSerialNumberByModelCode(String app, String moduleCode, Integer timeoutInSecond, Long keyExpireSeconds, String customCategory, Map<String,String> params) {
        String _app = null == app ? SystemSerialNumber.DEFAULT_ID : app;
        String _moduleCode = null == customCategory ? moduleCode:moduleCode+"_"+customCategory;
        String queueRegion = getQueueRegion(_app,_moduleCode);
        String lockKey = getLockRegion(_app,moduleCode);//单组序列号
        synchronized (lockKey.intern()) {
            String value = Static.cacheChannel.queuePop(queueRegion);
            if (value != null) {
                return value;
            }
            String lockRegion = getLockItemRegion(_app,_moduleCode);//单组原子序列号
            String finalApp = _app;
            boolean flag = Static.cacheChannel.lock(lockRegion, null != timeoutInSecond ? timeoutInSecond : 60, null != keyExpireSeconds ? keyExpireSeconds : 180, new DefaultLockCallback<Boolean>(false, false) {
                @Override
                public Boolean handleObtainLock() {
                    List<String> list = Static.systemSerialNumberService.generatePrepareSerialNumbers(finalApp, moduleCode,customCategory,params);
                    for (String serial : list) {
                        Static.cacheChannel.queuePush(queueRegion, serial);
                    }
                    return true;
                }

                @Override
                public Boolean handleException(LockInsideExecutedException e) {
                    logger.error(e.getMessage(),e);
                    return super.handleException(e);
                }
            });
            if (!flag) {
                logger.error("生成序列号失败，{}", queueRegion);
                return null;
            }
            value = Static.cacheChannel.queuePop(queueRegion);
            return value;
        }

    }

    public static void resetSerialNumber(){
        Static.systemSerialNumberService.resetSerialNumber();
    }

}
