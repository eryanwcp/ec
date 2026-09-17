package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序Short Link生成响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GenerateShortLinkResponse extends BaseResponse {

    /**
     * 生成的小程序 Short Link
     */
    @JsonProperty("link")
    private String link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
