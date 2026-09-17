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
import java.util.List;

/**
 * 用户登录设备 service
 * @author eryan
 * @date 2026-09-16
 */
@Service
public class UserDeviceService extends PCrudService<UserDeviceDao, UserDevice,String> {

    @Resource
    private IpService ipService;

    public List<UserDevice> findByUserId(String userId,String deviceId){
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS,DataEntity.STATUS_NORMAL);
        parameter.put("userId",userId);
        parameter.put("deviceId",deviceId);
        return dao.findByUserId(parameter);
    }


    /**
     *
     * @param userId
     * @param userName
     * @param request
     * @return
     */
    public UserDevice saveOrUpdate(String userId, String userName, HttpServletRequest request){
        String ua = UserAgentUtils.getHTTPUserAgent(request);
        String appVersion_s = WebUtils.getParameter(request, "appVersion");
        String deviceCode_s = WebUtils.getParameter(request, "deviceCode");
        String platform_s = WebUtils.getParameter(request, "platform");
        String deviceId = ua;
        if(StringUtils.isNotBlank(deviceCode_s)){
            deviceId = deviceCode_s;
        }else{
            deviceId = Encrypt.md5(ua);
        }
        String deviceType = null;

        boolean likeIOS = AppUtils.likeIOS(ua);
        boolean likeAndroid = AppUtils.likeAndroid(ua);
        if (likeIOS) {
            deviceType = DeviceType.iPhone.getDescription();
        } else if (likeAndroid) {
            deviceType = DeviceType.Android.getDescription();
        } else {
            deviceType = DeviceType.PC.getDescription();
        }
        String deviceName = ua;
        if(StringUtils.isNotBlank(platform_s)){
            deviceType = platform_s;
            deviceName = platform_s;
        }
        String ip = IpUtils.getIpAddr0(request);
        List<UserDevice> userDevices = findByUserId(userId,deviceId);
        UserDevice entity = userDevices.isEmpty() ? null:userDevices.get(0);
        if(entity == null){
            entity = new UserDevice();
            entity.setFirstLoginTime(Calendar.getInstance().getTime());
        }
        entity.setUserId(userId);
        entity.setUserName(StringUtils.defaultString(userName,userId));
        List<String> ips = Collections3.aggregate(JsonMapper.getInstance().toJavaObjectList(entity.getIps(),String.class), Lists.newArrayList(ip));
        entity.setIps(JsonMapper.getInstance().toJson(ips));
        GeoIP geoIP = ipService.getLocationByIp(ip);
        entity.setLocation(geoIP.toFormatLocation());
        entity.setDeviceId(deviceId);
        entity.setDeviceType(deviceType);
        entity.setDeviceNamme(deviceName);
        entity.setLastLoginTime(Calendar.getInstance().getTime());
        entity.setLoginCount((entity.getLoginCount() == null ? 0: entity.getLoginCount()) + 1);
        if(entity.getLoginCount() > 10){
            entity.setIsCommon(YesOrNo.YES.getValue());
        }else{
            entity.setIsCommon(YesOrNo.NO.getValue());
        }
        this.save(entity);
        return entity;
    }

}
