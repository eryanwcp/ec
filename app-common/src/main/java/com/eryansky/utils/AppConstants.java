/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.PrettyMemoryUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.SysConstants;
import com.eryansky.common.utils.io.FileUtils;
import com.eryansky.common.utils.io.PropertiesLoader;
import com.eryansky.modules.sys.service.ConfigService;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 系统使用的静态变量.
 *
 * @author Eryan
 * @date 2013-03-17 上午8:25:36
 */
public class AppConstants extends SysConstants {

    public static long SYS_INIT_TIME = System.currentTimeMillis();

    /**
     * 普通角色（基本角色）
     */
    public static final String ROLE_BASE = "role_base";
    /**
     * 系统管理员角色编号
     */
    public static final String ROLE_SYSTEM_MANAGER = "system_manager";
    /**
     * 电子邮件 管理员角色编号
     */
    public static final String ROLE_EMAIL_MANAGER = "email_manager";
    /**
     * 通知 管理员角色编号
     */
    public static final String ROLE_NOTICE_MANAGER = "notice_manager";
    /**
     * 云盘管理员
     */
    public static final String ROLE_DISK_MANAGER = "disk_manager";


    /**
     * 配置文件路径
     */
    public static final String CONFIG_FILE_PATH = "config.properties";

    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    private static final class Static {
        private static ConfigService configService = SpringContextHolder.getBean(ConfigService.class);
        private static PropertiesLoader config = getConfig();
        private static PropertiesLoader getConfig(){
            String activeProfile = getAppConfig().getActiveProfiles()[0];
            return new PropertiesLoader("config" + (null == activeProfile ? "" : "-" + activeProfile) + ".properties");
        }
    }

    /**
     * 获取jdbc交校验sql
     */
    public static String getJdbcValidationQuery() {
        return SysConstants.getAppConfig().getProperty("jdbc.validationQuery");
    }

    /**
     * 获取管理端根路径
     */
    public static String getAdminPath() {
        return SysConstants.getAppConfig().getProperty("adminPath");
    }

    /**
     * 获取前端根路径
     */
    public static String getFrontPath() {
        return SysConstants.getAppConfig().getProperty("frontPath");
    }

    /**
     * 获取移动端根路径
     */
    public static String getMobilePath() {
        return SysConstants.getAppConfig().getProperty("mobilePath");
    }

    /**
     * 获取URL后缀
     */
    public static String getUrlSuffix() {
        return SysConstants.getAppConfig().getProperty("urlSuffix");
    }

    /**
     * 配置文件(config.properties)
     */
    public static PropertiesLoader getConfig() {
        return Static.config;
    }

    /**
     * 获取配置
     */
    public static String getConfig(String key) {
        return getConfig().getProperty(key);
    }

    /**
     * 获取配置
     */
    public static String getConfig(String key, String defaultValue) {
        return getConfig().getProperty(key, defaultValue);
    }

    /**
     * 查找属性对应的属性值
     *
     * @param code 属性名称
     * @return
     */
    public static String getConfigValue(String code) {
        return getConfigValue(code, null);
    }

    /**
     * 查找属性对应的属性值
     *
     * @param code         属性名称
     * @param defaultValue 默认值
     * @return
     */
    public static String getConfigValue(String code, String defaultValue) {
        if (isdevMode()) {//调试模式 从本地配置文件读取
            return getConfig(code, defaultValue);
        }
        String configValue = Static.configService.getConfigValueByCode(code);
        if (StringUtils.isBlank(configValue)) {
            return getConfig(code, defaultValue);
        }
        return configValue == null ? defaultValue : configValue;
    }



    /**
     * 日志保留时间 天(默认值:30).
     */
    public static int getLogKeepTime() {
        String code = "system.logKeepTime";
        return Integer.parseInt(getConfigValue(code, "30"));
    }

    /**
     * 应用文件 系统日志文件保存路径
     *
     * @return
     */
    public static String getLogPath(String defaultPath) {
        String code = "system.logPath";
        String value = getConfigValue(code,defaultPath);
        if (StringUtils.isBlank(value)) {
            value = defaultPath;
        }
        return value;
    }


    /**
     * Oauth2拦截器是否启用
     *
     * @return
     */
    public static Boolean isOauth2Enable() {
        String code = "system.security.oauth2.enable";
        String value = getConfigValue(code,"false");
        return Boolean.valueOf(value);
    }

    /**
     * oauth2拦截器 默认：default；仅限包含URL或注释：include
     * @return
     */
    public static String getOauth2Scope() {
        String code = "system.security.oauth2.scope";
        return getConfigValue(code);
    }


    /**
     * oauth2 排除URL 多个之间以“,”分割
     * @return
     */
    public static String getOauth2ExcludePaths() {
        String code = "system.security.oauth2.excludePaths";
        return getConfigValue(code);
    }

    /**
     * oauth2 排除URL 多个之间以“,”分割
     * @return
     */
    public static List<String> getOauth2ExcludePathList() {
        String value = getOauth2ExcludePaths();
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }


    /**
     * oauth2 包含URL 多个之间以“,”分割
     * @return
     */
    public static String getOauth2IncludePaths() {
        String code = "system.security.oauth2.includePaths";
        return getConfigValue(code);
    }

    /**
     * oauth2 包含URL 多个之间以“,”分割
     * @return
     */
    public static List<String> getOauth2IncludePathList() {
        String value = getOauth2IncludePaths();
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }


    /**
     * 允许跨站 多个之间以“,”分割
     * @return
     */
    public static String getCorsAllowedOrigins() {
        String code = "system.security.cors.allowedOrigins";
        return getConfigValue(code);
    }

    /**
     * 允许跨站
     * @return
     */
    public static List<String> getCorsAllowedOriginList() {
        String value = getCorsAllowedOrigins();
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }

    /**
     * 登录账号白名单 不受“最大登录用户数限制” 每行一个或多个之间以";"分割
     * 自动转换成小写
     *
     * @return
     */
    public static List<String> getLimitUserWhiteList() {
        String code = "system.security.limit.user.whitelist";
        String value = getConfigValue(code);
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(StringUtils.split(StringUtils.trim(value.toUpperCase()).replaceAll("\r\n", ",").replaceAll("，", ",").replaceAll("；", ",").replaceAll(";", ","), ","));
        }
        return Collections.emptyList();
    }

    /**
     * 仅限IP白名单访问
     *
     * @return
     */
    public static Boolean isLimitIpEnable() {
        String code = "system.security.limit.ip.enable";
        String value = getConfigValue(code,"false");
        return Boolean.valueOf(value);
    }

    /**
     * 仅限IP白名单访问
     *
     * @return
     */
    public static Boolean isLimitIpWhiteEnable() {
        String code = "system.security.limit.ip.whiteEnable";
        String value = getConfigValue(code,"true");
        return Boolean.valueOf(value);
    }

    /**
     * #不通过应用集成账号验证的账号 每行一个或多个之间以";"分割
     * 自动转换成小写
     *
     * @return
     */
    public static List<String> getLimitIpWhiteList() {
        String code = "system.security.limit.ip.whitelist";
        String value = getConfigValue(code);
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(StringUtils.split(StringUtils.trim(value).replaceAll("\r\n", ",").replaceAll("，", ",").replaceAll("；", ",").replaceAll(";", ","), ","));
        }
        return Collections.emptyList();
    }

    /**
     * #不通过应用集成账号验证的账号 每行一个或多个之间以";"分割
     * 自动转换成小写
     *
     * @return
     */
    public static List<String> getLimitIpBlackList() {
        String code = "system.security.limit.ip.blacklist";
        String value = getConfigValue(code);
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(StringUtils.split(value.toUpperCase().replaceAll("，", ","), ","));
        }
        return Collections.emptyList();
    }


    /**
     * URL请求限制
     *
     * @return
     */
    public static Boolean isLimitUrlEnable() {
        String code = "system.security.limit.url.enable";
        String value = getConfigValue(code,"false");
        return Boolean.valueOf(value);
    }


    /**
     * 启用内部代理
     *
     * @return
     */
    public static Boolean isProxyEnable() {
        String code = "system.security.proxy.enable";
        String value = getConfigValue(code,"false");
        return Boolean.valueOf(value);
    }

    /**
     * 内部代理URL白名单 每行一个或多个之间以";"分割，支持"*"通配符
     * 自动转换成小写
     *
     * @return
     */
    public static List<String> getProxyWhiteList() {
        String code = "system.security.proxy.whitelist";
        String value = getConfigValue(code);
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(StringUtils.split(StringUtils.trim(value).replaceAll("\r\n", ",").replaceAll("，", ",").replaceAll("；", ",").replaceAll(";", ","), ","));
        }
        return Collections.emptyList();
    }


    /**
     * 应用文件 磁盘绝对路径
     *
     * @return
     */
    public static String getAppBasePath() {
        String code = "app.basePath";
        return getConfigValue(code);
    }


    /**
     * 应用文件存储目录 放置于webapp下 应用相对路径
     * 自动化部署 不推荐使用
     * 建议使用{@link AppConstants#getDiskBasePath()}
     *
     * @return
     */
    @Deprecated
    public static String getDiskBaseDir() {
        String code = "disk.baseDir";
        return getConfigValue(code);
    }


    /**
     * 云盘存储路径 磁盘绝对路径
     *
     * @return
     */
    public static String getDiskBasePath() {
        String code = "disk.basePath";
        String diskBasePath = getConfigValue(code);
        if (StringUtils.isBlank(diskBasePath)) {
            diskBasePath = getAppBasePath() + File.separator + "disk";
        }
        return diskBasePath;
    }


    /**
     * 文件缓存目录
     *
     * @return
     */
    public static String getDiskTempDir() {
        String code = "disk.tempDir";
        String tempDir = getConfigValue(code);
        if (StringUtils.isBlank(tempDir)) {
            tempDir = getAppBasePath() + File.separator + "temp";
        }
        FileUtils.checkSaveDir(tempDir);
        return tempDir;
    }

    /**
     * 单个文件上传最大 单位：字节
     *
     * @return
     */
    public static Integer getDiskMaxUploadSize() {
        String code = "disk.maxUploadSize";
        return Integer.valueOf(getConfigValue(code));
    }

    /**
     * 云盘附件上传大小限制 (Accepts units B KB MB GB)
     *
     * @return
     */
    public static String getPrettyDiskMaxUploadSize() {
        Integer maxUploadSize = getDiskMaxUploadSize();
        return PrettyMemoryUtils.prettyByteSize(maxUploadSize);
    }

    /**
     * 通知附件上传大小限制
     *
     * @return
     */
    public static Integer getNoticeMaxUploadSize() {
        String code = "notice.maxUploadSize";
        String value = getConfigValue(code);
        if (StringUtils.isBlank(value)) {
            return getDiskMaxUploadSize();
        }
        return Integer.valueOf(value);
    }

    /**
     * 通知附件上传大小限制 (Accepts units B KB MB GB)
     *
     * @return
     */
    public static String getPrettyNoticeMaxUploadSize() {
        Integer maxUploadSize = getNoticeMaxUploadSize();
        return PrettyMemoryUtils.prettyByteSize(maxUploadSize);
    }


    /**
     * 启用安全检查
     *
     * @return
     */
    public static boolean getIsSecurityOn() {
        String code = "security.on";
        String value = getConfigValue(code, "false");
        return "true".equals(value) || "1".equals(value);
    }

    /**
     * 启用登录密码安全检查
     *
     * @return
     */
    public static boolean isCheckLoginPassword() {
        String code = "security.password.checkLogin";
        String value = getConfigValue(code, "false");
        return "true".equals(value) || "1".equals(value);
    }

    /**
     * 启用强密码策略
     *
     * @return
     */
    public static boolean isCheckPasswordPolicy() {
        String code = "security.password.checkPolicy";
        String value = getConfigValue(code, "false");
        return "true".equals(value) || "1".equals(value);
    }


    /**
     * 修改密码地址 PC端
     * @return
     */
    public static String getSecurityUpdatePasswordUrlPc() {
        String code = "security.updatePasswordUrl.pc";
        return getConfigValue(code);
    }

    /**
     * 修改密码地址 移动端
     * @return
     */
    public static String getSecurityUpdatePasswordUrlMobile() {
        String code = "security.updatePasswordUrl.mobile";
        return getConfigValue(code);
    }



    /**
     * 系统最大登录用户数
     *
     * @return
     */
    public static int getSessionUserMaxSize() {
        String code = "security.sessionUser.MaxSize";
        String value = getConfigValue(code);
        return Integer.valueOf(value);
    }


    /**
     * 获取用户可创建会话数量 默认值：0
     * 0 无限制
     *
     * @return
     */
    public static int getUserSessionSize() {
        String code = "security.sessionUser.UserSessionSize";
        String value = getConfigValue(code);
        return StringUtils.isBlank(value) ? 0 : Integer.valueOf(value);
    }

    /**
     * 非法登录次数不超过X次
     *
     * @return
     */
    public static int getLoginAgainSize() {
        String code = "security.password.loginAgainSize";
        String value = getConfigValue(code);
        return StringUtils.isBlank(value) ? 3 : Integer.valueOf(value);
    }

    /**
     * 用户密码更新周期 （天） 默认值：30
     *
     * @return
     */
    public static int getUserPasswordUpdateCycle() {
        String code = "security.password.updateCycle";
        String value = getConfigValue(code);
        return StringUtils.isBlank(value) ? 30 : Integer.valueOf(value);
    }

    /**
     * 用户密码至少多少次内不能重复 不校验：0 默认值：5
     *
     * @return
     */
    public static int getUserPasswordRepeatCount() {
        String code = "security.password.repeatCount";
        String value = getConfigValue(code);
        return StringUtils.isBlank(value) ? 5 : Integer.valueOf(value);
    }


    /**
     * webserice发布地址
     *
     * @return
     */
    public static String getWebServiceUrl() {
        String code = "webservice.url";
        return getConfigValue(code);
    }

    /**
     * 当前应用服务地址（包含应用上下文）
     * @return
     */
    public static String getAppURL() {
        String code = "app.url";
        return getConfigValue(code);
    }


    /**
     * 应用名称
     * @return
     */
    public static String getAppName() {
        String code = "app.name";
        return getConfigValue(code);
    }

    /**
     * 应用简称
     * @return
     */
    public static String getAppShortName() {
        String code = "app.shortName";
        return getConfigValue(code);
    }

    /**
     * 应用名称全称
     * @return
     */
    public static String getAppFullName() {
        String code = "app.fullName";
        return getConfigValue(code);
    }

    /**
     * 系统登录后跳转主页
     * @return
     */
    public static String getAppHomePage() {
        String code = "app.homePage";
        return getConfigValue(code);
    }

    /**
     * 系统Portal首页
     * @return
     */
    public static String getAppPortalPage() {
        String code = "app.portalPage";
        return getConfigValue(code,"/a/portal");
    }

    /**
     * 软件版本
     * @return
     */
    public static String getAppVersion() {
        String code = "app.version";
        return getConfigValue(code);
    }

    /**
     * 厂商
     * @return
     */
    public static String getAppProductName() {
        String code = "app.productName";
        return getConfigValue(code);
    }

    /**
     * 厂商 网址
     * @return
     */
    public static String getAppProductURL() {
        String code = "app.productURL";
        return getConfigValue(code);
    }

    /**
     * 客服信息
     * @return
     */
    public static String getAppProductContact() {
        String code = "app.productContact";
        return getConfigValue(code);
    }


    /**
     * 客服链接
     * @return
     */
    public static String getAppProductContactUrl() {
        String code = "app.productContactUrl";
        return getConfigValue(code);
    }

    /**
     * REST 服务是否启用
     * @return
     */
    public static boolean getIsSystemRestEnable() {
        String code = "system.rest.enable";
        String value = getConfigValue(code, "false");
        return "true".equals(value) || "1".equals(value);
    }


    /**
     * REST 服务访问密钥
     * @return
     */
    public static String getRestDefaultApiKey() {
        String code = "system.rest.defaultApiKey";
        return getConfigValue(code,"");
    }

    /**
     * REST IP白名单访问限制
     * @return
     */
    public static boolean getIsSystemRestLimitEnable() {
        String code = "system.rest.limit.ip.enable";
        String value = getConfigValue(code, "true");
        return "true".equals(value) || "1".equals(value);
    }

    /**
     * REST IP访问白名单 每行一个或多个之间以";"分割，支持"*"通配符
     *
     * @return
     */
    public static List<String> getRestLimitIpWhiteList() {
        String code = "system.rest.limit.ip.whitelist";
        String value = getConfigValue(code);
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(StringUtils.split(StringUtils.trim(value).replaceAll("\r\n", ",").replaceAll("，", ",").replaceAll("；", ",").replaceAll(";", ","), ","));
        }
        return Collections.emptyList();
    }
}
