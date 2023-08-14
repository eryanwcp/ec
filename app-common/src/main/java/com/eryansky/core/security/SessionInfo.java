/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security;

import com.eryansky.common.orm.persistence.AbstractBaseEntity;
import com.eryansky.common.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Maps;
import eu.bitwalker.useragentutils.DeviceType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * session登录用户对象.
 *
 * @author Eryan
 * @date 2013-3-24 下午2:53:59
 */
@SuppressWarnings("serial")
public class SessionInfo implements Serializable {

    /**
     * ID
     */
    private String id;
    /**
     * sessionID
     */
    private String sessionId;
    /**
     * 服务主机
     */
    private String host;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 登录名
     */
    private String loginName;
    /**
     * 员工编号
     */
    private String code;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 手机号(脱敏)
     */
    private String mobileSensitive;
    /**
     * 可选登录账号
     */
    private List<String> loginNames = new ArrayList<String>(0);
    /**
     * 登录姓名
     */
    private String name;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 客户端IP
     */
    private String ip;
    /**
     * 设备类型 {@link com.eryansky.core.security._enum.DeviceType}
     */
    private String systemDeviceType;
    /**
     * 设备类型 {@link DeviceType}
     */
    private String deviceType;
    /**
     * 客户端（浏览器）类型 {@link eu.bitwalker.useragentutils.Browser}
     */
    private String browserType;
    /**
     * 客户端
     */
    private String userAgent;
    /**
     * 角色ID集合
     */
    private List<String> roleIds;
    /**
     * 部门ID
     */
    private String loginOrganId;
    /**
     * 系统登录部门编码
     */
    private String loginOrganSysCode;
    /**
     * 机构ID
     */
    private String loginCompanyId;
    /**
     * 登录所在机构
     */
    private String loginCompanyCode;
    /**
     * 所在公司机构层级
     */
    private Integer loginCompanyLevel;
    /**
     * 上级机构ID
     */
    private String loginHomeCompanyId;
    /**
     * 上级机构编码
     */
    private String loginHomeCompanyCode;
    /**
     * 系统登录部门名称
     */
    private String loginOrganName;
    /**
     * 用户岗位
     */
    private List<PermissonPost> permissonPosts = new ArrayList<>(0);
    /**
     * 用户岗位
     */
    private List<String> postCodes = new ArrayList<String>(0);

    /**
     * 登录时间
     */
    private Date loginTime = new Date();
    /**
     * 最后访问时间
     */
    private Date updateTime = new Date();
    /**
     * 经度
     */
    private BigDecimal longitude;
    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * 精确度
     */
    private BigDecimal accuracy;
    /**
     * 授权角色
     */
    private List<PermissonRole> permissonRoles = new ArrayList<>(0);
    /**
     * 授权权限（菜单/功能）
     */
    private List<Permisson> permissons = new ArrayList<Permisson>(0);

    /**
     * APP客户端版本
     */
    private String appVersion;
    /**
     * 登录的设备编号
     */
    private String deviceCode;
    /**
     * 所属区域
     */
    private String areaCode;
    private String openId;
    /**
     * token
     */
    private String token;
    /**
     * 刷新Token
     */
    private String refreshToken;
    private String avatar;
    private String gender;

    /**
     * 自定义属性
     */
    private Map<String, Object> attributes;


    public SessionInfo() {
        this.attributes = Maps.newHashMap();
    }

    /**
     * ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置ID
     */
    public SessionInfo setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * sessionID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 设置 sessionID
     */
    public SessionInfo setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * 服务主机
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置 服务主机
     * @param host
     */
    public SessionInfo setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     */
    public SessionInfo setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * 登录名
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * 设置 登录名
     */
    public SessionInfo setLoginName(String loginName) {
        this.loginName = loginName;
        return this;
    }

    public String getCode() {
        return code;
    }

    public SessionInfo setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public SessionInfo setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getMobileSensitive() {
        return mobileSensitive;
    }

    public SessionInfo setMobileSensitive(String mobileSensitive) {
        this.mobileSensitive = mobileSensitive;
        return this;
    }

    public List<String> getLoginNames() {
        return loginNames;
    }

    public SessionInfo setLoginNames(List<String> loginNames) {
        this.loginNames = loginNames;
        return this;
    }


    public synchronized SessionInfo addIfNotExistLoginName(String loginName) {
        if (StringUtils.isBlank(loginName)) {
            return this;
        }
        if (!this.loginNames.contains(loginName)) {
            this.loginNames.add(loginName);
        }
        return this;
    }

    /**
     * 登录姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置 登录姓名
     */
    public SessionInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 用户类型
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 设置 用户类型
     */
    public SessionInfo setUserType(String userType) {
        this.userType = userType;
        return this;
    }

    /**
     * 客户端IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * 设置 客户端IP
     */
    public SessionInfo setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getBrowserType() {
        return browserType;
    }

    public SessionInfo setBrowserType(String browserType) {
        this.browserType = browserType;
        return this;
    }

    public String getSystemDeviceType() {
        return systemDeviceType;
    }

    public SessionInfo setSystemDeviceType(String systemDeviceType) {
        this.systemDeviceType = systemDeviceType;
        return this;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public SessionInfo setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public SessionInfo setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * 角色ID集合
     */
    public List<String> getRoleIds() {
        return roleIds;
    }

    /**
     * 设置 角色ID集合
     */
    public SessionInfo setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
        return this;
    }

    /**
     * 设置登录时间
     */
    public SessionInfo setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
        return this;
    }

    /**
     * 登录时间
     */
    // 设定JSON序列化时的日期格式
    @JsonFormat(pattern = AbstractBaseEntity.DATE_TIME_FORMAT, timezone = AbstractBaseEntity.TIMEZONE)
    public Date getLoginTime() {
        return loginTime;
    }

    @JsonFormat(pattern = AbstractBaseEntity.DATE_TIME_FORMAT, timezone = AbstractBaseEntity.TIMEZONE)
    public Date getUpdateTime() {
        return updateTime;
    }

    public SessionInfo setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public SessionInfo setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
        return this;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public SessionInfo setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
        return this;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public SessionInfo setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
        return this;
    }


    public String getLoginOrganId() {
        return loginOrganId;
    }

    public SessionInfo setLoginOrganId(String loginOrganId) {
        this.loginOrganId = loginOrganId;
        return this;
    }


    public String getLoginOrganSysCode() {
        return loginOrganSysCode;
    }

    public SessionInfo setLoginOrganSysCode(String loginOrganSysCode) {
        this.loginOrganSysCode = loginOrganSysCode;
        return this;
    }

    public String getLoginCompanyId() {
        return loginCompanyId;
    }

    public SessionInfo setLoginCompanyId(String loginCompanyId) {
        this.loginCompanyId = loginCompanyId;
        return this;
    }

    public String getLoginCompanyCode() {
        return loginCompanyCode;
    }

    public SessionInfo setLoginCompanyCode(String loginCompanyCode) {
        this.loginCompanyCode = loginCompanyCode;
        return this;
    }

    public Integer getLoginCompanyLevel() {
        return loginCompanyLevel;
    }

    public SessionInfo setLoginCompanyLevel(Integer loginCompanyLevel) {
        this.loginCompanyLevel = loginCompanyLevel;
        return this;
    }

    public String getLoginHomeCompanyId() {
        return loginHomeCompanyId;
    }

    public SessionInfo setLoginHomeCompanyId(String loginHomeCompanyId) {
        this.loginHomeCompanyId = loginHomeCompanyId;
        return this;
    }

    public String getLoginHomeCompanyCode() {
        return loginHomeCompanyCode;
    }

    public SessionInfo setLoginHomeCompanyCode(String loginHomeCompanyCode) {
        this.loginHomeCompanyCode = loginHomeCompanyCode;
        return this;
    }

    /**
     * 默认登录组织机构名称
     *
     * @return
     */
    public String getLoginOrganName() {
        return loginOrganName;
    }

    /**
     * 设置默认登录组织机构名称
     */
    public SessionInfo setLoginOrganName(String loginOrganName) {
        this.loginOrganName = loginOrganName;
        return this;
    }

    public List<String> getPostCodes() {
        return postCodes;
    }

    public SessionInfo setPostCodes(List<String> postCodes) {
        this.postCodes = postCodes;
        return this;
    }

    public List<PermissonPost> getPermissonPosts() {
        return permissonPosts;
    }

    public SessionInfo setPermissonPosts(List<PermissonPost> permissonPosts) {
        this.permissonPosts = permissonPosts;
        return this;
    }

    /**
     * 是否是超级管理员
     *
     * @return
     */
    public boolean isSuperUser() {
        return SecurityUtils.isUserAdmin(this.getUserId());
    }

    public List<PermissonRole> getPermissonRoles() {
        return permissonRoles;
    }

    public SessionInfo setPermissonRoles(List<PermissonRole> permissonRoles) {
        this.permissonRoles = permissonRoles;
        return this;
    }


    public SessionInfo addPermissonRoles(PermissonRole permissonRole) {
        this.permissonRoles.add(permissonRole);
        return this;
    }

    public List<Permisson> getPermissons() {
        return permissons;
    }

    public SessionInfo setPermissons(List<Permisson> permissons) {
        this.permissons = permissons;
        return this;
    }

    public SessionInfo addPermissons(Permisson permisson) {
        this.permissons.add(permisson);
        return this;
    }

    public boolean isMobileLogin() {
        if (StringUtils.isBlank(deviceType)) {
            return false;
        }
        DeviceType _deviceType = null;
        try {
            _deviceType = DeviceType.valueOf(deviceType);
        } catch (IllegalArgumentException e) {
            return "iOS".equalsIgnoreCase(deviceType)|| "android".equals(deviceType);
        }
        return DeviceType.MOBILE.equals(_deviceType) || DeviceType.TABLET.equals(_deviceType);
    }

    public String getAppVersion() {
        return appVersion;
    }

    public SessionInfo setAppVersion(String appVersion) {
        this.appVersion = appVersion;
        return this;
    }


    public String getDeviceCode() {
        return deviceCode;
    }

    public SessionInfo setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean isAuthenticated() {
        return true;
    }

    public String getPrincipal() {
        return name;
    }

    /**
     * 添加自定义属性
     * @param key key
     * @param object 值
     * @return
     */
    public SessionInfo addAttribute(String key, Object object) {
        this.attributes.put(key, object);
        return this;
    }

    /**
     * 返回自定义属性
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 自定义属性
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public SessionInfo setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public SessionInfo setAreaCode(String areaCode) {
        this.areaCode = areaCode;
        return this;
    }

    public String getOpenId() {
        return openId;
    }

    public SessionInfo setOpenId(String openId) {
        this.openId = openId;
        return this;
    }

    public String getToken() {
        return token;
    }

    public SessionInfo setToken(String token) {
        this.token = token;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public SessionInfo setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public SessionInfo setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public SessionInfo setGender(String gender) {
        this.gender = gender;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInfo that = (SessionInfo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
