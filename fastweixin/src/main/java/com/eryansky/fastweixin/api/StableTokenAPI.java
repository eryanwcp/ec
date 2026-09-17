package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.GetStableTokenResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 稳定版 Token API
 *
 * 获取稳定版接口调用凭据，有两种调用模式:
 * 1. 普通模式: access_token 有效期内重复调用返回相同结果，过期后调用返回新的 access_token
 * 2. 强制刷新模式: 返回新的 access_token，旧 access_token 在5分钟内仍有效
 *
 * 接口文档: https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Get_stable_access_token.html
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class StableTokenAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(StableTokenAPI.class);

    public StableTokenAPI(ApiConfig config) {
        super(config);
    }

    /**
     * 获取稳定版接口调用凭据（普通模式）
     * 接口地址: POST /cgi-bin/stable_token
     *
     * @return 包含 access_token 和 expires_in 的响应
     */
    public GetStableTokenResponse getStableAccessToken() {
        return getStableAccessToken(false);
    }

    /**
     * 获取稳定版接口调用凭据
     * 接口地址: POST /cgi-bin/stable_token
     *
     * @param forceRefresh 是否强制刷新。
     *                     false: 普通模式，access_token 有效期内重复调用返回相同结果
     *                     true:  强制刷新模式，返回新的 access_token，旧的 access_token 将在5分钟内继续有效
     * @return 包含 access_token 和 expires_in 的响应
     */
    public GetStableTokenResponse getStableAccessToken(boolean forceRefresh) {
        LOG.debug("获取稳定版access_token......");
        String url = BASE_API_URL + "cgi-bin/stable_token";
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credential");
        params.put("appid", this.config.getAppid());
        params.put("secret", this.config.getSecret());
        params.put("force_refresh", forceRefresh);
        // stable_token接口不需要在URL中带access_token
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GetStableTokenResponse.class);
    }
}
