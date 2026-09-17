package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 小程序直播间信息响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GetLiveInfoResponse extends BaseResponse {

    /**
     * 直播间总数
     */
    private Integer total;

    /**
     * 直播间列表
     */
    @JsonProperty("room_info")
    private List<RoomInfo> roomInfo;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<RoomInfo> getRoomInfo() {
        return roomInfo;
    }

    public void setRoomInfo(List<RoomInfo> roomInfo) {
        this.roomInfo = roomInfo;
    }

    /**
     * 直播间信息
     */
    public static class RoomInfo {
        /**
         * 直播间名称
         */
        private String name;

        /**
         * 直播间ID
         */
        @JsonProperty("roomid")
        private Long roomId;

        /**
         * 直播间背景图链接
         */
        @JsonProperty("cover_img")
        private String coverImg;

        /**
         * 直播间分享图链接
         */
        @JsonProperty("share_img")
        private String shareImg;

        /**
         * 直播间状态
         * 101：直播中，102：未开始，103：已结束，104：禁播，105：暂停，106：异常，107：已过期
         */
        @JsonProperty("live_status")
        private Integer liveStatus;

        /**
         * 直播间开始时间
         */
        @JsonProperty("start_time")
        private Long startTime;

        /**
         * 直播间结束时间
         */
        @JsonProperty("end_time")
        private Long endTime;

        /**
         * 主播名
         */
        @JsonProperty("anchor_name")
        private String anchorName;

        /**
         * 直播间类型 1: 推流 0:手机直播
         */
        @JsonProperty("live_type")
        private Integer liveType;

        /**
         * 是否关闭点赞 0：开启，1：关闭
         */
        @JsonProperty("close_like")
        private Integer closeLike;

        /**
         * 是否关闭货架 0：开启，1：关闭
         */
        @JsonProperty("close_goods")
        private Integer closeGoods;

        /**
         * 是否关闭评论 0：开启，1：关闭
         */
        @JsonProperty("close_comment")
        private Integer closeComment;

        /**
         * 是否在直播间隐藏点赞
         */
        @JsonProperty("close_kf")
        private Integer closeKf;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }
        public String getCoverImg() { return coverImg; }
        public void setCoverImg(String coverImg) { this.coverImg = coverImg; }
        public String getShareImg() { return shareImg; }
        public void setShareImg(String shareImg) { this.shareImg = shareImg; }
        public Integer getLiveStatus() { return liveStatus; }
        public void setLiveStatus(Integer liveStatus) { this.liveStatus = liveStatus; }
        public Long getStartTime() { return startTime; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }
        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }
        public String getAnchorName() { return anchorName; }
        public void setAnchorName(String anchorName) { this.anchorName = anchorName; }
        public Integer getLiveType() { return liveType; }
        public void setLiveType(Integer liveType) { this.liveType = liveType; }
        public Integer getCloseLike() { return closeLike; }
        public void setCloseLike(Integer closeLike) { this.closeLike = closeLike; }
        public Integer getCloseGoods() { return closeGoods; }
        public void setCloseGoods(Integer closeGoods) { this.closeGoods = closeGoods; }
        public Integer getCloseComment() { return closeComment; }
        public void setCloseComment(Integer closeComment) { this.closeComment = closeComment; }
        public Integer getCloseKf() { return closeKf; }
        public void setCloseKf(Integer closeKf) { this.closeKf = closeKf; }
    }
}
