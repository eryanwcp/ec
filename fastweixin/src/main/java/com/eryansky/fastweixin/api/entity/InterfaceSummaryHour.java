package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class InterfaceSummaryHour extends InterfaceSummary {

    @JsonProperty("ref_hour")
    private Integer refHour;

    public Integer getRefHour() {
        return refHour;
    }

    public InterfaceSummaryHour setRefHour(Integer refHour) {
        this.refHour = refHour;
        return this;
    }
}
