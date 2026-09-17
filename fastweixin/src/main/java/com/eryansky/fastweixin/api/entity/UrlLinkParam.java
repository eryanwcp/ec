package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序 URL Link 生成参数
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class UrlLinkParam extends BaseModel {

    /**
     * 通过 URL Link 进入的小程序页面路径，默认是主页
     */
    private String path;

    /**
     * 通过 URL Link 进入小程序时的query，最大1024个字符
     */
    private String query;

    /**
     * 生成的 URL Link 类型，到期类型：默认值0，长期有效；1，到期失效
     */
    @JsonProperty("is_expire")
    private Boolean isExpire;

    /**
     * 到期失效的 URL Link 的失效时间，为 Unix 时间戳。
     * 生成的到期失效 URL Link 在该时间前有效。最长有效期为30天。
     */
    @JsonProperty("expire_time")
    private Long expireTime;

    /**
     * 到期失效的 URL Link 失效类型，失效时间：0，失效间隔天数：1
     */
    @JsonProperty("expire_type")
    private Integer expireType;

    /**
     * 到期失效的 URL Link 的失效间隔天数。
     */
    @JsonProperty("expire_interval")
    private Integer expireInterval;

    /**
     * 默认值"release"。要打开的小程序版本。
     */
    @JsonProperty("env_version")
    private String envVersion;

    /**
     * 云开发静态网站自定义 H5 配置参数
     */
    @JsonProperty("cloud_base")
    private CloudBase cloudBase;

    public String getPath() {
        return path;
    }

    public UrlLinkParam setPath(String path) {
        this.path = path;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public UrlLinkParam setQuery(String query) {
        this.query = query;
        return this;
    }

    public Boolean getIsExpire() {
        return isExpire;
    }

    public UrlLinkParam setIsExpire(Boolean isExpire) {
        this.isExpire = isExpire;
        return this;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public UrlLinkParam setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public Integer getExpireType() {
        return expireType;
    }

    public UrlLinkParam setExpireType(Integer expireType) {
        this.expireType = expireType;
        return this;
    }

    public Integer getExpireInterval() {
        return expireInterval;
    }

    public UrlLinkParam setExpireInterval(Integer expireInterval) {
        this.expireInterval = expireInterval;
        return this;
    }

    public String getEnvVersion() {
        return envVersion;
    }

    public UrlLinkParam setEnvVersion(String envVersion) {
        this.envVersion = envVersion;
        return this;
    }

    public CloudBase getCloudBase() {
        return cloudBase;
    }

    public UrlLinkParam setCloudBase(CloudBase cloudBase) {
        this.cloudBase = cloudBase;
        return this;
    }

    /**
     * 云开发静态网站自定义 H5 配置参数
     */
    public static class CloudBase extends BaseModel {
        /**
         * 云开发环境
         */
        private String env;
        /**
         * 静态网站自定义域名
         */
        private String domain;
        /**
         * 第三方平台自定义域名
         */
        @JsonProperty("custom_domain")
        private String customDomain;

        public String getEnv() { return env; }
        public CloudBase setEnv(String env) { this.env = env; return this; }
        public String getDomain() { return domain; }
        public CloudBase setDomain(String domain) { this.domain = domain; return this; }
        public String getCustomDomain() { return customDomain; }
        public CloudBase setCustomDomain(String customDomain) { this.customDomain = customDomain; return this; }
    }
}
