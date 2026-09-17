package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 异步内容安全审核响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class MediaCheckAsyncResponse extends BaseResponse {

    /**
     * 唯一请求标识
     */
    @JsonProperty("trace_id")
    private String traceId;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
