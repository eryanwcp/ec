package com.eryansky.fastweixin.company.api.response;

import com.eryansky.fastweixin.api.response.BaseResponse;

/**
 *  Response -- 创建新标签
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class CreateTagResponse extends BaseResponse {

    private Integer tagid;

    public Integer getTagid() {
        return tagid;
    }

    public void setTagid(Integer tagid) {
        this.tagid = tagid;
    }
}
