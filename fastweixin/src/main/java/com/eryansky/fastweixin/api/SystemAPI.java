package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.GetIpListResponse;
import com.eryansky.fastweixin.util.CollectionUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import com.eryansky.fastweixin.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统API
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class SystemAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(SystemAPI.class);

    public SystemAPI(ApiConfig config) {
        super(config);
    }

    // ========================= IP地址相关 =========================

    /**
     * 获取微信API服务器IP段
     * 接口地址: GET /cgi-bin/get_api_domain_ip
     *
     * 用于获取微信API服务器出口IP段，方便开发者设置防火墙白名单。
     * 微信服务器IP可能会变动，建议定期调用（如每天一次）并缓存结果。
     *
     * 注意：本接口返回的是微信API服务器发起调用时使用的出口IP，
     * 而非微信推送服务器的IP。如需获取推送服务器IP请使用 {@link #getPushServerIp()}
     *
     * @return IP列表响应对象
     */
    public GetIpListResponse getApiDomainIp() {
        LOG.debug("获取微信API服务器IP段......");
        String url = BASE_API_URL + "cgi-bin/get_api_domain_ip?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GetIpListResponse.class);
    }

    /**
     * 获取微信API服务器IP段（简化版，直接返回IP列表）
     *
     * @return IP地址列表，请求失败时返回null
     */
    public List<String> getApiDomainIpList() {
        LOG.debug("获取微信API服务器IP段(简化)......");
        String url = BASE_API_URL + "cgi-bin/get_api_domain_ip?access_token=#";
        BaseResponse r = executeGet(url);
        if (isSuccess(r.getErrcode())) {
            JsonNode array = JSONUtil.getJSONFromString(r.getErrmsg()).get("ip_list");
            if (array != null && array.isArray()) {
                List<String> result = CollectionUtil.newArrayList(array.size());
                for (JsonNode node : array) {
                    result.add(node.asText());
                }
                return result;
            }
        }
        return null;
    }

    /**
     * 获取微信推送服务器IP段
     * 接口地址: GET /cgi-bin/getcallbackip
     *
     * 用于获取微信推送服务器出口IP段，方便开发者在服务器端设置防火墙白名单，
     * 验证推送请求确实来自微信服务器。
     *
     * 注意：本接口返回的是微信推送服务器发起推送时使用的出口IP，
     * 与API服务器IP {@link #getApiDomainIp()} 不同，两者均需要加入白名单。
     *
     * @return IP列表响应对象
     */
    public GetIpListResponse getPushServerIp() {
        LOG.debug("获取微信推送服务器IP段......");
        String url = BASE_API_URL + "cgi-bin/getcallbackip?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GetIpListResponse.class);
    }

    /**
     * 获取微信推送服务器IP段（简化版，直接返回IP列表）
     *
     * @return IP地址列表，请求失败时返回null
     */
    public List<String> getPushServerIpList() {
        LOG.debug("获取微信推送服务器IP段(简化)......");
        String url = BASE_API_URL + "cgi-bin/getcallbackip?access_token=#";
        BaseResponse r = executeGet(url);
        if (isSuccess(r.getErrcode())) {
            JsonNode array = JSONUtil.getJSONFromString(r.getErrmsg()).get("ip_list");
            if (array != null && array.isArray()) {
                List<String> result = CollectionUtil.newArrayList(array.size());
                for (JsonNode node : array) {
                    result.add(node.asText());
                }
                return result;
            }
        }
        return null;
    }

    /**
     * 获取微信服务器IP地址列表
     *
     * @return IP地址列表
     * @deprecated 请使用语义更清晰的 {@link #getPushServerIpList()} 获取推送服务器IP，
     *             或 {@link #getApiDomainIpList()} 获取API服务器IP
     */
    @Deprecated
    public List<String> getCallbackIP() {
        return getPushServerIpList();
    }

    /**
     * 将一条长链接转成短链接
     *
     * @param longUrl 长链接
     * @return 对应的短链接
     */
    public String getShortUrl(String longUrl) {
        String result = "";
        LOG.debug("获取短URL.......");
        if (checkUrl(longUrl)) {
            String url = BASE_API_URL + "cgi-bin/shorturl?access_token=#";
            Map<String, String> params = new HashMap<String, String>();
            params.put("action", "long2short");
            params.put("long_url", longUrl);
            BaseResponse r = executePost(url, JSONUtil.toJson(params));
            if (isSuccess(r.getErrcode())) {
                result = JSONUtil.toMap(r.getErrmsg()).get("short_url").toString();
            }
        }
        return result;
    }

    /**
     * 检查URL是否支持
     *
     * @param url 需要检查的URL
     * @return 是否支持
     */
    private boolean checkUrl(String url) {
        return StrUtil.isNotBlank(url) && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("weixin://wxpay"));
    }
}
