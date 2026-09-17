package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.MsgSecCheckResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 内容安全相关API（公众号）
 * 包含文本、图片、音频等内容安全检查接口
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class ContentSecurityAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSecurityAPI.class);

    public ContentSecurityAPI(ApiConfig config) {
        super(config);
    }

    /**
     * 文本安全内容检测（旧版）
     * 接口地址: POST /wxa/msg_sec_check
     *
     * @param content 要检测的文本内容，不超过500KB，不超过1MB，支持UTF-8编码
     * @return 检测结果，errcode=0为正常，87014为违规内容
     */
    public BaseResponse msgSecCheck(String content) {
        BeanUtil.requireNonNull(content, "content is null");
        LOG.debug("文本内容安全检测......");
        String url = BASE_API_URL + "wxa/msg_sec_check?access_token=#";
        Map<String, String> params = new HashMap<>();
        params.put("content", content);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 文本安全内容检测（新版v2）
     * 接口地址: POST /wxa/msg_sec_check
     *
     * @param version 接口版本号，固定传2
     * @param openid  用户的openid
     * @param scene   场景值，1-资料, 2-评论, 3-论坛, 4-社交日志
     * @param content 要检测的文本内容
     * @return 内容安全检查详细响应
     */
    public MsgSecCheckResponse msgSecCheckV2(Integer version, String openid, Integer scene, String content) {
        BeanUtil.requireNonNull(openid, "openid is null");
        BeanUtil.requireNonNull(content, "content is null");
        LOG.debug("文本内容安全检测(v2)......");
        String url = BASE_API_URL + "wxa/msg_sec_check?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("version", version != null ? version : 2);
        params.put("openid", openid);
        params.put("scene", scene != null ? scene : 2);
        params.put("content", content);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, MsgSecCheckResponse.class);
    }

    /**
     * 文本安全内容检测（简化版）
     */
    public MsgSecCheckResponse msgSecCheckV2(String openid, String content) {
        return msgSecCheckV2(2, openid, 2, content);
    }

    /**
     * 异步校验图片/音频是否含有违法违规内容
     * 接口地址: POST /wxa/media_check_async
     *
     * @param mediaUrl  要检测的多媒体url
     * @param mediaType 1-音频; 2-图片
     * @return 异步审核响应（trace_id用于后续查询结果）
     */
    public BaseResponse mediaCheckAsync(String mediaUrl, Integer mediaType) {
        BeanUtil.requireNonNull(mediaUrl, "mediaUrl is null");
        BeanUtil.requireNonNull(mediaType, "mediaType is null");
        LOG.debug("异步内容安全审核......");
        String url = BASE_API_URL + "wxa/media_check_async?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("media_url", mediaUrl);
        params.put("media_type", mediaType);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 异步校验图片/音频（v2版本）
     *
     * @param mediaUrl  要检测的多媒体url
     * @param mediaType 1-音频; 2-图片
     * @param version   接口版本号，固定传2
     * @param openid    用户的openid
     * @param scene     场景值
     * @return 异步审核响应
     */
    public BaseResponse mediaCheckAsyncV2(String mediaUrl, Integer mediaType, Integer version, String openid, Integer scene) {
        BeanUtil.requireNonNull(mediaUrl, "mediaUrl is null");
        BeanUtil.requireNonNull(openid, "openid is null");
        LOG.debug("异步内容安全审核(v2)......");
        String url = BASE_API_URL + "wxa/media_check_async?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("media_url", mediaUrl);
        params.put("media_type", mediaType);
        params.put("version", version != null ? version : 2);
        params.put("openid", openid);
        params.put("scene", scene != null ? scene : 2);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 图片安全内容检测（旧版，上传文件方式）
     * 接口地址: POST /wxa/img_sec_check
     *
     * @param imageFile 要检测的图片文件，不超过1MB
     * @return 检测结果，errcode=0为正常，87014为违规内容
     */
    public BaseResponse imageSecCheck(File imageFile) {
        BeanUtil.requireNonNull(imageFile, "imageFile is null");
        LOG.debug("图片内容安全检测......");
        String url = BASE_API_URL + "wxa/img_sec_check?access_token=#";
        BaseResponse response = executePost(url, null, imageFile);
        return response;
    }
}
