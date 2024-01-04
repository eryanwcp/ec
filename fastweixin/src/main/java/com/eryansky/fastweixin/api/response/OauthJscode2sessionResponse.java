package com.eryansky.fastweixin.api.response;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 小程序登录
 * @author Eryan
 * @date 2024-01-04
 */
public class OauthJscode2sessionResponse extends BaseResponse {

    /**
     * 会话密钥
     */
    @JSONField(name = "session_key")
    private String sessionKey;
    /**
     * 用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台账号下会返回，详见 UnionID 机制说明。
     */
    private String unionid;
    /**
     * 用户唯一标识
     */
    private String openid;

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
