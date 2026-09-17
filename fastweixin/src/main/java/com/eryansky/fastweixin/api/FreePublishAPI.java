package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.enums.ResultType;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.PublishBatchGetResponse;
import com.eryansky.fastweixin.api.response.PublishResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 发布能力 API
 * 已发布文章管理，包括发布、查询发布状态、删除发布、获取已发布文章列表
 *
 * 接口文档: https://developers.weixin.qq.com/doc/offiaccount/Publish/Publish.html
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class FreePublishAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(FreePublishAPI.class);

    public FreePublishAPI(ApiConfig config) {
        super(config);
    }

    /**
     * 发布接口
     * 发布已经添加到草稿箱的图文消息
     * 接口地址: POST /cgi-bin/freepublish/submit
     *
     * @param mediaId 要发布的草稿的media_id
     * @return 包含publish_id的响应
     */
    public PublishResponse submit(String mediaId) {
        BeanUtil.requireNonNull(mediaId, "mediaId is null");
        LOG.debug("发布图文......");
        String url = BASE_API_URL + "cgi-bin/freepublish/submit?access_token=#";
        Map<String, String> params = new HashMap<>();
        params.put("media_id", mediaId);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, PublishResponse.class);
    }

    /**
     * 查询发布状态
     * 接口地址: POST /cgi-bin/freepublish/get
     *
     * @param publishId 发布任务id
     * @return 发布状态
     */
    public PublishResponse getPublishStatus(Long publishId) {
        BeanUtil.requireNonNull(publishId, "publishId is null");
        LOG.debug("查询发布状态......");
        String url = BASE_API_URL + "cgi-bin/freepublish/get?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("publish_id", publishId);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, PublishResponse.class);
    }

    /**
     * 删除发布
     * 接口地址: POST /cgi-bin/freepublish/delete
     *
     * @param articleId 成功发布时返回的 article_id
     * @param index     要删除的文章在图文消息中的位置，第一篇为0。不填或填0会删除所有文章
     * @return 操作结果
     */
    public ResultType delete(String articleId, Integer index) {
        BeanUtil.requireNonNull(articleId, "articleId is null");
        LOG.debug("删除发布......");
        String url = BASE_API_URL + "cgi-bin/freepublish/delete?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("article_id", articleId);
        if (index != null) {
            params.put("index", index);
        }
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 删除发布（删除所有文章）
     */
    public ResultType delete(String articleId) {
        return delete(articleId, null);
    }

    /**
     * 通过 article_id 获取已发布文章
     * 接口地址: POST /cgi-bin/freepublish/getarticle
     *
     * @param articleId 要获取的草稿的article_id
     * @return 文章内容详情
     */
    public PublishResponse getArticle(String articleId) {
        BeanUtil.requireNonNull(articleId, "articleId is null");
        LOG.debug("获取已发布文章......");
        String url = BASE_API_URL + "cgi-bin/freepublish/getarticle?access_token=#";
        Map<String, String> params = new HashMap<>();
        params.put("article_id", articleId);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, PublishResponse.class);
    }

    /**
     * 获取成功发布列表
     * 接口地址: POST /cgi-bin/freepublish/batchget
     *
     * @param offset    从全部素材的该偏移位置开始返回，0表示从第一个素材返回
     * @param count     返回素材的数量，取值在1到20之间
     * @param noContent 1 表示不返回 content 字段，0 表示正常返回，默认为 0
     * @return 发布列表
     */
    public PublishBatchGetResponse batchGet(int offset, int count, Integer noContent) {
        LOG.debug("获取发布列表......");
        String url = BASE_API_URL + "cgi-bin/freepublish/batchget?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("count", count);
        if (noContent != null) {
            params.put("no_content", noContent);
        }
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, PublishBatchGetResponse.class);
    }

    /**
     * 获取成功发布列表（默认返回content）
     */
    public PublishBatchGetResponse batchGet(int offset, int count) {
        return batchGet(offset, count, null);
    }
}
