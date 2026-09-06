package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class UpstreamMsgDist extends BaseDataCube {

    @JsonProperty("count_interval")
    private Integer countInterval;
    @JsonProperty("msg_user")
    private Integer msgUser;

    public Integer getCountInterval() {
        return countInterval;
    }

    public UpstreamMsgDist setCountInterval(Integer countInterval) {
        this.countInterval = countInterval;
        return this;
    }

    public Integer getMsgUser() {
        return msgUser;
    }

    public UpstreamMsgDist setMsgUser(Integer msgUser) {
        this.msgUser = msgUser;
        return this;
    }
}
