package com.eryansky.modules.sys.task;

import com.eryansky.common.spring.SpringContextHolder;
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
    private IpService ipService;

    /**
     * 异常设备登录提醒（登录尝试）
     */
    @Async
    public void checkRiskUserDevice(String loginName, String deviceCode, String ip, String userAgent) {
        try {
            User user = UserUtils.getUserByLoginNameOrMobile(loginName);
            if (user == null) {
                return;
            }
            UserDevice entity = userDeviceService.checkExist(user.getId(), deviceCode, ip, userAgent);
            if (entity == null) {
                String msg = buildAlertMessage("异常设备登录尝试！", loginName, ip, userAgent);
                sendMessage(msg);
            }
        } catch (Exception e) {
            logger.error("检查异常设备登录尝试失败, loginName: {}, ip: {}", loginName, ip, e);
        }
    }

    /**
     * 异常设备登录提醒（登录成功）
     */
    @Async
    public void checkRiskUserDevice(SessionInfo sessionInfo) {
        if (sessionInfo == null) {
            return;
        }
        try {
            UserDevice entity = userDeviceService.checkExist(sessionInfo.getUserId(), sessionInfo.getDeviceCode(), sessionInfo.getIp(), sessionInfo.getUserAgent());
            if (entity == null) {
                String msg = buildAlertMessage("异常设备登录成功！", sessionInfo.getLoginName(), sessionInfo.getIp(), sessionInfo.getUserAgent());
                sendMessage(msg);
            }
        } catch (Exception e) {
            logger.error("检查异常设备登录成功失败, userId: {}, ip: {}", sessionInfo.getUserId(), sessionInfo.getIp(), e);
        }
    }

    /**
     * 构建报警消息文本
     */
    private String buildAlertMessage(String eventType, String loginName, String ip, String userAgent) {
        GeoIP geoIP = ipService.getLocationByIp(ip);
        String locationStr = Optional.ofNullable(geoIP)
                .map(GeoIP::toFormatLocation)
                .map(loc -> "[" + loc + "]")
                .orElse("");

        return String.format("%s安全提醒：%s时间：%s，用户：%s，IP：%s%s,设备：%s",
                SpringContextHolder.getApplicationContext().getId(),
                eventType,
                DateUtils.getDateTime(),
                loginName,
                ip,
                locationStr,
                userAgent);
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
        if (sessionInfo == null) {
            return;
        }
        try {
            userDeviceService.saveOrUpdate(sessionInfo);
        } catch (Exception e) {
            logger.error("保存或更新用户设备信息失败, userId: {}", sessionInfo.getUserId(), e);
        }
    }
}