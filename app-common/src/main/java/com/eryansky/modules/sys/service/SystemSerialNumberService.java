/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.DateUtils;
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
import com.eryansky.utils.AppDateUtils;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;

/**
 * @author Eryan
 * @date 2016-07-14
 */
@Service
public class SystemSerialNumberService extends CrudService<SystemSerialNumberDao, SystemSerialNumber> {


    @Override
    public Page<SystemSerialNumber> findPage(Page<SystemSerialNumber> page, SystemSerialNumber entity) {
        entity.setEntityPage(page);
        page.setResult(dao.findList(entity));
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
     * @param moduleCode
     * @return
     */
    public SystemSerialNumber getByCode(String moduleCode) {
        return getByCode(SystemSerialNumber.DEFAULT_ID, moduleCode);
    }


    /**
     * 根据模块编码查找
     *
     * @param app        APP标识
     * @param moduleCode
     * @return
     */
    public SystemSerialNumber getByCode(String app, String moduleCode) {
        SystemSerialNumber entity = new SystemSerialNumber();
        entity.setApp(StringUtils.isNotBlank(app) ? app : VersionLog.DEFAULT_ID);
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
     * @param moduleCode 模块code
     * @return
     */
    public List<String> generatePrepareSerialNumbers(String app, String moduleCode) {
        return generatePrepareSerialNumbers(app, moduleCode, null, null);
    }

    /**
     * 根据模块code生成预数量的序列号存放到Map中
     *
     * @param moduleCode 模块code
     * @return
     */
    @Transactional(value = DBConfigurer.TX_MANAGER_NAME,propagation = Propagation.REQUIRES_NEW)//开启新事务 防止事务嵌套传递
    public List<String> generatePrepareSerialNumbers(String app, String moduleCode, String customCategory, Map<String, String> params) {
        String _moduleCode = null == customCategory ? moduleCode : moduleCode + "_" + customCategory;
        String maxSerialKey = null == customCategory ? SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL : SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL + "_" + customCategory;
        SystemSerialNumber entity = getByCode(StringUtils.isNotBlank(app) ? app : VersionLog.DEFAULT_ID, moduleCode);
        //预生成数量
        int prepare = StringUtils.isNotBlank(entity.getPreMaxNum()) ? Integer.parseInt(entity.getPreMaxNum()) : 1;
        //数据库存储的当前最大序列号
        if (null == entity.getMaxSerial()) {
            entity.setMaxSerial(new MaxSerial());
        }
        MaxSerialItem maxSerialItem = null;
        if (Collections3.isNotEmpty(entity.getMaxSerial().getItems())) {
            maxSerialItem = entity.getMaxSerial().getItems().stream().filter(v -> v.getKey().equals(maxSerialKey)).findFirst().orElse(null);
        }
        if (null == maxSerialItem) {
            maxSerialItem = new MaxSerialItem().setKey(maxSerialKey);
        }
        long maxSerialInt = maxSerialItem.getValue();
        //临时List变量
        List<String> resultList = new ArrayList<>(prepare);
        SNGenerateApp snGenerateApp = new SNGenerateApp();
        Map<String,Object> map = Maps.newHashMap(); //设定参数
        map.put(GeneratorConstants.PARAM_MODULE_CODE, _moduleCode);
        if (null != params) {
            map.putAll(params);
        }
        map.put(GeneratorConstants.PARAM_CUSTOM_CATEGORY, customCategory);
        for (int i = 0; i < prepare; i++) {
            map.put(GeneratorConstants.PARAM_MAX_SERIAL, maxSerialInt + "");
            String formatSerialNum = snGenerateApp.generateSN(entity.getConfigTemplate(), map);
            maxSerialInt++;
            resultList.add(formatSerialNum);
        }
        //更新数据
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
     * 年度重置序列号
     */
    public void resetSerialNumber() {
        List<SystemSerialNumber> list = this.findAll();
        list.forEach(v -> {
            resetSerialNumber(v.getId());
        });
    }

    /**
     * 重置序列号
     * @param entity
     * @return
     */
    public int updateSerialNumber(SystemSerialNumber entity){
        return dao.updateSerialNumber(entity);
    }

    /**
     * 年度重置序列号
     */
    public void resetSerialNumber(String id) {
        SystemSerialNumber systemSerialNumber = this.get(id);
        Date now = null;
        try {
            now = DateUtils.parseDate(DateUtils.getDate(), DateUtils.DATE_FORMAT);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        boolean flag = false;
        if (ResetType.Day.getValue().equals(systemSerialNumber.getResetType())) {
            flag = true;
        } else if (ResetType.Month.getValue().equals(systemSerialNumber.getResetType())) {
            flag = AppDateUtils.getCurrentMonthStartTime().equals(now);
        }
//            else if(ResetType.Quarter.getValue().equals(systemSerialNumber.getResetType())){
//                flag = AppDateUtils.getCurrentQuarterStartTime().equals(now);
//            }
        else if (ResetType.Year.getValue().equals(systemSerialNumber.getResetType())) {
            flag = AppDateUtils.getCurrentYearStartTime().equals(now);
        }
        if (flag) {
            MaxSerial maxSerial = systemSerialNumber.getMaxSerial();
            //子项序列号
            List<String> cs = Lists.newArrayList();
            if (null != maxSerial) {
                maxSerial.getItems().forEach(v -> {
                    String key = StringUtils.substringAfter(v.getKey(), SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL + "_");
                    if (StringUtils.isNotBlank(key)) {
                        cs.add(systemSerialNumber.getModuleCode() + "_" + key);
                    }
                });
            }

            logger.info("重置序列号，{}：{}", systemSerialNumber.getApp(), systemSerialNumber.getModuleCode());
            systemSerialNumber.setMaxSerial(new MaxSerial());
            systemSerialNumber.setVersion(0);
            systemSerialNumber.setUpdateTime(Calendar.getInstance().getTime());
            this.updateSerialNumber(systemSerialNumber);
            //清空缓存
            clearCacheQueueByModuleCode(systemSerialNumber.getApp(), systemSerialNumber.getModuleCode());
            cs.forEach(v -> {
                clearCacheQueueByModuleCode(systemSerialNumber.getApp(), v);
            });
        }
    }

    /**
     * 清空队列缓存(指定key)
     */
    public void clearCacheQueueByModuleCode(String app, String moduleCode) {
        //单项
        String queueRegion = SystemSerialNumberUtils.getQueueRegion(app, moduleCode);
        CacheUtils.getCacheChannel().queueClear(queueRegion);
        //存在子项
//        SystemSerialNumber systemSerialNumber = getByCode(app, moduleCode);
//        if (null != systemSerialNumber) {
//            MaxSerial maxSerial = systemSerialNumber.getMaxSerial();
//            if (null != maxSerial) {
//                maxSerial.getItems().forEach(v -> {
//                    String key = StringUtils.substringAfter(v.getKey(), SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL + "_");
//                    if (StringUtils.isNotBlank(key)) {
//                        String _queueRegion = SystemSerialNumberUtils.getQueueRegion(app, moduleCode + "_" + key);
//                        CacheUtils.getCacheChannel().queueClear(_queueRegion);
//                    }
//
//                });
//            }
//
//        }
    }

    /**
     * 清空队列缓存
     */
    public void clearAllCacheQueue() {
        List<SystemSerialNumber> numberList = this.findAll();
        for (SystemSerialNumber systemSerialNumber : numberList) {
            clearCacheQueueByModuleCode(systemSerialNumber.getApp(), systemSerialNumber.getModuleCode());
            MaxSerial maxSerial = systemSerialNumber.getMaxSerial();
            //子项序列号
            List<String> cs = Lists.newArrayList();
            if (null != maxSerial) {
                maxSerial.getItems().forEach(v -> {
                    String key = StringUtils.substringAfter(v.getKey(), SystemSerialNumber.DEFAULT_KEY_MAX_SERIAL + "_");
                    if (StringUtils.isNotBlank(key)) {
                        cs.add(systemSerialNumber.getModuleCode() + "_" + key);
                    }
                });
            }
            cs.forEach(v -> {
                clearCacheQueueByModuleCode(systemSerialNumber.getApp(), v);
            });

        }
    }

}
