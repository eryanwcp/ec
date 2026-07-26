package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class UserShare extends BaseDataCube {

    @JsonProperty("share_scene")
    private Integer shareScene;
    @JsonProperty("share_count")
    private Integer shareCount;
    @JsonProperty("share_user")
    private Integer shareUser;

    public Integer getShareScene() {
        return shareScene;
    }

    public UserShare setShareScene(Integer shareScene) {
        this.shareScene = shareScene;
        return this;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public UserShare setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
        return this;
    }

    public Integer getShareUser() {
        return shareUser;
    }

    public UserShare setShareUser(Integer shareUser) {
        this.shareUser = shareUser;
        return this;
    }
}
