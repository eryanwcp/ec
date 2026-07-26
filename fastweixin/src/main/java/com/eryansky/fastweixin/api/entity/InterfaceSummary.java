package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class InterfaceSummary extends BaseDataCube {

    @JsonProperty("callback_count")
    private Integer callbackCount;
    @JsonProperty("fail_count")
    private Integer failCount;
    @JsonProperty("total_time_cost")
    private Integer totalTimeCost;
    @JsonProperty("max_time_cost")
    private Integer maxTimeCost;

    public Integer getCallbackCount() {
        return callbackCount;
    }

    public InterfaceSummary setCallbackCount(Integer callbackCount) {
        this.callbackCount = callbackCount;
        return this;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public InterfaceSummary setFailCount(Integer failCount) {
        this.failCount = failCount;
        return this;
    }

    public Integer getTotalTimeCost() {
        return totalTimeCost;
    }

    public InterfaceSummary setTotalTimeCost(Integer totalTimeCost) {
        this.totalTimeCost = totalTimeCost;
        return this;
    }

    public Integer getMaxTimeCost() {
        return maxTimeCost;
    }

    public InterfaceSummary setMaxTimeCost(Integer maxTimeCost) {
        this.maxTimeCost = maxTimeCost;
        return this;
    }
}
