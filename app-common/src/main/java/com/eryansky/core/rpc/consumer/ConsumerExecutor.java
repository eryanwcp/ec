package com.eryansky.core.rpc.consumer;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.config.RestTemplateHolder;
import com.eryansky.utils.AppConstants;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

public class ConsumerExecutor {

    public static final String REST_AUTHORITY_HEADER_NAME = "X-Api-Key";
    private static final JsonMapper jsonMapper = JsonMapper.getInstance();

    public static Object execute(String url, Object[] params, Class<?> resultType) throws Exception {
        // 获取RestTemplate对象
        RestTemplate restTemplate = RestTemplateHolder.restTemplate();
        // 构建请求体
        HttpEntity<?> httpEntity = createHttpEntity(params);
        // 进行远程rpc请求
        ResponseEntity responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, resultType);
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
        httpHeaders.add(REST_AUTHORITY_HEADER_NAME, AppConstants.getRestDefaultApiKey());
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
