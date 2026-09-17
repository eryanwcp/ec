package com.eryansky.fastweixin.company.api.response;

import com.eryansky.fastweixin.api.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 获取企业微信IP段响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GetQYIPResponse extends BaseResponse {

    /**
     * IP列表
     */
    @JsonProperty("ip_list")
    private List<String> ipList;

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }
}
