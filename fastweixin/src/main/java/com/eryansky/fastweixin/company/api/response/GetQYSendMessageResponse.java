package com.eryansky.fastweixin.company.api.response;/**
 * Created by Nottyjay on 2015/6/12.
 */

import com.alibaba.fastjson.annotation.JSONField;
import com.eryansky.fastweixin.api.response.BaseResponse;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class GetQYSendMessageResponse extends BaseResponse {

    @JSONField(name = "invaliduser")
    private String invalidUser;
    @JSONField(name = "invalidParty")
    private String invalidParty;
    @JSONField(name = "invalidtag")
    private String invalidTag;
    @JSONField(name = "msgid")
    private String msgid;
    @JSONField(name = "response_code")
    private String responseCode;

    public String getInvalidUser() {
        return invalidUser;
    }

    public void setInvalidUser(String invalidUser) {
        this.invalidUser = invalidUser;
    }

    public String getInvalidParty() {
        return invalidParty;
    }

    public void setInvalidParty(String invalidParty) {
        this.invalidParty = invalidParty;
    }

    public String getInvalidTag() {
        return invalidTag;
    }

    public void setInvalidTag(String invalidTag) {
        this.invalidTag = invalidTag;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
}
