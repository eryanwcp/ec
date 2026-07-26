package com.eryansky.fastweixin.api.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.eryansky.fastweixin.api.enums.LangType;
import com.eryansky.fastweixin.api.enums.MiniprogramState;

import java.util.Map;

/**
 * 订阅消息
 */
public class SubcribeMsg extends BaseModel {
    /**
     * 接收者（用户）的 openid
     */
    private String touser;
    /**
     * 订阅模板id
     */
    @JSONField(name = "template_id")
    private String templateId;
    /**
     * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     * {@link  MiniprogramState}
     */
    @JSONField(name = "miniprogram_state")
    private String miniprogramState;
    /**
     * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     * {@link LangType}
     */
    private String lang;
    /**
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
     */
    private String page;

    /**
     * 模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }的object
     */
    private Map<String, SubcribeParam> data;

    public SubcribeMsg() {
        this.lang = LangType.zh_CN.toString();
    }

    public String getTouser() {
        return touser;
    }

    public SubcribeMsg setTouser(String touser) {
        this.touser = touser;
        return this;
    }

    public String getTemplateId() {
        return templateId;
    }

    public SubcribeMsg setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public String getMiniprogramState() {
        return miniprogramState;
    }

    public SubcribeMsg setMiniprogramState(String miniprogramState) {
        this.miniprogramState = miniprogramState;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public SubcribeMsg setLang(String lang) {
        this.lang = lang;
        return this;
    }

    public String getPage() {
        return page;
    }

    public SubcribeMsg setPage(String page) {
        this.page = page;
        return this;
    }

    public Map<String, SubcribeParam> getData() {
        return data;
    }

    public SubcribeMsg setData(Map<String, SubcribeParam> data) {
        this.data = data;
        return this;
    }
}
