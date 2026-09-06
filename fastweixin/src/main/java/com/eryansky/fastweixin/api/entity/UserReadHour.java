package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class UserReadHour extends BaseDataCube {

    @JsonProperty("ref_hour")
    private Integer refHour;
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

    public Integer getRefHour() {
        return refHour;
    }

    public UserReadHour setRefHour(Integer refHour) {
        this.refHour = refHour;
        return this;
    }

    public Integer getIntPageReadUser() {
        return intPageReadUser;
    }

    public UserReadHour setIntPageReadUser(Integer intPageReadUser) {
        this.intPageReadUser = intPageReadUser;
        return this;
    }

    public Integer getIntPageReadCount() {
        return intPageReadCount;
    }

    public UserReadHour setIntPageReadCount(Integer intPageReadCount) {
        this.intPageReadCount = intPageReadCount;
        return this;
    }

    public Integer getOriPageReadUser() {
        return oriPageReadUser;
    }

    public UserReadHour setOriPageReadUser(Integer oriPageReadUser) {
        this.oriPageReadUser = oriPageReadUser;
        return this;
    }

    public Integer getOriPageReadCount() {
        return oriPageReadCount;
    }

    public UserReadHour setOriPageReadCount(Integer oriPageReadCount) {
        this.oriPageReadCount = oriPageReadCount;
        return this;
    }

    public Integer getShareUser() {
        return shareUser;
    }

    public UserReadHour setShareUser(Integer shareUser) {
        this.shareUser = shareUser;
        return this;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public UserReadHour setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
        return this;
    }

    public Integer getAddToFavUser() {
        return addToFavUser;
    }

    public UserReadHour setAddToFavUser(Integer addToFavUser) {
        this.addToFavUser = addToFavUser;
        return this;
    }

    public Integer getAddToFavCount() {
        return addToFavCount;
    }

    public UserReadHour setAddToFavCount(Integer addToFavCount) {
        this.addToFavCount = addToFavCount;
        return this;
    }
}
