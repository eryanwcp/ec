package com.eryansky.fastweixin.company.api.response;

import com.eryansky.fastweixin.api.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.eryansky.fastweixin.company.api.entity.QYTag;

import java.util.List;

/**
 *  Response -- 获取标签列表
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class GetTagListResponse extends BaseResponse {

    @JsonProperty("taglist")
    private List<QYTag> tags;

    public List<QYTag> getTags() {
        return tags;
    }

    public void setTags(List<QYTag> tags) {
        this.tags = tags;
    }
}
