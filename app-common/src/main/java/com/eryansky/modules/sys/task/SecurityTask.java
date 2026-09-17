/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.task;

import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.mapper.UserDevice;
import com.eryansky.modules.sys.service.IpService;
import com.eryansky.modules.sys.service.UserDeviceService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.modules.sys.vo.GeoIP;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 安全相关送异步任务
 *
 * @author Eryan
 * @date 2026-09-17
 */
@Component
public class SecurityTask {

    private static final Logger logger = LoggerFactory.getLogger(SecurityTask.class);

    @Resource
    private UserDeviceService userDeviceService;
    @Resource
    private IpService ipService;

    /**
     * 异常设备登录提醒
     */
    @Async
    public void checkRiskUserDevice(String loginName, String deviceCode, String ip, String userAgent) {
        User user = UserUtils.getUserByLoginNameOrMobile(loginName);
        if (user == null) {
            return;
        }
        UserDevice entity = userDeviceService.checkExist(user.getId(), deviceCode, ip, userAgent);
        if (entity == null) {
            GeoIP geoIP = ipService.getLocationByIp(ip);
            String location = Optional.ofNullable(geoIP)
                    .map(GeoIP::toFormatLocation)
                    .orElse(null);
            StringBuffer msg = new StringBuffer();
            msg.append("安全预警：异常设备登录尝试！时间：" + DateUtils.getCurrentDateTime() + "，用户：" + loginName + "，IP："  + (location != null ? ("["+geoIP+"]"):"") + ",设备：" + userAgent);
            sendMessage(msg.toString());
        }
    }

    /**
     * 异常设备登录提醒
     */
    @Async
    public void checkRiskUserDevice(SessionInfo sessionInfo) {
        UserDevice entity = userDeviceService.checkExist(sessionInfo.getUserId(), sessionInfo.getDeviceCode(), sessionInfo.getIp(), sessionInfo.getUserAgent());
        if (entity == null) {
            GeoIP geoIP = ipService.getLocationByIp(sessionInfo.getIp());
            String location = Optional.ofNullable(geoIP)
                    .map(GeoIP::toFormatLocation)
                    .orElse(null);
            StringBuffer msg = new StringBuffer();
            msg.append("安全预警：异常设备登录成功！时间：" + DateUtils.getCurrentDateTime() + "，用户：" + sessionInfo.getLoginName() + "，IP：" + sessionInfo.getIp() + (location != null ? ("["+geoIP+"]"):"") + ",设备：" + sessionInfo.getUserAgent());
            sendMessage(msg.toString());
        }
    }

    /**
     * 异常设备登录提醒
     */
    @Async
    public void sendMessage(String msg) {
        List<String> systemOpsWarnUserIds = UserUtils.findUsersByLoginNames(AppConstants.getSystemOpsWarnLoginNameList())
                .stream()
                .map(BaseEntity::getId)
                .collect(Collectors.toList());

        if (Collections3.isEmpty(systemOpsWarnUserIds)) {
            systemOpsWarnUserIds = Lists.newArrayList(User.SUPERUSER_ID);
        }
        MessageUtils.sendToUserMessage(systemOpsWarnUserIds, msg);
    }

    /**
     * 记录用户登录设备信息
     * @param sessionInfo
     */
    @Async
    public void saveOrUpdateUserDevice(SessionInfo sessionInfo) {
        userDeviceService.saveOrUpdate(sessionInfo);
    }
}
