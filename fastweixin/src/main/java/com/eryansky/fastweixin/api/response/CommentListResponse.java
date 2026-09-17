package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 评论列表响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class CommentListResponse extends BaseResponse {

    /**
     * 总数，非 real total
     */
    private Integer total;

    /**
     * 评论列表
     */
    private List<Comment> comment;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Comment> getComment() {
        return comment;
    }

    public void setComment(List<Comment> comment) {
        this.comment = comment;
    }

    /**
     * 评论对象
     */
    public static class Comment {
        /**
         * 用户评论id
         */
        @JsonProperty("user_comment_id")
        private Long userCommentId;

        /**
         * 用户openid
         */
        private String openid;

        /**
         * 评论时间
         */
        @JsonProperty("create_time")
        private Long createTime;

        /**
         * 评论内容
         */
        private String content;

        /**
         * 是否精选评论，0为即非精选，1为true，即精选
         */
        @JsonProperty("comment_type")
        private Integer commentType;

        /**
         * 精选评论回复
         */
        private Reply reply;

        public Long getUserCommentId() {
            return userCommentId;
        }

        public void setUserCommentId(Long userCommentId) {
            this.userCommentId = userCommentId;
        }

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public Long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Long createTime) {
            this.createTime = createTime;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Integer getCommentType() {
            return commentType;
        }

        public void setCommentType(Integer commentType) {
            this.commentType = commentType;
        }

        public Reply getReply() {
            return reply;
        }

        public void setReply(Reply reply) {
            this.reply = reply;
        }
    }

    /**
     * 评论回复
     */
    public static class Reply {
        /**
         * 回复内容
         */
        private String content;

        /**
         * 回复时间
         */
        @JsonProperty("create_time")
        private Long createTime;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Long createTime) {
            this.createTime = createTime;
        }
    }
}
