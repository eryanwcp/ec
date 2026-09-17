package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序 Scheme 生成参数
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class SchemeParam extends BaseModel {

    /**
     * 跳转到的目标小程序信息
     */
    @JsonProperty("jump_wxa")
    private JumpWxa jumpWxa;

    /**
     * 成的scheme码类型，到期类型：默认值0，长期有效；1，到期失效
     */
    @JsonProperty("is_expire")
    private Boolean isExpire;

    /**
     * 到期失效的scheme码的失效时间，为Unix时间戳。生成的到期失效scheme码在该时间前有效。
     * 最长有效期为30天。生成到期失效的scheme时必填。
     */
    @JsonProperty("expire_time")
    private Long expireTime;

    /**
     * 到期失效的 scheme 码失效类型，失效时间：0，失效间隔天数：1
     */
    @JsonProperty("expire_type")
    private Integer expireType;

    /**
     * 到期失效的 scheme 码的失效间隔天数。
     * 生成的到期失效 scheme 码在该间隔时间到达前有效。最长间隔天数为30天。
     */
    @JsonProperty("expire_interval")
    private Integer expireInterval;

    public JumpWxa getJumpWxa() {
        return jumpWxa;
    }

    public SchemeParam setJumpWxa(JumpWxa jumpWxa) {
        this.jumpWxa = jumpWxa;
        return this;
    }

    public Boolean getIsExpire() {
        return isExpire;
    }

    public SchemeParam setIsExpire(Boolean isExpire) {
        this.isExpire = isExpire;
        return this;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public SchemeParam setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public Integer getExpireType() {
        return expireType;
    }

    public SchemeParam setExpireType(Integer expireType) {
        this.expireType = expireType;
        return this;
    }

    public Integer getExpireInterval() {
        return expireInterval;
    }

    public SchemeParam setExpireInterval(Integer expireInterval) {
        this.expireInterval = expireInterval;
        return this;
    }

    /**
     * 跳转到的目标小程序信息
     */
    public static class JumpWxa extends BaseModel {
        /**
         * 通过 scheme 码进入的小程序页面路径
         */
        private String path;
        /**
         * 通过 scheme 码进入小程序时的query
         */
        private String query;
        /**
         * 默认值0，跳转小程序的环境版本
         * 0-正式版，1-开发版，2-体验版
         */
        @JsonProperty("env_version")
        private String envVersion;

        public String getPath() {
            return path;
        }

        public JumpWxa setPath(String path) {
            this.path = path;
            return this;
        }

        public String getQuery() {
            return query;
        }

        public JumpWxa setQuery(String query) {
            this.query = query;
            return this;
        }

        public String getEnvVersion() {
            return envVersion;
        }

        public JumpWxa setEnvVersion(String envVersion) {
            this.envVersion = envVersion;
            return this;
        }
    }
}
