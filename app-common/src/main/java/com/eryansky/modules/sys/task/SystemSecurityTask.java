package com.eryansky.modules.sys.task;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.mapper.UserDevice;
import com.eryansky.core.ip.IpAPI;
import com.eryansky.modules.sys.service.UserDeviceService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.core.ip.dto.GeoIP;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 安全相关异步任务
 *
 * @author Eryan
 * @date 2026-09-17
 */
@Component
public class SystemSecurityTask {

    private static final Logger logger = LoggerFactory.getLogger(SystemSecurityTask.class);

    @Resource
    private UserDeviceService userDeviceService;

    @Resource
    private IpAPI ipAPI;

    /**
     * 异常设备登录提醒（登录尝试）
     */
    @Async
    public void checkRiskUserDevice(String loginName, String deviceCode, String ip, String userAgent) {
        checkRiskUserDevice(null,loginName,deviceCode,ip,userAgent);
    }

    /**
     * 异常设备登录提醒（登录尝试）
     */
    @Async
    public void checkRiskUserDevice(String appId,String loginName, String deviceCode, String ip, String userAgent) {
        try {
            User user = UserUtils.getUserByLoginNameOrMobile(loginName);
            if (user == null) {
                return;
            }
            UserDevice entity = userDeviceService.checkExist(appId,user.getId(), deviceCode, ip, userAgent);
            if (entity == null) {
                String msg = buildAlertMessage("设备登录尝试！", loginName, ip, deviceCode,userAgent);
                sendMessage(msg);
            }
        } catch (Exception e) {
            logger.error("检查设备登录尝试失败, loginName: {}, ip: {}", loginName, ip, e);
        }
    }

    /**
     * 异常设备登录提醒（登录成功）
     */
    @Async
    public void checkRiskUserDevice(SessionInfo sessionInfo) {
        checkRiskUserDevice(null,sessionInfo);
    }

    /**
     * 异常设备登录提醒（登录成功）
     */
    @Async
    public void checkRiskUserDevice(String appId,SessionInfo sessionInfo) {
        if (sessionInfo == null) {
            return;
        }
        try {
            UserDevice entity = userDeviceService.checkExist(appId,sessionInfo.getUserId(), sessionInfo.getDeviceCode(), sessionInfo.getIp(), sessionInfo.getUserAgent());
            if (entity == null) {
                String msg = buildAlertMessage("设备首次登录成功！", sessionInfo.getLoginName(), sessionInfo.getIp(), sessionInfo.getDeviceCode(),sessionInfo.getUserAgent());
                sendMessage(msg);
            }
        } catch (Exception e) {
            logger.error("检查设备首次登录成功失败, userId: {}, ip: {}", sessionInfo.getUserId(), sessionInfo.getIp(), e);
        }
    }

    /**
     * 构建报警消息文本
     */
    private String buildAlertMessage(String eventType, String loginName, String ip, String deviceCode,String userAgent) {
        GeoIP geoIP = ipAPI.getLocationByIp(ip);
        String locationStr = Optional.ofNullable(geoIP)
                .map(GeoIP::toFormatLocation)
                .map(loc -> "[" + loc + "]")
                .orElse("");

        return String.format("安全提醒：%s时间：%s，用户：%s，IP：%s%s，设备：%s %s。%s",
                eventType,
                DateUtils.getDateTime(),
                loginName,
                ip,
                locationStr,
                deviceCode,
                userAgent,
                SpringContextHolder.getApplicationContext().getId());
    }

    /**
     * 发送系统预警消息
     */
    @Async
    public void sendMessage(String msg) {
        try {
            List<String> systemOpsWarnUserIds = UserUtils.findUsersByLoginNames(AppConstants.getSystemOpsWarnLoginNameList())
                    .stream()
                    .map(BaseEntity::getId)
                    .collect(Collectors.toList());

            if (Collections3.isEmpty(systemOpsWarnUserIds)) {
                systemOpsWarnUserIds = Lists.newArrayList(User.SUPERUSER_ID);
            }
            MessageUtils.sendToUserMessage(systemOpsWarnUserIds, msg);
        } catch (Exception e) {
            logger.error("发送安全预警消息失败", e);
        }
    }
    /**
     * 记录用户登录设备信息
     */
    @Async
    public void saveOrUpdateUserDevice(SessionInfo sessionInfo) {
        saveOrUpdateUserDevice(null,sessionInfo);
    }
    /**
     * 记录用户登录设备信息
     */
    @Async
    public void saveOrUpdateUserDevice(String appId,SessionInfo sessionInfo) {
        if (sessionInfo == null) {
            return;
        }
        try {
            userDeviceService.saveOrUpdate(appId,sessionInfo);
        } catch (Exception e) {
            logger.error("保存或更新用户设备信息失败, userId: {}", sessionInfo.getUserId(), e);
        }
    }
}