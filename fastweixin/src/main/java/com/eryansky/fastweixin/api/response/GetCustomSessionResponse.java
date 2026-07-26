package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 客户会话状态
 * @author Eryan
 * @date 2018-08-06
 */
public class GetCustomSessionResponse extends BaseResponse {

    /**
     * 正在接待的客服，为空表示没有人在接待
     */
    @JsonProperty("kf_account")
    private String accountName;

    /**
     * 会话接入的时间
     */
    @JsonProperty("createtime")
    private Long createtime;

    public GetCustomSessionResponse() {
    }

    public GetCustomSessionResponse(String accountName, Long createtime) {
        this.accountName = accountName;
        this.createtime = createtime;
    }

    public String getAccountName() {
        return accountName;
    }

    public GetCustomSessionResponse setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    public Long getCreatetime() {
        return createtime;
    }

    public GetCustomSessionResponse setCreatetime(Long createtime) {
        this.createtime = createtime;
        return this;
    }
}
