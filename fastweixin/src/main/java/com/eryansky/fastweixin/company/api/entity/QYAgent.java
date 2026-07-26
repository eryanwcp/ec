package com.eryansky.fastweixin.company.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.eryansky.fastweixin.api.entity.BaseModel;

import java.util.Map;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYAgent extends BaseModel {

    @JsonProperty("agentid")
    private String              agentId;
    @JsonProperty("name")
    private String              name;
    @JsonProperty("square_logo_url")
    private String              squareLogoUrl;
    @JsonProperty("round_logo_url")
    private String              roundLogoUrl;
    @JsonProperty("description")
    private String              description;
    @JsonProperty("allow_userinfos")
    private Map<String, Object> allowUserInfos;
    @JsonProperty("allow_partys")
    private Map<String, Object> allowPartys;
    @JsonProperty("allow_tags")
    private Map<String, Object> allowTags;
    @JsonProperty("close")
    private Integer             close;
    @JsonProperty("redirect_domain")
    private String              redirectDomain;
    @JsonProperty("report_location_flag")
    private Integer             reportLocationFlag;
    @JsonProperty("isreportuser")
    private Integer             isReportUser;
    @JsonProperty("isreportenter")
    private Integer             isReportEnter;

    public QYAgent() {
    }

    /**
     * 创建新应用时候需要用到的参数
     *
     * @param agentId            ID
     * @param name               名称
     * @param description        应用详情
     * @param redirectDomain     应用可信域名
     * @param reportLocationFlag 企业应用是否打开地理位置上报 0：不上报；1：进入会话上报；2：持续上报
     * @param isReportUser       是否接收用户变更通知。0：不接收；1：接收
     * @param isReportEnter      是否上报用户进入应用事件。0：不接收；1：接收
     */
    public QYAgent(String agentId, String name, String description, String redirectDomain, Integer reportLocationFlag, Integer isReportUser, Integer isReportEnter) {
        this.agentId = agentId;
        this.name = name;
        this.description = description;
        this.redirectDomain = redirectDomain;
        this.reportLocationFlag = reportLocationFlag;
        this.isReportEnter = isReportEnter;
        this.isReportUser = isReportUser;
    }

    public QYAgent(String agentId, String name, String squareLogoUrl, String roundLogoUrl, String description, Map<String, Object> allowUserInfos, Map<String, Object> allowPartys, Map<String, Object> allowTags, Integer close, String redirectDomain, Integer reportLocationFlag, Integer isReportUser, Integer isReportEnter) {
        this(agentId, name, description, redirectDomain, reportLocationFlag, isReportEnter, isReportUser);
        this.squareLogoUrl = squareLogoUrl;
        this.roundLogoUrl = roundLogoUrl;
        this.allowUserInfos = allowUserInfos;
        this.allowPartys = allowPartys;
        this.allowTags = allowTags;
        this.close = close;
    }

    public String getAgentId() {
        return agentId;
    }

    public QYAgent setAgentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public String getName() {
        return name;
    }

    public QYAgent setName(String name) {
        this.name = name;
        return this;
    }

    public String getSquareLogoUrl() {
        return squareLogoUrl;
    }

    public QYAgent setSquareLogoUrl(String squareLogoUrl) {
        this.squareLogoUrl = squareLogoUrl;
        return this;
    }

    public String getRoundLogoUrl() {
        return roundLogoUrl;
    }

    public QYAgent setRoundLogoUrl(String roundLogoUrl) {
        this.roundLogoUrl = roundLogoUrl;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public QYAgent setDescription(String description) {
        this.description = description;
        return this;
    }

    public Map<String, Object> getAllowUserInfos() {
        return allowUserInfos;
    }

    public QYAgent setAllowUserInfos(Map<String, Object> allowUserInfos) {
        this.allowUserInfos = allowUserInfos;
        return this;
    }

    public Map<String, Object> getAllowPartys() {
        return allowPartys;
    }

    public QYAgent setAllowPartys(Map<String, Object> allowPartys) {
        this.allowPartys = allowPartys;
        return this;
    }

    public Map<String, Object> getAllowTags() {
        return allowTags;
    }

    public QYAgent setAllowTags(Map<String, Object> allowTags) {
        this.allowTags = allowTags;
        return this;
    }

    public Integer getClose() {
        return close;
    }

    public QYAgent setClose(Integer close) {
        this.close = close;
        return this;
    }

    public String getRedirectDomain() {
        return redirectDomain;
    }

    public QYAgent setRedirectDomain(String redirectDomain) {
        this.redirectDomain = redirectDomain;
        return this;
    }

    public Integer getReportLocationFlag() {
        return reportLocationFlag;
    }

    public QYAgent setReportLocationFlag(Integer reportLocationFlag) {
        this.reportLocationFlag = reportLocationFlag;
        return this;
    }

    public Integer getIsReportUser() {
        return isReportUser;
    }

    public QYAgent setIsReportUser(Integer isReportUser) {
        this.isReportUser = isReportUser;
        return this;
    }

    public Integer getIsReportEnter() {
        return isReportEnter;
    }

    public QYAgent setIsReportEnter(Integer isReportEnter) {
        this.isReportEnter = isReportEnter;
        return this;
    }

    @Override
    public String toString() {
        return "QYAgent{" +
                "agentId='" + agentId + '\'' +
                ", name='" + name + '\'' +
                ", squareLogoUrl='" + squareLogoUrl + '\'' +
                ", roundLogoUrl='" + roundLogoUrl + '\'' +
                ", description='" + description + '\'' +
                ", allowUserInfos=" + allowUserInfos +
                ", allowPartys=" + allowPartys +
                ", allowTags=" + allowTags +
                ", close=" + close +
                ", redirectDomain='" + redirectDomain + '\'' +
                ", reportLocationFlag=" + reportLocationFlag +
                ", isReportUser=" + isReportUser +
                ", isReportEnter=" + isReportEnter +
                '}';
    }
}
