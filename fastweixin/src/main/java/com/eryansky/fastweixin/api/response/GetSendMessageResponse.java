package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  获取群发消息结果
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class GetSendMessageResponse extends BaseResponse {

    @JsonProperty("msg_id")
    private String msgId;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
