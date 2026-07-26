package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class ArticleTotalDetail extends BaseModel {

    @JsonProperty(""stat_date", format = "yyyy-MM-dd")
    private Date    statDate;
    @JsonProperty("target_user")
    private Integer targetUser;
    @JsonProperty("int_page_read_user")
    private Integer intPageReadUser;
    @JsonProperty("int_page_read_count")
    private Integer intPageReadCount;
    @JsonProperty("ori_page_read_user")
    private Integer oriPageReadUser;
    @JsonProperty("ori_page_read_count")
    private Integer oriPageReadCount;
    @JsonProperty("share_user")
    private Integer shareUser;
    @JsonProperty("share_count")
    private Integer shareCount;
    @JsonProperty("add_to_fav_user")
    private Integer addToFavUser;
    @JsonProperty("add_to_fav_count")
    private Integer addToFavCount;

    public Date getStatDate() {
        return statDate;
    }

    public ArticleTotalDetail setStatDate(Date statDate) {
        this.statDate = statDate;
        return this;
    }

    public Integer getTargetUser() {
        return targetUser;
    }

    public ArticleTotalDetail setTargetUser(Integer targetUser) {
        this.targetUser = targetUser;
        return this;
    }

    public Integer getIntPageReadUser() {
        return intPageReadUser;
    }

    public ArticleTotalDetail setIntPageReadUser(Integer intPageReadUser) {
        this.intPageReadUser = intPageReadUser;
        return this;
    }

    public Integer getIntPageReadCount() {
        return intPageReadCount;
    }

    public ArticleTotalDetail setIntPageReadCount(Integer intPageReadCount) {
        this.intPageReadCount = intPageReadCount;
        return this;
    }

    public Integer getOriPageReadUser() {
        return oriPageReadUser;
    }

    public ArticleTotalDetail setOriPageReadUser(Integer oriPageReadUser) {
        this.oriPageReadUser = oriPageReadUser;
        return this;
    }

    public Integer getOriPageReadCount() {
        return oriPageReadCount;
    }

    public ArticleTotalDetail setOriPageReadCount(Integer oriPageReadCount) {
        this.oriPageReadCount = oriPageReadCount;
        return this;
    }

    public Integer getShareUser() {
        return shareUser;
    }

    public ArticleTotalDetail setShareUser(Integer shareUser) {
        this.shareUser = shareUser;
        return this;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public ArticleTotalDetail setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
        return this;
    }

    public Integer getAddToFavUser() {
        return addToFavUser;
    }

    public ArticleTotalDetail setAddToFavUser(Integer addToFavUser) {
        this.addToFavUser = addToFavUser;
        return this;
    }

    public Integer getAddToFavCount() {
        return addToFavCount;
    }

    public ArticleTotalDetail setAddToFavCount(Integer addToFavCount) {
        this.addToFavCount = addToFavCount;
        return this;
    }
}
