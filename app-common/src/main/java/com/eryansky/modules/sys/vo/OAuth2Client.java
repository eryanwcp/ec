package com.eryansky.modules.sys.vo;

import java.io.Serializable;
import java.util.List;

/**
 * OAuth2客户端
 */
public class OAuth2Client implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户端系统名称（可选，用于日志和标记）
     */
    private String clientName;

    /**
     * 客户端唯一标识 (Client ID)
     */
    private String clientId;

    /**
     * 客户端密钥 (Client Secret)
     */
    private String clientSecret;

    /**
     * 允许访问的客户端 IP 列表 / 白名单（为空代表不限制）
     */
    private List<String> clientIps;

    /**
     * 允许重定向的回调地址白名单列表
     */
    private List<String> redirectUris;
    /**
     * 授权列表
     */
    private List<String> permissions;

    public OAuth2Client() {
        this.permissions = new java.util.ArrayList<>();
        this.redirectUris = new java.util.ArrayList<>();
        this.clientIps = new java.util.ArrayList<>();
    }

    public String getClientName() {
        return clientName;
    }

    /**
     * 设置客户端名称并返回当前对象，支持链式调用
     */
    public OAuth2Client setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    /**
     * 设置客户端ID并返回当前对象，支持链式调用
     */
    public OAuth2Client setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * 设置客户端密钥并返回当前对象，支持链式调用
     */
    public OAuth2Client setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public List<String> getClientIps() {
        return clientIps;
    }

    /**
     * 设置客户端IP列表并返回当前对象，支持链式调用
     */
    public OAuth2Client setClientIps(List<String> clientIps) {
        this.clientIps = clientIps;
        return this;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    /**
     * 设置重定向URI列表并返回当前对象，支持链式调用
     */
    public OAuth2Client setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
        return this;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * 设置授权列表并返回当前对象，支持链式调用
     */
    public OAuth2Client setPermissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }
}
