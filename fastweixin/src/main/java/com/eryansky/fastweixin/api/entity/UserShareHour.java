package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class UserShareHour extends BaseDataCube {

    @JsonProperty("ref_hour")
    private Integer refHour;
    @JsonProperty("share_scene")
    private Integer shareScene;
    @JsonProperty("share_count")
    private Integer shareCount;
    @JsonProperty("share_user")
    private Integer shareUser;

    public Integer getRefHour() {
        return refHour;
    }

    public UserShareHour setRefHour(Integer refHour) {
        this.refHour = refHour;
        return this;
    }

    public Integer getShareScene() {
        return shareScene;
    }

    public UserShareHour setShareScene(Integer shareScene) {
        this.shareScene = shareScene;
        return this;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public UserShareHour setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
        return this;
    }

    public Integer getShareUser() {
        return shareUser;
    }

    public UserShareHour setShareUser(Integer shareUser) {
        this.shareUser = shareUser;
        return this;
    }
}
