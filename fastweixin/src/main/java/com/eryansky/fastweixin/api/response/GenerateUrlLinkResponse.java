package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序URL Link生成响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GenerateUrlLinkResponse extends BaseResponse {

    /**
     * 生成的小程序 URL Link
     */
    @JsonProperty("url_link")
    private String urlLink;

    public String getUrlLink() {
        return urlLink;
    }

    public void setUrlLink(String urlLink) {
        this.urlLink = urlLink;
    }
}
