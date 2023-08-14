/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.j2cache.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Eryan
 * @date 2019-02-11
 */
@ConfigurationProperties("j2cache.session")
public class J2CacheSessionProperties {

    private final Filter filter = new Filter();
    /**
     * session数量
     */
    private String maxSizeInMemory;
    /**
     * session有效时间 单位：秒
     */
    private String maxAge;
    private final Redis redis = new Redis();


    public Filter getFilter() {
        return filter;
    }


    public String getMaxSizeInMemory() {
        return maxSizeInMemory;
    }

    public void setMaxSizeInMemory(String maxSizeInMemory) {
        this.maxSizeInMemory = maxSizeInMemory;
    }

    public String getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }

    public Redis getRedis() {
        return redis;
    }

    public static class Filter {
        /**
         * Enable SessionFilter.
         */
        private boolean enabled = true;
        private Integer order;
        private String blackListURL;
        private String whiteListURL;
        private String cookieName;
        private String cookieDomain;
        private String cookiePath;
        private String discardNonSerializable;


        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public String getBlackListURL() {
            return blackListURL;
        }

        public void setBlackListURL(String blackListURL) {
            this.blackListURL = blackListURL;
        }

        public String getWhiteListURL() {
            return whiteListURL;
        }

        public void setWhiteListURL(String whiteListURL) {
            this.whiteListURL = whiteListURL;
        }

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public String getCookieDomain() {
            return cookieDomain;
        }

        public void setCookieDomain(String cookieDomain) {
            this.cookieDomain = cookieDomain;
        }

        public String getCookiePath() {
            return cookiePath;
        }

        public void setCookiePath(String cookiePath) {
            this.cookiePath = cookiePath;
        }

        public String isDiscardNonSerializable() {
            return discardNonSerializable;
        }

        public void setDiscardNonSerializable(String discardNonSerializable) {
            this.discardNonSerializable = discardNonSerializable;
        }

    }

    public static class Redis {
        /**
         * Enable SessionFilter.
         */
        private String enabled = "true";
        private String mode;
        private String hosts;
        private String channel;
        private String cluster_name;
        private String timeout;
        private String password;
        private String database;
        private String maxTotal;
        private String maxIdle;
        private String minIdle;


        public String isEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getHosts() {
            return hosts;
        }

        public void setHosts(String hosts) {
            this.hosts = hosts;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getCluster_name() {
            return cluster_name;
        }

        public void setCluster_name(String cluster_name) {
            this.cluster_name = cluster_name;
        }

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(String maxTotal) {
            this.maxTotal = maxTotal;
        }

        public String getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(String maxIdle) {
            this.maxIdle = maxIdle;
        }

        public String getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(String minIdle) {
            this.minIdle = minIdle;
        }
    }
}
