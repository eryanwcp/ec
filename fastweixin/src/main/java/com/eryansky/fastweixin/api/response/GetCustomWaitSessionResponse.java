package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * 未接入会话信息
 *
 * @author Eryan
 * @date 2018-08-06
 */
public class GetCustomWaitSessionResponse extends BaseResponse {

    /**
     * 未接入会话数量
     */
    @JsonProperty("count")
    private Integer count;

    /**
     * 未接入会话列表，最多返回100条数据，按照来访顺序
     */
    @JsonProperty("waitcaselist")
    List<WaitSessionInfo> waitcaselist;

    public GetCustomWaitSessionResponse() {
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<WaitSessionInfo> getWaitcaselist() {
        return waitcaselist;
    }

    public void setWaitcaselist(List<WaitSessionInfo> waitcaselist) {
        this.waitcaselist = waitcaselist;
    }

    static class WaitSessionInfo {
        /**
         * 粉丝的openid
         */
        @JsonProperty("openid")
        private String openid;
        @JsonProperty("openid")
        private Long latestTime;

        public WaitSessionInfo() {
        }

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public Long getLatestTime() {
            return latestTime;
        }

        public void setLatestTime(Long latestTime) {
            this.latestTime = latestTime;
        }
    }
}
