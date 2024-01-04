package com.eryansky.fastweixin.api.enums;

/**
 * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
 */
public enum MiniprogramState {

    /**
     * 开发版
     */
    developer("developer"),

    /**
     * 体验版
     */
    trial("trial"),

    /**
     * 正式版
     */
    formal("formal");

    String value;

    MiniprogramState(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
