package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.enums.ResultType;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.CommentListResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论管理 API
 * 公众号文章评论管理，包括打开/关闭评论、查看/精选/删除评论、回复评论
 *
 * 接口文档: https://developers.weixin.qq.com/doc/offiaccount/Comments_management/Image_Comments_management_Interface.html
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class CommentAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(CommentAPI.class);

    public CommentAPI(ApiConfig config) {
        super(config);
    }

    /**
     * 打开文章评论
     * 接口地址: POST /cgi-bin/comment/open
     *
     * @param msgDataId 群发返回的msg_data_id
     * @param index     多图文时，用来指定第几篇图文，从0开始，默认0
     * @return 操作结果
     */
    public ResultType open(String msgDataId, Integer index) {
        BeanUtil.requireNonNull(msgDataId, "msgDataId is null");
        LOG.debug("打开文章评论......");
        String url = BASE_API_URL + "cgi-bin/comment/open?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("msg_data_id", msgDataId);
        if (index != null) {
            params.put("index", index);
        }
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 关闭文章评论
     * 接口地址: POST /cgi-bin/comment/close
     *
     * @param msgDataId 群发返回的msg_data_id
     * @param index     多图文时，用来指定第几篇图文，从0开始，默认0
     * @return 操作结果
     */
    public ResultType close(String msgDataId, Integer index) {
        BeanUtil.requireNonNull(msgDataId, "msgDataId is null");
        LOG.debug("关闭文章评论......");
        String url = BASE_API_URL + "cgi-bin/comment/close?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("msg_data_id", msgDataId);
        if (index != null) {
            params.put("index", index);
        }
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 查看指定文章的评论数据
     * 接口地址: POST /cgi-bin/comment/list
     *
     * @param msgDataId 群发返回的msg_data_id
     * @param index     多图文时，用来指定第几篇图文，从0开始
     * @param begin     起始位置
     * @param count     获取数目（<=50）
     * @param type      0-普通评论, 1-精选评论
     * @return 评论列表
     */
    public CommentListResponse list(String msgDataId, Integer index, int begin, int count, int type) {
        BeanUtil.requireNonNull(msgDataId, "msgDataId is null");
        LOG.debug("查看文章评论......");
        String url = BASE_API_URL + "cgi-bin/comment/list?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("msg_data_id", msgDataId);
        if (index != null) {
            params.put("index", index);
        }
        params.put("begin", begin);
        params.put("count", count);
        params.put("type", type);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, CommentListResponse.class);
    }

    /**
     * 将评论标记精选
     * 接口地址: POST /cgi-bin/comment/markelect
     *
     * @param msgDataId     群发返回的msg_data_id
     * @param index         多图文时，用来指定第几篇图文，从0开始
     * @param userCommentId 用户评论id
     * @return 操作结果
     */
    public ResultType markElect(String msgDataId, Integer index, Long userCommentId) {
        BeanUtil.requireNonNull(msgDataId, "msgDataId is null");
        BeanUtil.requireNonNull(userCommentId, "userCommentId is null");
        LOG.debug("标记精选评论......");
        String url = BASE_API_URL + "cgi-bin/comment/markelect?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("msg_data_id", msgDataId);
        if (index != null) {
            params.put("index", index);
        }
        params.put("user_comment_id", userCommentId);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 将评论取消精选
     * 接口地址: POST /cgi-bin/comment/unmarkelect
     *
     * @param msgDataId     群发返回的msg_data_id
     * @param index         多图文时，用来指定第几篇图文，从0开始
     * @param userCommentId 用户评论id
     * @return 操作结果
     */
    public ResultType unmarkElect(String msgDataId, Integer index, Long userCommentId) {
        BeanUtil.requireNonNull(msgDataId, "msgDataId is null");
        BeanUtil.requireNonNull(userCommentId, "userCommentId is null");
        LOG.debug("取消精选评论......");
        String url = BASE_API_URL + "cgi-bin/comment/unmarkelect?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("msg_data_id", msgDataId);
        if (index != null) {
            params.put("index", index);
        }
        params.put("user_comment_id", userCommentId);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 删除评论
     * 接口地址: POST /cgi-bin/comment/delete
     *
     * @param msgDataId     群发返回的msg_data_id
     * @param index         多图文时，用来指定第几篇图文，从0开始
     * @param userCommentId 用户评论id
     * @return 操作结果
     */
    public ResultType deleteComment(String msgDataId, Integer index, Long userCommentId) {
        BeanUtil.requireNonNull(msgDataId, "msgDataId is null");
        BeanUtil.requireNonNull(userCommentId, "userCommentId is null");
        LOG.debug("删除评论......");
        String url = BASE_API_URL + "cgi-bin/comment/delete?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("msg_data_id", msgDataId);
        if (index != null) {
            params.put("index", index);
        }
        params.put("user_comment_id", userCommentId);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 回复评论
     * 接口地址: POST /cgi-bin/comment/reply/add
     *
     * @param msgDataId     群发返回的msg_data_id
     * @param index         多图文时，用来指定第几篇图文，从0开始
     * @param userCommentId 用户评论id
     * @param content       回复内容
     * @return 操作结果
     */
    public ResultType replyAdd(String msgDataId, Integer index, Long userCommentId, String content) {
        BeanUtil.requireNonNull(msgDataId, "msgDataId is null");
        BeanUtil.requireNonNull(userCommentId, "userCommentId is null");
        BeanUtil.requireNonNull(content, "content is null");
        LOG.debug("回复评论......");
        String url = BASE_API_URL + "cgi-bin/comment/reply/add?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("msg_data_id", msgDataId);
        if (index != null) {
            params.put("index", index);
        }
        params.put("user_comment_id", userCommentId);
        params.put("content", content);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 删除回复
     * 接口地址: POST /cgi-bin/comment/reply/delete
     *
     * @param msgDataId     群发返回的msg_data_id
     * @param index         多图文时，用来指定第几篇图文，从0开始
     * @param userCommentId 用户评论id
     * @return 操作结果
     */
    public ResultType replyDelete(String msgDataId, Integer index, Long userCommentId) {
        BeanUtil.requireNonNull(msgDataId, "msgDataId is null");
        BeanUtil.requireNonNull(userCommentId, "userCommentId is null");
        LOG.debug("删除评论回复......");
        String url = BASE_API_URL + "cgi-bin/comment/reply/delete?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("msg_data_id", msgDataId);
        if (index != null) {
            params.put("index", index);
        }
        params.put("user_comment_id", userCommentId);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }
}
