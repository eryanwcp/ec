/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.configure.DBConfigurer;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.modules.sys._enum.ResetType;
import com.eryansky.modules.sys.dao.SystemSerialNumberDao;
import com.eryansky.modules.sys.mapper.SystemSerialNumber;
import com.eryansky.modules.sys.mapper.VersionLog;
import com.eryansky.modules.sys.sn.GeneratorConstants;
import com.eryansky.modules.sys.sn.MaxSerial;
import com.eryansky.modules.sys.sn.MaxSerialItem;
import com.eryansky.modules.sys.sn.SNGenerateApp;
import com.eryansky.modules.sys.utils.SystemSerialNumberUtils;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 序列号生成与管理服务
 *
 * @author Eryan
 * @date 2016-07-14
 */
@Service
public class SystemSerialNumberService extends CrudService<SystemSerialNumberDao, SystemSerialNumber> {

    @Override
    public Page<SystemSerialNumber> findPage(Page<SystemSerialNumber> page, SystemSerialNumber entity) {
        entity.setEntityPage(page);
        page.autoResult(dao.findList(entity));
        return page;
    }

    /**
     * 乐观锁更新方式
     *
     * @param entity
     * @return 返回更新数 0：更新失败 1：更新成功
     */
    public void updateByVersion(SystemSerialNumber entity) {
        int result = dao.updateByVersion(entity);
        if (result == 0) {
            throw new ServiceException("乐观锁更新失败," + entity.toString());
        }
    }

    /**
     * 根据模块编码查找
     *
     * @param moduleCode 模块编码
     * @return 序列号实体
     */
    public SystemSerialNumber getByCode(String moduleCode) {
        return getByCode(SystemSerialNumber.DEFAULT_ID, moduleCode);
    }

    /**
     * 根据模块编码查找
     *
     * @param app        APP标识
     * @param moduleCode 模块编码
     * @return 序列号实体
     */
    public SystemSerialNumber getByCode(String app, String moduleCode) {
        SystemSerialNumber entity = new SystemSerialNumber();
        entity.setApp(StringUtils.defaultIfBlank(app, VersionLog.DEFAULT_ID));
        entity.setModuleCode(moduleCode);
        return dao.getByCode(entity);
    }

    /**
     * 查询所有序列号配置信息
     */
    public List<SystemSerialNumber> findAll() {
        SystemSerialNumber entity = new SystemSerialNumber();
        return dao.findAllList(entity);
    }

    /**
     * 根据模块code生成预数量的序列号存放到Map中
     *
     * @param app        APP标识
     * @param moduleCode 模块code
     * @return 序列号列表
     */
    public List<String> generatePrepareSerialNumbers(String app, String moduleCode) {
        return generatePrepareSerialNumbers(app, moduleCode, null, null);
    }

    /**
     * 根据模块code生成预数量的序列号存放到Map中
     *
     * @param app            APP标识
     * @param moduleCode     模块code
     * @param customCategory 自定义分类
     * @param params         附加参数
     * @return 序列号列表
     */
    @Transactional(value = DBConfigurer.TX_MANAGER_NAME, propagation = Propagation.REQUIRES_NEW) // 开启新事务 防止事务嵌套传递
    public List<String> generatePrepareSerialNumbers(String app, String moduleCode, String customCategory, Map<String, String> params) {
        String actualApp = StringUtils.defaultIfBlank(app, VersionLog.DEFAULT_ID);
        SystemSerialNumber entity = getByCode(actualApp, moduleCode);
        if (entity == null) {
            throw new ServiceException("未找到模块[" + moduleCode + "]的序列号配置");
        }

        String _moduleCode = customCategory == null ? moduleCode : moduleCode + "_" + customCategory;
        String maxSerialKey = customCategory == null
                ? SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL
                : SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL + "_" + customCategory;

        // 预生成数量
        int prepare = StringUtils.isNotBlank(entity.getPreMaxNum()) ? Integer.parseInt(entity.getPreMaxNum()) : 1;

        if (entity.getMaxSerial() == null) {
            entity.setMaxSerial(new MaxSerial());
        }

        // 查找或初始化 MaxSerialItem
        MaxSerialItem maxSerialItem = Optional.ofNullable(entity.getMaxSerial().getItems())
                .flatMap(items -> items.stream().filter(v -> maxSerialKey.equals(v.getKey())).findFirst())
                .orElseGet(() -> new MaxSerialItem().setKey(maxSerialKey));

        long maxSerialInt = maxSerialItem.getValue();
        List<String> resultList = new ArrayList<>(prepare);
        SNGenerateApp snGenerateApp = new SNGenerateApp();

        Map<String, Object> map = Maps.newHashMap();
        map.put(GeneratorConstants.PARAM_MODULE_CODE, _moduleCode);
        if (params != null) {
            map.putAll(params);
        }
        map.put(GeneratorConstants.PARAM_CUSTOM_CATEGORY, customCategory);

        for (int i = 0; i < prepare; i++) {
            map.put(GeneratorConstants.PARAM_MAX_SERIAL, String.valueOf(maxSerialInt));
            String formatSerialNum = snGenerateApp.generateSN(entity.getConfigTemplate(), map);
            maxSerialInt++;
            resultList.add(formatSerialNum);
        }

        // 更新最大序列号与版本信息
        maxSerialItem.setValue(maxSerialInt);
        entity.getMaxSerial().addIfNotExist(maxSerialItem.getKey(), maxSerialItem.getValue());
        entity.getMaxSerial().update(maxSerialItem.getKey(), maxSerialItem.getValue());
        entity.setUpdateTime(Calendar.getInstance().getTime());
        int result = dao.updateByVersion(entity);
        if (result == 0) {
            throw new ServiceException("乐观锁更新失败," + entity.toString());
        }
        return resultList;
    }

    /**
     * 批量重置所有序列号（年度）
     */
    public void resetSerialNumber() {
        List<SystemSerialNumber> list = this.findAll();
        list.forEach(v -> resetSerialNumber(v.getId()));
    }

    /**
     * 更新序列号实体
     *
     * @param entity 序列号实体
     * @return 影响行数
     */
    public int updateSerialNumber(SystemSerialNumber entity) {
        return dao.updateSerialNumber(entity);
    }

    /**
     * 根据ID按周期重置序列号
     *
     * @param id 序列号ID
     */
    public void resetSerialNumber(String id) {
        SystemSerialNumber systemSerialNumber = this.get(id);
        if (systemSerialNumber == null) {
            return;
        }

        boolean shouldReset = false;
        Calendar calendar = Calendar.getInstance();
        ResetType resetType = ResetType.getByValue(systemSerialNumber.getResetType());

        if (resetType != null) {
            switch (resetType) {
                case Day:
                    shouldReset = true;
                    break;
                case Month:
                    shouldReset = calendar.get(Calendar.DAY_OF_MONTH) == 1;
                    break;
                case Year:
                    shouldReset = calendar.get(Calendar.DAY_OF_YEAR) == 1;
                    break;
                default:
                    break;
            }
        }

        if (shouldReset) {
            List<String> childKeys = extractChildKeys(systemSerialNumber);

            logger.info("重置序列号，{}：{}", systemSerialNumber.getApp(), systemSerialNumber.getModuleCode());
            systemSerialNumber.setMaxSerial(new MaxSerial());
            systemSerialNumber.setVersion(0);
            systemSerialNumber.setUpdateTime(Calendar.getInstance().getTime());

            this.updateSerialNumber(systemSerialNumber);

            // 清空主项及子项缓存
            clearCacheQueueByModuleCode(systemSerialNumber.getApp(), systemSerialNumber.getModuleCode());
            childKeys.forEach(key -> clearCacheQueueByModuleCode(systemSerialNumber.getApp(), key));
        }
    }

    /**
     * 清空队列缓存(指定key)
     *
     * @param app        APP标识
     * @param moduleCode 模块编码
     */
    public void clearCacheQueueByModuleCode(String app, String moduleCode) {
        String queueRegion = SystemSerialNumberUtils.getQueueRegion(app, moduleCode);
        CacheUtils.getCacheChannel().queueClear(queueRegion);
    }

    /**
     * 清空所有队列缓存
     */
    public void clearAllCacheQueue() {
        List<SystemSerialNumber> numberList = this.findAll();
        for (SystemSerialNumber systemSerialNumber : numberList) {
            clearCacheQueueByModuleCode(systemSerialNumber.getApp(), systemSerialNumber.getModuleCode());
            extractChildKeys(systemSerialNumber).forEach(key ->
                    clearCacheQueueByModuleCode(systemSerialNumber.getApp(), key)
            );
        }
    }

    /**
     * 辅助工具方法：提取序列号配置中的子项序列号 Key 列表
     *
     * @param systemSerialNumber 序列号配置实体
     * @return 子项 Key 列表
     */
    private List<String> extractChildKeys(SystemSerialNumber systemSerialNumber) {
        List<String> childKeys = Lists.newArrayList();
        MaxSerial maxSerial = systemSerialNumber.getMaxSerial();
        if (maxSerial != null && Collections3.isNotEmpty(maxSerial.getItems())) {
            maxSerial.getItems().forEach(v -> {
                String key = StringUtils.substringAfter(v.getKey(), SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL + "_");
                if (StringUtils.isNotBlank(key)) {
                    childKeys.add(systemSerialNumber.getModuleCode() + "_" + key);
                }
            });
        }
        return childKeys;
    }
}