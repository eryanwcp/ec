package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.entity.DraftArticle;
import com.eryansky.fastweixin.api.enums.ResultType;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.DraftResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 草稿箱 API
 * 公众号草稿箱接口，用于管理图文消息草稿
 *
 * 接口文档: https://developers.weixin.qq.com/doc/offiaccount/Draft_Box/Add_draft.html
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class DraftAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(DraftAPI.class);

    public DraftAPI(ApiConfig config) {
        super(config);
    }

    /**
     * 新建草稿
     * 接口地址: POST /cgi-bin/draft/add
     *
     * @param articles 图文消息列表（最多8个）
     * @return 草稿的media_id
     */
    public DraftResponse add(List<DraftArticle> articles) {
        BeanUtil.requireNonNull(articles, "articles is null");
        LOG.debug("新建草稿......");
        String url = BASE_API_URL + "cgi-bin/draft/add?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("articles", articles);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, DraftResponse.class);
    }

    /**
     * 获取草稿
     * 接口地址: POST /cgi-bin/draft/get
     *
     * @param mediaId 草稿的media_id
     * @return 草稿详细信息
     */
    public DraftResponse get(String mediaId) {
        BeanUtil.requireNonNull(mediaId, "mediaId is null");
        LOG.debug("获取草稿......");
        String url = BASE_API_URL + "cgi-bin/draft/get?access_token=#";
        Map<String, String> params = new HashMap<>();
        params.put("media_id", mediaId);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, DraftResponse.class);
    }

    /**
     * 删除草稿
     * 接口地址: POST /cgi-bin/draft/delete
     *
     * @param mediaId 草稿的media_id
     * @return 操作结果
     */
    public ResultType delete(String mediaId) {
        BeanUtil.requireNonNull(mediaId, "mediaId is null");
        LOG.debug("删除草稿......");
        String url = BASE_API_URL + "cgi-bin/draft/delete?access_token=#";
        Map<String, String> params = new HashMap<>();
        params.put("media_id", mediaId);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 修改草稿
     * 接口地址: POST /cgi-bin/draft/update
     *
     * @param mediaId 草稿的media_id
     * @param index   要更新的文章在草稿中的位置，第一篇为0
     * @param article 要更新的文章内容
     * @return 操作结果
     */
    public ResultType update(String mediaId, int index, DraftArticle article) {
        BeanUtil.requireNonNull(mediaId, "mediaId is null");
        BeanUtil.requireNonNull(article, "article is null");
        LOG.debug("修改草稿......");
        String url = BASE_API_URL + "cgi-bin/draft/update?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("media_id", mediaId);
        params.put("index", index);
        params.put("articles", article);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 获取草稿总数
     * 接口地址: GET /cgi-bin/draft/count
     *
     * @return 草稿总数（在DraftResponse.totalCount中）
     */
    public DraftResponse count() {
        LOG.debug("获取草稿总数......");
        String url = BASE_API_URL + "cgi-bin/draft/count?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, DraftResponse.class);
    }

    /**
     * 获取草稿列表
     * 接口地址: POST /cgi-bin/draft/batchget
     *
     * @param offset 从全部素材的该偏移位置开始返回，0表示从第一个素材返回
     * @param count  返回素材的数量，取值在1到20之间
     * @param noContent 1 表示不返回 content 字段，0 表示正常返回，默认为 0
     * @return 草稿列表
     */
    public DraftResponse batchGet(int offset, int count, Integer noContent) {
        LOG.debug("获取草稿列表......");
        String url = BASE_API_URL + "cgi-bin/draft/batchget?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("count", count);
        if (noContent != null) {
            params.put("no_content", noContent);
        }
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, DraftResponse.class);
    }

    /**
     * 获取草稿列表（默认不忽略content）
     */
    public DraftResponse batchGet(int offset, int count) {
        return batchGet(offset, count, null);
    }
}
