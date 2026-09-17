/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.vo.GeoIP;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.eryansky.modules.sys.mapper.UserDevice;
import com.eryansky.modules.sys.dao.UserDeviceDao;
import com.eryansky.core.orm.mybatis.service.PCrudService;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户登录设备 service
 * @author eryan
 * @date 2026-09-16
 */
@Service
public class UserDeviceService extends PCrudService<UserDeviceDao, UserDevice, String> {

    @Resource
    private IpService ipService;

    public List<UserDevice> findByUserId(String userId, String deviceId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("userId", userId);
        parameter.put("deviceId", deviceId);
        return dao.findByUserId(parameter);
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
        String deviceId = resolveDeviceId(deviceCode, userAgent, ip);

        List<UserDevice> userDevices = findByUserId(userId, deviceId);
        return Collections3.isNotEmpty(userDevices) ? userDevices.get(0) : null;
    }
    /**
     * 获取或生成唯一设备标识 deviceId
     */
    public String resolveDeviceId(String deviceCode, String ua,String ip) {
        // 1. 优先使用客户端传入的明确设备编码（App 端或前端生成的 UUID）
        if (StringUtils.isNotBlank(deviceCode)) {
            return deviceCode;
        }

        // 2. 极弱兜底：结合 IP + UA 组合生成防重碰撞的设备指纹（仅作为无Cookie场景下的备用标识）
        String rawFingerprint = String.format("%s|%s", StringUtils.defaultString(ip), StringUtils.defaultString(ua));

        return Encrypt.md5(rawFingerprint);
    }

    /**
     * 保存或更新用户设备登录记录
     *
     * @param sessionInfo  登录会话信息
     * @return UserDevice
     */
    public UserDevice saveOrUpdate(SessionInfo sessionInfo) {
        String ua = sessionInfo.getUserAgent();
        String deviceCode_s = sessionInfo.getDeviceCode();
        String ip = sessionInfo.getIp();

        // 1. 确定设备唯一标识 deviceId
        String deviceId = resolveDeviceId(deviceCode_s, ua,ip);

        // 2. 确定设备类型 deviceType 与设备名称 deviceName
        String deviceType = sessionInfo.getSystemDeviceType();
        String deviceName = StringUtils.defaultString(sessionInfo.getDeviceName(),ua);


        // 3. 获取客户端 IP 及位置信息
        GeoIP geoIP = ipService.getLocationByIp(ip);
        String location = Optional.ofNullable(geoIP)
                .map(GeoIP::toFormatLocation)
                .orElse(null);

        // 4. 查询是否存在历史设备记录
        List<UserDevice> userDevices = findByUserId(sessionInfo.getUserId(), deviceId);
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
        entity.setUserId(sessionInfo.getUserId());
        entity.setUserName(StringUtils.defaultString(sessionInfo.getName(), sessionInfo.getUserId()));
        entity.setUserType(sessionInfo.getUserType());
        entity.setIps(JsonMapper.getInstance().toJson(updatedIps));
        entity.setLocation(location);
        entity.setDeviceId(deviceId);
        entity.setDeviceType(deviceType);
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