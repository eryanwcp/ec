package com.eryansky.modules.sys.vo;

import java.io.Serializable;
import java.util.Map;

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

    private String created_at;
    private String lastAccess_at;

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

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getLastAccess_at() {
        return lastAccess_at;
    }

    public void setLastAccess_at(String lastAccess_at) {
        this.lastAccess_at = lastAccess_at;
    }
}