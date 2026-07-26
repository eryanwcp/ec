package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息分送分时数据
 * @author Eryan
 * @date 2016-03-15
 */
public class UpstreamMsgHour extends BaseDataCube {

    @JsonProperty("ref_hour")
    private Integer refHour;
    @JsonProperty("msg_type")
    private Integer msgType;
    @JsonProperty("msg_user")
    private Integer msgUser;
    @JsonProperty("msg_count")
    private Integer msgCount;

    public Integer getRefHour() {
        return refHour;
    }

    public UpstreamMsgHour setRefHour(Integer refHour) {
        this.refHour = refHour;
        return this;
    }

    public Integer getMsgType() {
        return msgType;
    }

    public UpstreamMsgHour setMsgType(Integer msgType) {
        this.msgType = msgType;
        return this;
    }

    public Integer getMsgUser() {
        return msgUser;
    }

    public UpstreamMsgHour setMsgUser(Integer msgUser) {
        this.msgUser = msgUser;
        return this;
    }

    public Integer getMsgCount() {
        return msgCount;
    }

    public UpstreamMsgHour setMsgCount(Integer msgCount) {
        this.msgCount = msgCount;
        return this;
    }
}
