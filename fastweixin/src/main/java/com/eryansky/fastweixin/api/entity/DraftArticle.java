package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 草稿箱图文素材
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class DraftArticle extends BaseModel {

    /**
     * 标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 图文消息的摘要
     */
    private String digest;

    /**
     * 图文消息的具体内容，支持 HTML 标签
     */
    private String content;

    /**
     * 图文消息的原文网址
     */
    @JsonProperty("content_source_url")
    private String contentSourceUrl;

    /**
     * 图文消息的封面图片素材 media_id
     */
    @JsonProperty("thumb_media_id")
    private String thumbMediaId;

    /**
     * 是否显示封面，0为false，1为true。默认为1
     */
    @JsonProperty("show_cover_pic")
    private Integer showCoverPic;

    /**
     * Uint32 是否打开评论，0不打开(默认)，1打开
     */
    @JsonProperty("need_open_comment")
    private Integer needOpenComment;

    /**
     * Uint32 是否粉丝才可评论，0所有人可评论(默认)，1粉丝才可评论
     */
    @JsonProperty("only_fans_can_comment")
    private Integer onlyFansCanComment;

    /**
     * 草稿的临时链接
     */
    @JsonProperty("temp_url")
    private String tempUrl;

    /**
     * 该图文是否被删除
     */
    @JsonProperty("is_deleted")
    private Integer isDeleted;

    public String getTitle() {
        return title;
    }

    public DraftArticle setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public DraftArticle setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getDigest() {
        return digest;
    }

    public DraftArticle setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public String getContent() {
        return content;
    }

    public DraftArticle setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContentSourceUrl() {
        return contentSourceUrl;
    }

    public DraftArticle setContentSourceUrl(String contentSourceUrl) {
        this.contentSourceUrl = contentSourceUrl;
        return this;
    }

    public String getThumbMediaId() {
        return thumbMediaId;
    }

    public DraftArticle setThumbMediaId(String thumbMediaId) {
        this.thumbMediaId = thumbMediaId;
        return this;
    }

    public Integer getShowCoverPic() {
        return showCoverPic;
    }

    public DraftArticle setShowCoverPic(Integer showCoverPic) {
        this.showCoverPic = showCoverPic;
        return this;
    }

    public Integer getNeedOpenComment() {
        return needOpenComment;
    }

    public DraftArticle setNeedOpenComment(Integer needOpenComment) {
        this.needOpenComment = needOpenComment;
        return this;
    }

    public Integer getOnlyFansCanComment() {
        return onlyFansCanComment;
    }

    public DraftArticle setOnlyFansCanComment(Integer onlyFansCanComment) {
        this.onlyFansCanComment = onlyFansCanComment;
        return this;
    }

    public String getTempUrl() {
        return tempUrl;
    }

    public void setTempUrl(String tempUrl) {
        this.tempUrl = tempUrl;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
