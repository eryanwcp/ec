/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.ip.IpAPI;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys._enum.DeviceType;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.core.ip.dto.GeoIP;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import com.google.common.collect.Lists;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.eryansky.modules.sys.mapper.UserDevice;
import com.eryansky.modules.sys.dao.UserDeviceDao;
import com.eryansky.core.orm.mybatis.service.PCrudService;

import java.util.*;

/**
 * 用户登录设备 service
 * @author eryan
 * @date 2026-09-16
 */
@Service
public class UserDeviceService extends PCrudService<UserDeviceDao, UserDevice, String> {

    @Resource
    private IpAPI ipAPI;

    public List<UserDevice> findByUserId(String appId,String userId, String deviceId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("appId", appId);
        parameter.put("userId", userId);
        parameter.put("deviceId", deviceId);
        return dao.findByUserId(parameter);
    }


    public Page<UserDevice> findPageByUserId(String appId,Page<UserDevice> page, String userId, String query) {
        Parameter parameter = Parameter.newPageParameter(page);
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("appId", appId);
        parameter.put("userId", userId);
        parameter.put("query", query);
        return page.setResult(dao.findByUserId(parameter));
    }

    public Page<UserDevice> findPage(Page<UserDevice> page, UserDevice entity) {
        entity.setEntityPage(page);
        return page.setResult(dao.findList(entity));
    }

    public Page<UserDevice> findPage(Page<UserDevice> page,
                                     UserDevice entity,
                                     Date beginLastLoginTime,
                                     Date endLastLoginTime) {
        Parameter parameter = Parameter.newPageParameter(page);
        parameter.put(DataEntity.FIELD_STATUS,DataEntity.STATUS_NORMAL);
        parameter.put("appId",entity.getAppId());
        parameter.put("query",entity.getQuery());
        parameter.put("deviceType",entity.getDeviceType());
        parameter.put("beginLastLoginTime",beginLastLoginTime != null ? DateUtils.getDateStart(beginLastLoginTime):null);
        parameter.put("endLastLoginTime",endLastLoginTime != null ? DateUtils.getDateEnd(endLastLoginTime):null);
        return page.setResult(dao.findQueryList(parameter));
    }


    /**
     * 检查设备是否登录过
     * @param userId
     * @param deviceCode
     * @param ip
     * @param userAgent
     * @return
     */
    public UserDevice checkExist(String userId,String deviceCode,String ip,String userAgent) {
        return checkExist(null,userId,deviceCode,ip,userAgent);
    }


    /**
     * 检查设备是否登录过
     * @param userId
     * @param deviceCode
     * @param ip
     * @param userAgent
     * @return
     */
    public UserDevice checkExist(String appId,String userId,String deviceCode,String ip,String userAgent) {
        String deviceId = AppUtils.resolveDeviceId(deviceCode, userAgent, ip);

        List<UserDevice> userDevices = findByUserId(appId,userId, deviceId);
        return Collections3.isNotEmpty(userDevices) ? userDevices.get(0) : null;
    }

    /**
     * 保存或更新用户设备登录记录
     *
     * @param sessionInfo  登录会话信息
     * @return UserDevice
     */
    public UserDevice saveOrUpdate(SessionInfo sessionInfo) {
        return saveOrUpdate(null,sessionInfo);
    }
    /**
     * 保存或更新用户设备登录记录
     *
     * @param sessionInfo  登录会话信息
     * @param appId  应用ID
     * @return UserDevice
     */
    public UserDevice saveOrUpdate(String appId,SessionInfo sessionInfo) {
        String ua = sessionInfo.getUserAgent();
        String deviceCode_s = sessionInfo.getDeviceCode();
        String ip = sessionInfo.getIp();

        // 1. 确定设备唯一标识 deviceId
        String deviceId = AppUtils.resolveDeviceId(deviceCode_s, ua,ip);

        // 2. 确定设备类型 deviceType 与设备名称 deviceName
        String deviceType = sessionInfo.getSystemDeviceType();
        String deviceName = StringUtils.defaultString(sessionInfo.getDeviceName(),ua);


        // 3. 获取客户端 IP 及位置信息
        GeoIP geoIP = ipAPI.getLocationByIp(ip);
        String location = Optional.ofNullable(geoIP)
                .map(GeoIP::toFormatLocation)
                .orElse(null);

        // 4. 查询是否存在历史设备记录
        List<UserDevice> userDevices = findByUserId(appId,sessionInfo.getUserId(), deviceId);
        UserDevice entity = Collections3.isNotEmpty(userDevices) ? userDevices.get(0) : null;

        if (entity == null) {
            entity = new UserDevice();
            entity.setFirstLoginTime(Calendar.getInstance().getTime());
        }

        // 5. 更新 IP 列表 JSON
        List<String> existIps = Collections.emptyList();
        if (StringUtils.isNotBlank(entity.getIps())) {
            existIps = JsonMapper.getInstance().toJavaObjectList(entity.getIps(), String.class);
        }
        List<String> updatedIps = Collections3.aggregate(existIps, Lists.newArrayList(ip));

        // 6. 填充实体字段
        entity.setAppId(appId);
        entity.setUserId(sessionInfo.getUserId());
        entity.setUserName(StringUtils.defaultString(sessionInfo.getName(), sessionInfo.getUserId()));
        entity.setUserType(sessionInfo.getUserType());
        entity.setIps(JsonMapper.getInstance().toJson(updatedIps));
        if(location != null){
            entity.setLocation(location);
        }
        entity.setDeviceId(deviceId);
        entity.setDeviceType(GenericEnumUtils.getDescriptionByValue(DeviceType.class,deviceType,deviceType));
        entity.setDeviceName(deviceName); // 修正拼写错误 setDeviceNamme -> setDeviceName
        entity.setLastLoginTime(Calendar.getInstance().getTime());

        // 7. 计算登录次数与常用设备标识
        int loginCount = Optional.ofNullable(entity.getLoginCount()).orElse(0) + 1;
        entity.setLoginCount(loginCount);
        entity.setIsCommon(loginCount > 10 ? YesOrNo.YES.getValue() : YesOrNo.NO.getValue());

        this.save(entity);
        return entity;
    }
}