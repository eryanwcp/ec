package com.eryansky.fastweixin.company.api.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.eryansky.fastweixin.api.response.BaseResponse;

/**
 * userid与openid互换
 *
 * @author Eryan
 * @date 2019-09-08
 */
public class GetUserConvertToOpenidResponse extends BaseResponse {


    @JSONField(name = "openid")
    private String openid;

    public GetUserConvertToOpenidResponse() {
    }

    public GetUserConvertToOpenidResponse(String openid) {
        this.openid = openid;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
