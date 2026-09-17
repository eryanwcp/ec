/**
 * Copyright (c) XXX有限公司 2013-2026 https://github.com/eryanwcp/ec
 *
 */
package com.eryansky.modules.sys.mapper;


import com.eryansky.core.orm.mybatis.entity.PDataEntity;

/**
 * 用户登录设备
 * @author eryan
 * @date 2026-09-16
 */
public class UserDevice extends PDataEntity<UserDevice, String> {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备唯一标识/指纹
     */
    private String deviceId;
    /**
     * 设备类型
     */
    private String deviceType;
    /**
     * 历史登录IP集合信息
     */
    private String ips;
    /**
     * 最近登录地理位置
     */
    private String location;
    /**
     * 成功登录次数
     */
    private Integer loginCount;
    /**
     * 是否为常见设备: 0-否, 1-是
     */
    private String isCommon;
    /**
     * 首次使用时间
     */
    private java.util.Date firstLoginTime;
    /**
     * 最后登录时间
     */
    private java.util.Date lastLoginTime;
    /**
     * 自定义扩展数据 {'key1':Object,'key2':Object}
     */
    private String extendAttr;

    public UserDevice() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public void setIps(String ips) {
        this.ips = ips;
    }

    public String getIps() {
        return this.ips;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Integer getLoginCount() {
        return this.loginCount;
    }

    public void setIsCommon(String isCommon) {
        this.isCommon = isCommon;
    }

    public String getIsCommon() {
        return this.isCommon;
    }

    public void setFirstLoginTime(java.util.Date firstLoginTime) {
        this.firstLoginTime = firstLoginTime;
    }

    public java.util.Date getFirstLoginTime() {
        return this.firstLoginTime;
    }

    public void setLastLoginTime(java.util.Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public java.util.Date getLastLoginTime() {
        return this.lastLoginTime;
    }

    public void setExtendAttr(String extendAttr) {
        this.extendAttr = extendAttr;
    }

    public String getExtendAttr() {
        return this.extendAttr;
    }

}
