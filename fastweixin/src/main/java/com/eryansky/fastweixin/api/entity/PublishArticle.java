package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 已发布文章信息
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class PublishArticle extends BaseModel {

    /**
     * 文章id
     */
    @JsonProperty("article_id")
    private String articleId;

    /**
     * 图文消息的具体内容，支持 HTML 标签
     */
    private String content;

    /**
     * 草稿的临时链接
     */
    @JsonProperty("article_url")
    private String articleUrl;

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
     * 图文消息的封面图片素材 media_id
     */
    @JsonProperty("thumb_media_id")
    private String thumbMediaId;

    /**
     * 封面图片的URL
     */
    @JsonProperty("thumb_url")
    private String thumbUrl;

    /**
     * 是否显示封面，0为false，1为true。
     */
    @JsonProperty("show_cover_pic")
    private Integer showCoverPic;

    /**
     * 图文消息的原文网址
     */
    @JsonProperty("content_source_url")
    private String contentSourceUrl;

    /**
     * Uint32 是否打开评论
     */
    @JsonProperty("need_open_comment")
    private Integer needOpenComment;

    /**
     * Uint32 是否粉丝才可评论
     */
    @JsonProperty("only_fans_can_comment")
    private Integer onlyFansCanComment;

    public String getArticleId() {
        return articleId;
    }

    public PublishArticle setArticleId(String articleId) {
        this.articleId = articleId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PublishArticle setContent(String content) {
        this.content = content;
        return this;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }

    public String getTitle() {
        return title;
    }

    public PublishArticle setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public PublishArticle setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getDigest() {
        return digest;
    }

    public PublishArticle setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public String getThumbMediaId() {
        return thumbMediaId;
    }

    public PublishArticle setThumbMediaId(String thumbMediaId) {
        this.thumbMediaId = thumbMediaId;
        return this;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public Integer getShowCoverPic() {
        return showCoverPic;
    }

    public PublishArticle setShowCoverPic(Integer showCoverPic) {
        this.showCoverPic = showCoverPic;
        return this;
    }

    public String getContentSourceUrl() {
        return contentSourceUrl;
    }

    public PublishArticle setContentSourceUrl(String contentSourceUrl) {
        this.contentSourceUrl = contentSourceUrl;
        return this;
    }

    public Integer getNeedOpenComment() {
        return needOpenComment;
    }

    public PublishArticle setNeedOpenComment(Integer needOpenComment) {
        this.needOpenComment = needOpenComment;
        return this;
    }

    public Integer getOnlyFansCanComment() {
        return onlyFansCanComment;
    }

    public PublishArticle setOnlyFansCanComment(Integer onlyFansCanComment) {
        this.onlyFansCanComment = onlyFansCanComment;
        return this;
    }
}
