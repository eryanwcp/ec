package com.eryansky.fastweixin.api.response;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class QrcodeResponse extends BaseResponse {

    private String  ticket;
    @JSONField(name = "expire_seconds")
    private Integer expireSeconds;
    private String  url;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Integer getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Integer expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
