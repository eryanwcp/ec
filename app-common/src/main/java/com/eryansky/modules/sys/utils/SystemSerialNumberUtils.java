/*
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统序列号生成工具类
 *
 * @author Eryan
 * @date 2016-05-12
 */
public class SystemSerialNumberUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemSerialNumberUtils.class);

    /**
     * 用于代替 intern() 的线程安全本地锁容器，避免 JVM 锁范围过大或死锁风险
     */
    private static final ConcurrentHashMap<String, Object> LOCAL_LOCKS = new ConcurrentHashMap<>();

    private SystemSerialNumberUtils() {}

    /**
     * 静态内部类，延迟加载，线程安全的单例模式
     */
    public static final class Static {
        private static final SystemSerialNumberService systemSerialNumberService = SpringContextHolder.getBean(SystemSerialNumberService.class);
        private static final CacheChannel cacheChannel = CacheUtils.getCacheChannel();

        private Static() {}
    }

    public static String getQueueRegion(String app, String moduleCode) {
        return SystemSerialNumber.QUEUE_KEY + "_" + app + "_" + moduleCode;
    }

    public static String getLockRegion(String app, String moduleCode) {
        return SystemSerialNumber.LOCK_KEY + "_" + app + "_" + moduleCode;
    }

    public static String getLockItemRegion(String app, String moduleCode) {
        return SystemSerialNumber.LOCK_ITEM_KEY + "_" + app + "_" + moduleCode;
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
        return getByModuleCode(null, moduleCode);
    }

    public static SystemSerialNumber getByModuleCode(String app, String moduleCode) {
        if (StringUtils.isNotBlank(moduleCode)) {
            return Static.systemSerialNumberService.getByCode(app, moduleCode);
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
        return getMaxSerialByModuleCode(null, moduleCode, null);
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

    public static Long getMaxSerialByModuleCode(String app, String moduleCode, String customCategory) {
        SystemSerialNumber systemSerialNumber = getByModuleCode(app, moduleCode);
        String maxSerialKey = (customCategory == null)
                ? SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL
                : SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL + "_" + customCategory;

        if (systemSerialNumber != null && systemSerialNumber.getMaxSerial() != null
                && Collections3.isNotEmpty(systemSerialNumber.getMaxSerial().getItems())) {

            MaxSerialItem item = systemSerialNumber.getMaxSerial().getItems().stream()
                    .filter(v -> v.getKey().equals(maxSerialKey))
                    .findFirst()
                    .orElse(new MaxSerialItem());
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
    public static String generateSerialNumberByModuleCode(String moduleCode) {
        return generateSerialNumberByModuleCode(null, moduleCode, null, null, null, null);
    }

    /**
     * 根据模块code生成序列号
     *
     * @param moduleCode 模块code
     * @return 序列号
     */
    public static String generateSerialNumberByModuleCode(String moduleCode, String customCategory) {
        return generateSerialNumberByModuleCode(null, moduleCode, null, null, customCategory, null);
    }

    /**
     * 根据模块code生成序列号
     *
     * @param moduleCode 模块code
     * @param customCategory 自定义分类编码
     * @param params 自定义参数
     * @return 序列号
     */
    public static String generateSerialNumberByModuleCode(String moduleCode, String customCategory, Map<String, String> params) {
        return generateSerialNumberByModuleCode(null, moduleCode, null, null, customCategory, params);
    }

    /**
     * 根据模块code生成序列号（核心方法）
     *
     * @param app                APP标识
     * @param moduleCode         模块code
     * @param timeoutInSecond    获取锁超时时间 单位：秒
     * @param keyExpireSeconds   锁超时时间（使用redis有效） 单位：秒
     * @param customCategory     自定义分类编码
     * @param params             自定义分类参数
     * @return 序列号
     */
    public static String generateSerialNumberByModuleCode(String app, String moduleCode, Integer timeoutInSecond, Long keyExpireSeconds, String customCategory, Map<String, String> params) {
        String _app = (app == null) ? SystemSerialNumber.DEFAULT_ID : app;
        String _moduleCode = (customCategory == null) ? moduleCode : moduleCode + "_" + customCategory;

        String queueRegion = getQueueRegion(_app, _moduleCode);
        // 使用锁粒度与 customCategory 一致的 key，提升并发性能
        String lockKey = getLockRegion(_app, _moduleCode);

        // 获取本地同步锁对象
        Object lockObj = LOCAL_LOCKS.computeIfAbsent(lockKey, k -> new Object());

        synchronized (lockObj) {
            try {
                // 1. 先尝试从缓存队列直接弹出可用序列号
                String value = Static.cacheChannel.queuePop(queueRegion);
                if (value != null) {
                    return value;
                }

                // 2. 队列为空，尝试获取分布式锁批量补充序列号
                String lockRegion = getLockItemRegion(_app, _moduleCode);
                String finalApp = _app;
                int lockTimeout = (timeoutInSecond != null) ? timeoutInSecond : 60;
                long expireSeconds = (keyExpireSeconds != null) ? keyExpireSeconds : 180;

                boolean flag = Static.cacheChannel.lock(lockRegion, lockTimeout, expireSeconds, new DefaultLockCallback<Boolean>(false, false) {
                    @Override
                    public Boolean handleObtainLock() {
                        List<String> list = Static.systemSerialNumberService.generatePrepareSerialNumbers(finalApp, moduleCode, customCategory, params);
                        if (Collections3.isNotEmpty(list)) {
                            for (String serial : list) {
                                Static.cacheChannel.queuePush(queueRegion, serial);
                            }
                        }
                        return true;
                    }

                    @Override
                    public Boolean handleException(LockInsideExecutedException e) {
                        logger.error("分布式锁执行异常: {}", e.getMessage(), e);
                        return super.handleException(e);
                    }
                });

                // 3. 分布式锁获取或执行失败时的兜底逻辑
                if (!flag) {
                    value = Static.cacheChannel.queuePop(queueRegion);
                    if (value != null) {
                        return value;
                    }
                    logger.error("生成序列号失败，无法获取锁且队列为空，queueRegion: {}", queueRegion);
                    return null;
                }

                // 4. 再次获取补充后的序列号
                return Static.cacheChannel.queuePop(queueRegion);
            } finally {
                // 可选：防内存泄漏清理本地锁（无并发时清空）
                LOCAL_LOCKS.remove(lockKey, lockObj);
            }
        }
    }

    public static void resetSerialNumber() {
        Static.systemSerialNumberService.resetSerialNumber();
    }
}