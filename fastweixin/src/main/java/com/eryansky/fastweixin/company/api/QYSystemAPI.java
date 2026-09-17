package com.eryansky.fastweixin.company.api;

import com.eryansky.fastweixin.company.api.config.QYAPIConfig;
import com.eryansky.fastweixin.company.api.response.GetQYIPResponse;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.util.CollectionUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 企业微信系统API
 * 提供获取企业微信服务器IP段等系统级接口
 *
 * 接口文档:
 * - 获取企业微信API域名IP段: https://developer.work.weixin.qq.com/document/path/92520
 * - 获取企业微信服务器的ip段: https://developer.work.weixin.qq.com/document/path/90238
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class QYSystemAPI extends QYBaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(QYSystemAPI.class);

    public QYSystemAPI(QYAPIConfig config) {
        super(config);
    }

    /**
     * 获取企业微信API域名IP段
     * 接口地址: GET /cgi-bin/get_api_domain_ip
     *
     * 用于获取企业微信API的域名IP段，方便企业设置防火墙白名单。
     * 企业微信服务器的出口IP可能会变动，建议定期调用该接口并缓存结果。
     *
     * @return IP列表响应
     */
    public GetQYIPResponse getApiDomainIp() {
        LOG.debug("获取企业微信API域名IP段......");
        String url = BASE_API_URL + "cgi-bin/get_api_domain_ip?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GetQYIPResponse.class);
    }

    /**
     * 获取企业微信回调IP段
     * 接口地址: GET /cgi-bin/getcallbackip
     *
     * 用于获取企业微信回调服务器的IP段，方便企业设置防火墙白名单。
     * 企业微信回调服务器的出口IP可能会变动，建议定期调用该接口并缓存结果。
     *
     * @return IP列表响应
     */
    public GetQYIPResponse getCallbackIp() {
        LOG.debug("获取企业微信回调IP段......");
        String url = BASE_API_URL + "cgi-bin/getcallbackip?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GetQYIPResponse.class);
    }

    /**
     * 获取企业微信API域名IP段（简化版，直接返回IP列表）
     *
     * @return IP地址列表，请求失败时返回null
     */
    public List<String> getApiDomainIpList() {
        LOG.debug("获取企业微信API域名IP段(简化)......");
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
     * 获取企业微信回调IP段（简化版，直接返回IP列表）
     *
     * @return IP地址列表，请求失败时返回null
     */
    public List<String> getCallbackIpList() {
        LOG.debug("获取企业微信回调IP段(简化)......");
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
}
