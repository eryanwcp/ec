package com.eryansky.fastweixin.api.response;

import com.eryansky.fastweixin.api.entity.PublishArticle;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 发布能力相关响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class PublishResponse extends BaseResponse {

    /**
     * 发布任务的id（发布提交时返回）
     */
    @JsonProperty("publish_id")
    private Long publishId;

    /**
     * 文章id（发布成功后返回）
     */
    @JsonProperty("article_id")
    private String articleId;

    /**
     * 发布状态：
     * 0-成功, 1-发布中, 2-原创失败(结果不会改变), 3-常规失败(结果不会改变), 4-平台审核不通过
     */
    @JsonProperty("publish_status")
    private Integer publishStatus;

    /**
     * 已通过群发的 article_detail
     */
    @JsonProperty("article_detail")
    private ArticleDetail articleDetail;

    /**
     * 当 publish_status 不为0或3时，返回该字段
     */
    @JsonProperty("fail_idx")
    private List<Integer> failIdx;

    public Long getPublishId() {
        return publishId;
    }

    public void setPublishId(Long publishId) {
        this.publishId = publishId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public ArticleDetail getArticleDetail() {
        return articleDetail;
    }

    public void setArticleDetail(ArticleDetail articleDetail) {
        this.articleDetail = articleDetail;
    }

    public List<Integer> getFailIdx() {
        return failIdx;
    }

    public void setFailIdx(List<Integer> failIdx) {
        this.failIdx = failIdx;
    }

    /**
     * 发布成功的文章详情
     */
    public static class ArticleDetail {
        private Integer count;

        private List<PublishArticle> item;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public List<PublishArticle> getItem() {
            return item;
        }

        public void setItem(List<PublishArticle> item) {
            this.item = item;
        }
    }
}
