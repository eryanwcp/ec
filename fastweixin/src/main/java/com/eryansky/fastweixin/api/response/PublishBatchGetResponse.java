package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 发布列表批量获取响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class PublishBatchGetResponse extends BaseResponse {

    /**
     * 成功发布总数
     */
    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * 发布列表
     */
    private List<PublishItem> item;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<PublishItem> getItem() {
        return item;
    }

    public void setItem(List<PublishItem> item) {
        this.item = item;
    }

    /**
     * 发布列表项
     */
    public static class PublishItem {

        @JsonProperty("article_id")
        private String articleId;

        private PublishResponse.ArticleDetail content;

        @JsonProperty("update_time")
        private Long updateTime;

        public String getArticleId() {
            return articleId;
        }

        public void setArticleId(String articleId) {
            this.articleId = articleId;
        }

        public PublishResponse.ArticleDetail getContent() {
            return content;
        }

        public void setContent(PublishResponse.ArticleDetail content) {
            this.content = content;
        }

        public Long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }
    }
}
