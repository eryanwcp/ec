package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 创建动态消息活动ID响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class CreateActivityIdResponse extends BaseResponse {

    /**
     * 动态消息ID
     */
    @JsonProperty("activity_id")
    private String activityId;

    /**
     * activity_id的过期时间戳（Unix时间），默认有效期24小时
     */
    @JsonProperty("expiration_time")
    private Long expirationTime;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
