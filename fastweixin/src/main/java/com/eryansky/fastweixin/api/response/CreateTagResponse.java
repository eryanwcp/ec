package com.eryansky.fastweixin.api.response;


import com.eryansky.fastweixin.api.entity.Tag;

/**
 * @author Eryan
 * @date 2016-05-14
 */
public class CreateTagResponse extends BaseResponse {

    private Tag tag;

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}