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
    private String key;
    private String keyEncodeUrl;
    private String loginUser;
    private String host;
    private String clientIP;

    private Map<String, Object> data;

    private Long ttl1;
    private Long ttl2;

    private Date createdTime;
    private Date updateTime;

    public SessionVo() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyEncodeUrl() {
        return keyEncodeUrl;
    }

    public void setKeyEncodeUrl(String keyEncodeUrl) {
        this.keyEncodeUrl = keyEncodeUrl;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Long getTtl1() {
        return ttl1;
    }

    public void setTtl1(Long ttl1) {
        this.ttl1 = ttl1;
    }

    public Long getTtl2() {
        return ttl2;
    }

    public void setTtl2(Long ttl2) {
        this.ttl2 = ttl2;
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @JsonFormat(pattern = DATE_TIME_FORMAT, timezone = TIMEZONE)
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}