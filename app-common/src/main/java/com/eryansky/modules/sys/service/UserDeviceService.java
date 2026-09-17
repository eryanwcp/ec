/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.security._enum.DeviceType;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.vo.GeoIP;
import com.eryansky.utils.AppUtils;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
     * 保存或更新用户设备登录记录
     *
     * @param userId   用户ID
     * @param userName 用户名
     * @param request  请求对象
     * @return UserDevice
     */
    public UserDevice saveOrUpdate(String userId, String userName, HttpServletRequest request) {
        String ua = UserAgentUtils.getHTTPUserAgent(request);
        String deviceCode_s = WebUtils.getParameter(request, "deviceCode");
        String platform_s = WebUtils.getParameter(request, "platform");

        // 1. 确定设备唯一标识 deviceId
        String deviceId = StringUtils.isNotBlank(deviceCode_s) ? deviceCode_s : Encrypt.md5(ua);

        // 2. 确定设备类型 deviceType 与设备名称 deviceName
        String deviceType;
        String deviceName = ua;

        if (StringUtils.isNotBlank(platform_s)) {
            deviceType = platform_s;
            deviceName = platform_s;
        } else if (AppUtils.likeIOS(ua)) {
            deviceType = DeviceType.iPhone.getDescription();
        } else if (AppUtils.likeAndroid(ua)) {
            deviceType = DeviceType.Android.getDescription();
        } else {
            deviceType = DeviceType.PC.getDescription();
        }

        // 3. 获取客户端 IP 及位置信息
        String ip = IpUtils.getIpAddr0(request);
        GeoIP geoIP = ipService.getLocationByIp(ip);
        String location = Optional.ofNullable(geoIP)
                .map(GeoIP::toFormatLocation)
                .orElse(null);

        // 4. 查询是否存在历史设备记录
        List<UserDevice> userDevices = findByUserId(userId, deviceId);
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
        entity.setUserId(userId);
        entity.setUserName(StringUtils.defaultString(userName, userId));
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