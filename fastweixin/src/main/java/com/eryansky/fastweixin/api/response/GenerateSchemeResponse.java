package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序Scheme生成响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GenerateSchemeResponse extends BaseResponse {

    /**
     * 生成的小程序 Scheme
     */
    @JsonProperty("openlink")
    private String openLink;

    public String getOpenLink() {
        return openLink;
    }

    public void setOpenLink(String openLink) {
        this.openLink = openLink;
    }
}
