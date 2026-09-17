package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 小程序直播回放响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GetLiveReplayResponse extends BaseResponse {

    private Integer total;

    @JsonProperty("live_replay")
    private List<LiveReplay> liveReplay;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<LiveReplay> getLiveReplay() {
        return liveReplay;
    }

    public void setLiveReplay(List<LiveReplay> liveReplay) {
        this.liveReplay = liveReplay;
    }

    /**
     * 直播回放信息
     */
    public static class LiveReplay {
        /**
         * 直播回放链接
         */
        @JsonProperty("expire_time")
        private Long expireTime;

        /**
         * 回放视频url
         */
        @JsonProperty("create_time")
        private Long createTime;

        /**
         * 回放视频media url
         */
        @JsonProperty("media_url")
        private String mediaUrl;

        public Long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(Long expireTime) {
            this.expireTime = expireTime;
        }

        public Long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Long createTime) {
            this.createTime = createTime;
        }

        public String getMediaUrl() {
            return mediaUrl;
        }

        public void setMediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
        }
    }
}
