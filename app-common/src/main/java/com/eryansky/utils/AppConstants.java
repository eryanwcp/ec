/**
 *  Copyright (c) 2012-2026 https://www.eryansky.com
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
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.utils.SerializerFactory;
import com.eryansky.modules.sys.service.ConfigService;
import com.eryansky.modules.sys.vo.OAuth2Client;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * 系统使用的静态变量.
 *
 * @author Eryan
 * @date 2013-03-17 上午8:25:36
 */
public class AppConstants extends SysConstants {

    private static final Logger log = LoggerFactory.getLogger(AppConstants.class);

    /**
     * 系统初始化时间戳
     */
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

    private static final Splitter SPLITTER = Splitter.onPattern("[,，;；\\r\\n]")
            .trimResults()       // 自动剔除每个元素的前后空格
            .omitEmptyStrings(); // 自动忽略空元素
    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    private static final class Static {
        private static final PropertiesLoader config = initConfig();

        private static PropertiesLoader initConfig() {
            String activeProfile = null;
            try {
                String[] profiles = getAppConfig().getActiveProfiles();
                if (profiles != null && profiles.length > 0) {
                    activeProfile = profiles[0];
                }
            } catch (Exception e) {
                log.warn("获取 activeProfile 失败，将使用默认配置", e);
            }
            return new PropertiesLoader("config" + (null == activeProfile ? "" : "-" + activeProfile) + ".properties");
        }
    }

    /**
     * 安全获取 ConfigService 实例（避免 Spring 容器未就绪时直接抛异常）
     */
    private static ConfigService getConfigService() {
        try {
            return SpringContextHolder.getBean(ConfigService.class);
        } catch (Exception e) {
            log.debug("ConfigService 未初始化或获取失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将字符串按分隔符切分为列表（复用分割逻辑）
     */
    public static List<String> splitToList(String value) {
        if (StringUtils.isNotBlank(value)) {
            return SPLITTER.splitToList(value);
        }
        return Collections.emptyList();
    }

    /**
     * 安全解析 int 配置
     */
    private static int getIntConfig(String code, int defaultValue) {
        String val = getConfigValue(code);
        if (StringUtils.isBlank(val)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            log.warn("配置项 [{}] 值 [{}] 无法转换为整数，使用默认值: {}", code, val, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 安全解析 long 配置
     */
    private static long getLongConfig(String code, long defaultValue) {
        String val = getConfigValue(code);
        if (StringUtils.isBlank(val)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            log.warn("配置项 [{}] 值 [{}] 无法转换为长整型，使用默认值: {}", code, val, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 更新系统初始化时间为当前时间戳
     */
    public static void updateSysInitTime() {
        SYS_INIT_TIME = System.currentTimeMillis();
    }

    /**
     * 更新系统初始化时间为指定的时间戳
     *
     * @param initTime 指定的毫秒级时间戳
     */
    public static void updateSysInitTime(long initTime) {
        SYS_INIT_TIME = initTime;
    }

    /**
     * 获取jdbc交校验sql
     */
    public static String getJdbcValidationQuery() {
        return getAppConfig().getProperty("jdbc.validationQuery");
    }

    /**
     * 获取管理端根路径
     */
    public static String getAdminPath() {
        return getAppConfig().getProperty("adminPath");
    }

    /**
     * 获取前端根路径
     */
    public static String getFrontPath() {
        return getAppConfig().getProperty("frontPath");
    }

    /**
     * 获取移动端根路径
     */
    public static String getMobilePath() {
        return getAppConfig().getProperty("mobilePath");
    }

    /**
     * 获取URL后缀
     */
    public static String getUrlSuffix() {
        return getAppConfig().getProperty("urlSuffix");
    }

    /**
     * 系统文件存储方式
     */
    public static String getSystemDiskType() {
        return getAppConfig().getProperty("system.disk.type");
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
        if (isdevMode()) {
            //调试模式 从本地配置文件读取
            return getConfig(code, defaultValue);
        }
        ConfigService configService = getConfigService();
        if (configService != null) {
            String configValue = configService.getConfigValueByCode(code);
            if (StringUtils.isNotBlank(configValue)) {
                return configValue;
            }
        }
        return getConfig(code, defaultValue);
    }

    /**
     * 日志保留时间 天(默认值:30).
     */
    public static int getLogKeepTime() {
        return getIntConfig("system.logKeepTime", 30);
    }

    /**
     * 应用文件 系统日志文件保存路径
     *
     * @return
     */
    public static String getLogPath(String defaultPath) {
        String code = "system.logPath";
        String value = getConfigValue(code, defaultPath);
        if (StringUtils.isBlank(value)) {
            value = defaultPath;
        }
        return value;
    }

    /**
     * 权限拦截器是否启用
     *
     * @return
     */
    public static boolean isAuthEnable() {
        String code = "system.security.auth.enable";
        String value = getConfigValue(code, "true");
        return Boolean.parseBoolean(value);
    }

    /**
     * auth 排除URL 多个之间以“,”分割
     * @return
     */
    public static String getAuthExcludePaths() {
        String code = "system.security.auth.excludePaths";
        return getConfigValue(code, "");
    }

    /**
     * auth 排除URL 多个之间以“,”分割
     * @return
     */
    public static List<String> getAuthExcludePathList() {
        return splitToList(getAuthExcludePaths());
    }

    /**
     * Oauth2拦截器是否启用
     *
     * @return
     */
    public static Boolean isOauth2SSOEnable() {
        String code = "system.security.oauth2.sso.enable";
        String value = getConfigValue(code, "false");
        return Boolean.parseBoolean(value);
    }

    /**
     * oauth2 SSO排除URL 多个之间以“,”分割
     * @return
     */
    public static String getOauth2SSOExcludePaths() {
        String code = "system.security.oauth2.sso.excludePaths";
        return getConfigValue(code);
    }

    /**
     * oauth2 SSO排除URL 多个之间以“,”分割
     * @return
     */
    public static List<String> getOauth2SSOExcludePathList() {
        return splitToList(getOauth2SSOExcludePaths());
    }

    /**
     * oauth2 SSO 包含URL 多个之间以“,”分割
     * @return
     */
    public static String getOauth2SSOIncludePaths() {
        String code = "system.security.oauth2.sso.includePaths";
        return getConfigValue(code);
    }

    /**
     * oauth2 SSO 包含URL 多个之间以“,”分割
     * @return
     */
    public static List<String> getOauth2SSOIncludePathList() {
        return splitToList(getOauth2SSOIncludePaths());
    }

    /**
     * Oauth2拦截器是否启用
     *
     * @return
     */
    public static Boolean isOauth2Enable() {
        String code = "system.security.oauth2.enable";
        String value = getConfigValue(code, "false");
        return Boolean.parseBoolean(value);
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
        return splitToList(getOauth2ExcludePaths());
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
        return splitToList(getOauth2IncludePaths());
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
        return splitToList(getCorsAllowedOrigins());
    }

    /**
     * 登录账号白名单 不受“最大登录用户数限制” 每行一个或多个之间以";"分割
     * 自动转换成小写
     *
     * @return
     */
    public static List<String> getLimitUserWhiteList() {
        String code = "system.security.limit.user.whitelist";
        return splitToList(getConfigValue(code));
    }

    /**
     * 仅限IP白名单访问
     *
     * @return
     */
    public static Boolean isLimitIpEnable() {
        String code = "system.security.limit.ip.enable";
        String value = getConfigValue(code, "false");
        return Boolean.valueOf(value);
    }

    /**
     * 仅限IP白名单访问
     *
     * @return
     */
    public static Boolean isLimitIpWhiteEnable() {
        String code = "system.security.limit.ip.whiteEnable";
        String value = getConfigValue(code, "true");
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
        return splitToList(getConfigValue(code));
    }

    /**
     * #不通过应用集成账号验证的账号 每行一个或多个之间以";"分割
     * 自动转换成小写
     *
     * @return
     */
    public static List<String> getLimitIpBlackList() {
        String code = "system.security.limit.ip.blacklist";
        return splitToList(getConfigValue(code));
    }

    /**
     * 仅限IP白名单访问
     *
     * @return
     */
    public static Boolean isXssEnable() {
        String code = "system.security.xssFilter.enable";
        String value = getAppConfig(code, "true");
        return Boolean.valueOf(value);
    }

    /**
     * XSS拦截黑名单 不拦截 每行一个或多个之间以";"分割
     * 自动转换成小写
     *
     * @return
     */
    public static String getXssBlackListURL() {
        String code = "system.security.xssFilter.blackListURL";
        return getAppConfig(code);
    }

    /**
     * XSS拦截黑名单 不拦截
     *
     * @return
     */
    public static List<String> getXssBlackList() {
        return splitToList(getXssBlackListURL());
    }

    /**
     * URL请求限制
     *
     * @return
     */
    public static Boolean isLimitUrlEnable() {
        String code = "system.security.limit.url.enable";
        String value = getConfigValue(code, "false");
        return Boolean.valueOf(value);
    }

    /**
     * 启用内部代理
     *
     * @return
     */
    public static Boolean isProxyEnable() {
        String code = "system.security.proxy.enable";
        String value = getConfigValue(code, "false");
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
        return splitToList(getConfigValue(code));
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
    public static Long getDiskMaxUploadSize() {
        return getLongConfig("disk.maxUploadSize", 0L);
    }

    /**
     * 云盘附件上传大小限制 (Accepts units B KB MB GB)
     *
     * @return
     */
    public static String getPrettyDiskMaxUploadSize() {
        Long maxUploadSize = getDiskMaxUploadSize();
        return PrettyMemoryUtils.prettyByteSize(maxUploadSize);
    }

    /**
     * 通知附件上传大小限制
     *
     * @return
     */
    public static Long getNoticeMaxUploadSize() {
        String code = "notice.maxUploadSize";
        String value = getConfigValue(code);
        if (StringUtils.isBlank(value)) {
            return getDiskMaxUploadSize();
        }
        return getLongConfig("notice.maxUploadSize", getDiskMaxUploadSize());
    }

    /**
     * 通知附件上传大小限制 (Accepts units B KB MB GB)
     *
     * @return
     */
    public static String getPrettyNoticeMaxUploadSize() {
        Long maxUploadSize = getNoticeMaxUploadSize();
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
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * 启用登录密码安全检查
     *
     * @return
     */
    public static boolean isCheckLoginPassword() {
        String code = "security.password.checkLogin";
        String value = getConfigValue(code, "false");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * 启用强密码策略
     *
     * @return
     */
    public static boolean isCheckPasswordPolicy() {
        String code = "security.password.checkPolicy";
        String value = getConfigValue(code, "false");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
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
        return getIntConfig("security.sessionUser.MaxSize", 0);
    }

    /**
     * 获取用户可创建会话数量 默认值：0
     * 0 无限制
     *
     * @return
     */
    public static int getUserSessionSize() {
        return getIntConfig("security.sessionUser.UserSessionSize", 0);
    }

    /**
     * 非法登录次数不超过X次
     *
     * @return
     */
    public static int getLoginAgainSize() {
        return getIntConfig("security.password.loginAgainSize", 3);
    }

    /**
     * 用户密码更新周期 （天） 默认值：30
     *
     * @return
     */
    public static int getUserPasswordUpdateCycle() {
        return getIntConfig("security.password.updateCycle", 30);
    }

    /**
     * 用户密码至少多少次内不能重复 不校验：0 默认值：5
     *
     * @return
     */
    public static int getUserPasswordRepeatCount() {
        return getIntConfig("security.password.repeatCount", 5);
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
        return getConfigValue(code, "/a/portal");
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
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * REST 服务默认拦截器是否启用
     * @return
     */
    public static boolean isRestDefaultInterceptorEnable() {
        String code = "system.rest.defaultInterceptor.enable";
        String value = getConfigValue(code, "true");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * REST 服务访问密钥
     * @return
     */
    public static String getRestDefaultApiKey() {
        String code = "system.rest.defaultApiKey";
        return getConfigValue(code, "");
    }

    /**
     * REST IP白名单访问限制
     * @return
     */
    public static boolean getIsSystemRestLimitEnable() {
        String code = "system.rest.limit.ip.enable";
        String value = getConfigValue(code, "true");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * REST IP访问白名单 每行一个或多个之间以";"分割，支持"*"通配符
     *
     * @return
     */
    public static List<String> getRestLimitIpWhiteList() {
        String code = "system.rest.limit.ip.whitelist";
        return splitToList(getConfigValue(code));
    }

    /**
     * RPC 服务认证客户端密钥
     * @return
     */
    public static String getRPCClientApiKey() {
        String code = "system.rpc.client.apiKey";
        return getConfigValue(code, "");
    }

    /**
     * RPC 服务客户端序列化方式
     * @return
     */
    public static String getRPCClientSerializer() {
        String code = "system.rpc.client.serializer";
        return getConfigValue(code, SerializerFactory.DEFAULT_SERIALIZER);
    }

    /**
     * 统预警消息推送（运维账号） 接收消息账号
     * @return
     */
    public static String getSystemOpsWarnLoginNames() {
        String code = "system.ops.warn.loginNames";
        return getConfigValue(code);
    }

    /**
     * 统预警消息推送（运维账号） 接收消息账号
     * @return
     */
    public static List<String> getSystemOpsWarnLoginNameList() {
        return splitToList(getSystemOpsWarnLoginNames());
    }

    /**
     * 并发线程数
     *
     * @return
     */
    public static Integer getPoolParallelism() {
        String code = "system.pool.parallelism";
        String value = getConfigValue(code, "");
        if (StringUtils.isNotBlank(value)) {
            try {
                return Integer.valueOf(value.trim());
            } catch (NumberFormatException e) {
                log.warn("配置项 [{}] 值 [{}] 解析错误，返回 null", code, value);
            }
        }
        return null;
    }

    /**
     * 序列化注册 示例：com.erynasky.* 白名单 多个之间检以";"分割
     * @return
     */
    public static String getSerializerTypeCheckAllowClasses() {
        String code = "system.security.SerializerTypeCheck.allowClasses";
        return getConfigValue(code, "com.eryansky.*");
    }

    public static List<String> getSerializerTypeCheckAllowClassList() {
        return splitToList(getSerializerTypeCheckAllowClasses());
    }

    /**
     * 序列化注册  黑名单(权限等级高于白名单) 多个之间检以";"分割
     * @return
     */
    public static String getSerializerTypeCheckDisallowClasses() {
        String code = "system.security.SerializerTypeCheck.disallowClasses";
        return getConfigValue(code);
    }

    public static List<String> getSerializerTypeCheckDisallowClassList() {
        return splitToList(getSerializerTypeCheckDisallowClasses());
    }

    /**
     * 是否启用SSO单点登录
     * @return
     */
    public static boolean getIsSSOEnable() {
        String code = "system.sso.enable";
        String value = getAppConfig(code, "false");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * SSO单点登录签名者
     * @return
     */
    public static String getSSOIssuer() {
        String code = "system.sso.issuer";
        return getAppConfig(code, "");
    }

    /**
     * SSO单点登录页面
     * @return
     */
    public static String getSSOIssuerUri() {
        String code = "system.sso.issuerUri";
        return getAppConfig(code, "");
    }

    /**
     * SSO单点登录 客户端标识
     * @return
     */
    public static String getSSOClientId() {
        String code = "system.sso.clientId";
        return getAppConfig(code, "");
    }

    /**
     * SSO单点登录密钥 密钥16字节 hex加密密钥
     * 参考方法可自动生成密钥 {@link Sm4Utils#generateHexKeyString}
     * @return
     */
    public static String getSSOClientSecret() {
        String code = "system.sso.clientSecret";
        return getAppConfig(code, "");
    }

    /**
     * SSO单点登录 回调地址
     * @return
     */
    public static String getSSOCallbackUrl() {
        String code = "system.sso.callbackUrl";
        return getAppConfig(code, "");
    }

    /**
     * Oauth2 客户端配置
     * @return
     */
    public static String getOauth2Clients() {
        String code = "system.oauth2.clients";
        return getConfigValue(code, "");
    }

    /**
     * Oauth2 客户端配置
     * @return
     */
    public static List<OAuth2Client> getOauth2ClientList() {
        String value = getOauth2Clients();
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        List<OAuth2Client> clients = JsonMapper.getInstance().toJavaObjectList(value, OAuth2Client.class);
        return clients != null ? clients : Collections.emptyList();
    }
}