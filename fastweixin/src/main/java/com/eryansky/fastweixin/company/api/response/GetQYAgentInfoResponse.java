package com.eryansky.fastweixin.company.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.company.api.entity.QYAgent;

import java.util.Map;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class GetQYAgentInfoResponse extends BaseResponse {

    @JsonProperty("agentid")
    private String agentId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("square_logo_url")
    private String squareLogoUrl;
    @JsonProperty("round_logo_url")
    private String roundLogoUrl;
    @JsonProperty("description")
    private String description;
    @JsonProperty("allow_userinfos")
    private Map<String, Object> allowUserInfos;
    @JsonProperty("allow_partys")
    private Map<String, Object> allowPartys;
    @JsonProperty("allow_tags")
    private Map<String, Object> allowTags;
    @JsonProperty("close")
    private Integer close;
    @JsonProperty("redirect_domain")
    private String redirectDomain;
    @JsonProperty("report_location_flag")
    private Integer reportLocationFlag;
    @JsonProperty("isreportuser")
    private Integer isReportUser;
    @JsonProperty("isreportenter")
    private Integer isReportEnter;


    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSquareLogoUrl() {
        return squareLogoUrl;
    }

    public void setSquareLogoUrl(String squareLogoUrl) {
        this.squareLogoUrl = squareLogoUrl;
    }

    public String getRoundLogoUrl() {
        return roundLogoUrl;
    }

    public void setRoundLogoUrl(String roundLogoUrl) {
        this.roundLogoUrl = roundLogoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getAllowUserInfos() {
        return allowUserInfos;
    }

    public void setAllowUserInfos(Map<String, Object> allowUserInfos) {
        this.allowUserInfos = allowUserInfos;
    }

    public Map<String, Object> getAllowPartys() {
        return allowPartys;
    }

    public void setAllowPartys(Map<String, Object> allowPartys) {
        this.allowPartys = allowPartys;
    }

    public Map<String, Object> getAllowTags() {
        return allowTags;
    }

    public void setAllowTags(Map<String, Object> allowTags) {
        this.allowTags = allowTags;
    }

    public Integer getClose() {
        return close;
    }

    public void setClose(Integer close) {
        this.close = close;
    }

    public String getRedirectDomain() {
        return redirectDomain;
    }

    public void setRedirectDomain(String redirectDomain) {
        this.redirectDomain = redirectDomain;
    }

    public Integer getReportLocationFlag() {
        return reportLocationFlag;
    }

    public void setReportLocationFlag(Integer reportLocationFlag) {
        this.reportLocationFlag = reportLocationFlag;
    }

    public Integer getIsReportUser() {
        return isReportUser;
    }

    public void setIsReportUser(Integer isReportUser) {
        this.isReportUser = isReportUser;
    }

    public Integer getIsReportEnter() {
        return isReportEnter;
    }

    public void setIsReportEnter(Integer isReportEnter) {
        this.isReportEnter = isReportEnter;
    }

    public QYAgent getQyAgent() {
        return new QYAgent(agentId, name, squareLogoUrl, roundLogoUrl, description, allowUserInfos, allowPartys, allowTags, close, redirectDomain, reportLocationFlag, isReportUser, isReportEnter);
    }

}
