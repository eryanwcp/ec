package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 发送模版消息响应
 */
public class SendTemplateResponse extends BaseResponse {

    /**
     * 消息id
     */
    @JsonProperty("msgid")
    private String msgid;

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }
}
