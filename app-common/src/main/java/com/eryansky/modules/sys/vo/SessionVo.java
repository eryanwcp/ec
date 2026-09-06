package com.eryansky.modules.sys.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static com.eryansky.client.common.utils.Constants.DATE_TIME_FORMAT;
import static com.eryansky.client.common.utils.Constants.TIMEZONE;

/**
 * Session 信息
 */
public class SessionVo implements Serializable {
    private String id;
    private String loginUser;
    private String host;
    private String clientIP;

    private Map<String, Object> data;

    /**
     * 请求次数
     */
    private Long accessCount;

    private Long ttl1;
    private Long ttl2;

    private Date createdTime;
    private Date updateTime;

    public SessionVo() {
    }

    public String getId() {
        return id;
    }

    public SessionVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public SessionVo setLoginUser(String loginUser) {
        this.loginUser = loginUser;
        return this;
    }

    public String getHost() {
        return host;
    }

    public SessionVo setHost(String host) {
        this.host = host;
        return this;
    }

    public String getClientIP() {
        return clientIP;
    }

    public SessionVo setClientIP(String clientIP) {
        this.clientIP = clientIP;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public SessionVo setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public SessionVo setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
        return this;
    }

    public Long getTtl1() {
        return ttl1;
    }

    public SessionVo setTtl1(Long ttl1) {
        this.ttl1 = ttl1;
        return this;
    }

    public Long getTtl2() {
        return ttl2;
    }

    public SessionVo setTtl2(Long ttl2) {
        this.ttl2 = ttl2;
        return this;
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getCreatedTime() {
        return createdTime;
    }

    public SessionVo setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getUpdateTime() {
        return updateTime;
    }

    public SessionVo setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}