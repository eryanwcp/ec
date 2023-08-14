/**
 * Copyright (c) 2012-2013 http://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.modules.sys._enum.OrganType;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.modules.sys._enum.LogType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.eryansky.modules.sys.utils.UserUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 系统操作日志
 *
 * @author Eryan
 * @date: 13-11-27 下午9:18
 */
@JsonFilter(" ")
public class Log extends DataEntity<Log> {

    /**
     * 操作日志类型
     *
     * @see LogType
     */
    private String type;

    /**
     * 用户IP地址
     */
    private String ip;
    /**
     * 设备类型 {@link eu.bitwalker.useragentutils.DeviceType}
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
     * 登录名
     */
    private String userId;
    /**
     * 操作开始时间
     */
    private Date operTime;
    /**
     * 操作耗时ms
     */
    private String actionTime;


    private String title;

    /**
     * 模块
     */
    private String module;
    /**
     * 操作方法
     */
    private String action;
    /**
     * 备注
     */
    private String remark;
    /**
     * 异常信息
     */
    private String exception;
    /**
     * 经度
     */
    private BigDecimal longitude;
    /**
     * 纬度
     */
    private BigDecimal latitude;


    /**
     * 查询条件
     */
    private String query;
    /**
     * 操作人姓名
     */
    private String userName;
    /**
     * 操作人部门名称
     */
    private String userOrganName;
    /**
     * 操作人单位名称
     */
    private String userCompanyName;
    /**
     * 开始时间
     */
    private Long startTime;
    /**
     * 结束时间
     */
    private Long endTime;

    public Log() {
        super();
    }

    public Log(String id) {
        super(id);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getOperTime() {
        return operTime;
    }

    public void setOperTime(Date operTime) {
        this.operTime = operTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionTime() {
        return actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }


    public String getBrowserType() {
        return browserType;
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    //    @JsonIgnore
    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }


    /**
     * 设置请求参数
     *
     * @param paramMap
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setParams(Map paramMap) {
        if (paramMap == null) {
            return;
        }
        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, String[]> param : ((Map<String, String[]>) paramMap).entrySet()) {
            params.append(("".equals(params.toString()) ? "" : "&") + param.getKey() + "=");
            String paramValue = (param.getValue() != null && param.getValue().length > 0 ? param.getValue()[0] : "");
            params.append(StringUtils.abbr(StringUtils.endsWithIgnoreCase(param.getKey(), "password") ? "" : paramValue, 100));
        }
        if (StringUtils.isNotBlank(this.remark)) {
            this.remark = params.insert(0, this.remark).toString();
        } else {
            this.remark = params.toString();
        }
    }


    /**
     * 日志类类型 View
     */
    public String getTypeView() {
        return GenericEnumUtils.getDescriptionByValue(LogType.class,type,type);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserOrganName() {
        return userOrganName;
    }

    public void setUserOrganName(String userOrganName) {
        this.userOrganName = userOrganName;
    }

    public String getUserCompanyName() {
        return userCompanyName;
    }

    public void setUserCompanyName(String userCompanyName) {
        this.userCompanyName = userCompanyName;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}