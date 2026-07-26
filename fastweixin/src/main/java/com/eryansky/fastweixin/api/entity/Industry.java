package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 设置行业参数
 */
public class Industry extends BaseModel {
    /**
     * 行业1
     */
    @JsonProperty("industry_id1")
    private String industryId1;
    /**
     * 行业2
     */
    @JsonProperty("industry_id2")
    private String industryId2;

    public String getIndustryId1() {
        return industryId1;
    }

    public Industry setIndustryId1(String industryId1) {
        this.industryId1 = industryId1;
        return this;
    }

    public String getIndustryId2() {
        return industryId2;
    }

    public Industry setIndustryId2(String industryId2) {
        this.industryId2 = industryId2;
        return this;
    }
}
