package com.eryansky.core.rpc.client;

import com.eryansky.common.utils.mapper.JsonMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

public class RpcConsumerExecutor {
    /**
     * 默认是Http协议
     */
    private static final String SCHEME = "http://";

    private static JsonMapper jsonMapper = JsonMapper.getInstance();

    public static Object execute(String url, Object[] params, Class<?> resultType) throws Exception {
        // 获取RestTemplate对象
        RestTemplate restTemplate = RpcRestTemplateUtils.restTemplate();
        // 构建请求体
        HttpEntity<?> httpEntity = createHttpEntity(params);
        // 进行远程rpc请求
        ResponseEntity responseEntity = restTemplate.exchange(SCHEME + url, HttpMethod.POST, httpEntity, resultType);
        // 返回接口
        return responseEntity.getBody();
    }

    /**
     * 构建请求体，默认是JSON数组
     *
     * @param params
     * @return
     */
    private static HttpEntity<?> createHttpEntity(Object[] params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (params != null && params.length != 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int i = 0; i < params.length; i++) {
                builder.append(jsonMapper.toJson(params[i]));
                if (i != params.length - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");
            return new HttpEntity<>(builder.toString().getBytes(StandardCharsets.UTF_8), httpHeaders);
        } else {
            return new HttpEntity<>(httpHeaders);
        }
    }
}
