/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.modules.sys._enum.VersionLogType;
import com.eryansky.modules.sys.mapper.VersionLog;
import com.eryansky.modules.sys.service.VersionLogService;
import com.eryansky.utils.AppUtils;
import eu.bitwalker.useragentutils.OperatingSystem;

import javax.servlet.http.HttpServletRequest;

import static com.eryansky.utils.AppUtils.likeIOS;

/**
 * @author Eryan
 * @date 2019-11-21
 */
public class VersionLogUtils {

    private VersionLogUtils(){}

    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    public static final class Static {
        private static final VersionLogService versionLogService = SpringContextHolder.getBean(VersionLogService.class);
        private Static(){}
    }
    /**
     * 获取当前版本的更新说明
     *
     * @return
     */
    public static VersionLog getLatestVersionLog() {
        return getLatestVersionLog(null);
    }
    /**
     * 获取当前版本的更新说明
     *
     * @return
     */
    public static VersionLog getLatestVersionLog(String app) {
        String userAgent = UserAgentUtils.getHTTPUserAgent(SpringMVCHolder.getRequest());
        if (AppUtils.likeAndroid(userAgent)) {
            return Static.versionLogService.getLatestVersionLog(app,VersionLogType.Android.getValue());
        } else if (likeIOS(userAgent)) {
            return Static.versionLogService.getLatestVersionLog(app,VersionLogType.iPhone.getValue());
        }
        return Static.versionLogService.getLatestVersionLog(app,VersionLogType.Server.getValue());
    }

    /**
     * 获取当前版本的更新说明
     *
     * @param versionLogType {@link com.eryansky.modules.sys._enum.VersionLogType}
     * @return
     */
    public static VersionLog getLatestVersionLog(String app,String versionLogType) {
        return Static.versionLogService.getLatestVersionLog(app,versionLogType);
    }


    /**
     * 获取当前版本的类型
     *
     * @param request
     * @return
     */
    public static VersionLogType getLatestVersionLogType(HttpServletRequest request) {
        OperatingSystem operatingSystem = UserAgentUtils.getUserAgent(request).getOperatingSystem();
        if(OperatingSystem.ANDROID.equals(operatingSystem)){
            return VersionLogType.Android;
        }else if(OperatingSystem.IOS.equals(operatingSystem)){
            return VersionLogType.iPhoneAPP;
        }
        return VersionLogType.Server;
    }
}
