package com.eryansky.fastweixin.api.response;

import com.eryansky.fastweixin.api.entity.DraftArticle;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 草稿箱相关响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class DraftResponse extends BaseResponse {

    /**
     * 草稿的media_id（新建/修改返回）
     */
    @JsonProperty("media_id")
    private String mediaId;

    /**
     * 草稿总数
     */
    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * 草稿列表中的item数量
     */
    @JsonProperty("item_count")
    private Integer itemCount;

    /**
     * 草稿列表
     */
    private List<DraftItem> item;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public List<DraftItem> getItem() {
        return item;
    }

    public void setItem(List<DraftItem> item) {
        this.item = item;
    }

    /**
     * 草稿列表项
     */
    public static class DraftItem {

        @JsonProperty("media_id")
        private String mediaId;

        private DraftContent content;

        @JsonProperty("update_time")
        private Long updateTime;

        public String getMediaId() {
            return mediaId;
        }

        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }

        public DraftContent getContent() {
            return content;
        }

        public void setContent(DraftContent content) {
            this.content = content;
        }

        public Long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }
    }

    /**
     * 草稿内容包装
     */
    public static class DraftContent {

        @JsonProperty("news_item")
        private List<DraftArticle> newsItem;

        public List<DraftArticle> getNewsItem() {
            return newsItem;
        }

        public void setNewsItem(List<DraftArticle> newsItem) {
            this.newsItem = newsItem;
        }
    }
}
