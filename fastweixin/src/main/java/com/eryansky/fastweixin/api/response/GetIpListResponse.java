package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 获取IP列表响应（通用）
 * 适用于:
 * - 获取微信API服务器IP段: /cgi-bin/get_api_domain_ip
 * - 获取微信推送服务器IP段: /cgi-bin/getcallbackip
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GetIpListResponse extends BaseResponse {

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
