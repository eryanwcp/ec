package com.eryansky.fastweixin.company.api.response;

import com.eryansky.fastweixin.api.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response -- 从Oauth中获取的用户信息
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class GetOauthUserInfoResponse extends BaseResponse {

    @JsonProperty("UserId")
    private String userid;
    @JsonProperty("OpenId")
    private String openid;
    @JsonProperty("DeviceId")
    private String deviceid;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }
}
