package com.eryansky.fastweixin.api.enums;

/**
 * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
 */
public enum LangType {

    /**
     * 简体中文
     */
    zh_CN("zh_CN"),

    /**
     * 英文
     */
    en_US("en_US"),

    /**
     * zh_TW
     */
    zh_TW("zh_TW");

    String value;

    LangType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
