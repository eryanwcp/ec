package com.eryansky.core.rpc.consumer;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.config.RestTemplateHolder;
import com.eryansky.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConsumerExecutor {

    private static final Logger log = LoggerFactory.getLogger(ConsumerExecutor.class);
    private static final JsonMapper jsonMapper = JsonMapper.getInstance();

    public static <T> T  execute(String url, Map<String,String> headers, Object[] params, ParameterizedTypeReference responseType) throws Exception {
        // 获取RestTemplate对象
        RestTemplate restTemplate = RestTemplateHolder.restTemplate();
        // 构建请求体
        HttpEntity<?> httpEntity = createHttpEntity(params,headers);
        // 进行远程rpc请求
        ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, responseType);
        if(!HttpStatus.OK.equals(responseEntity.getStatusCode())){
            log.error("{}",JsonMapper.toJsonString(responseEntity.getBody()));
        }
        // 返回接口
        return responseEntity.getBody();
    }

    /**
     * 构建请求体，默认是JSON数组
     *
     * @param params
     * @return
     */
    private static HttpEntity<?> createHttpEntity(Object[] params, Map<String,String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if(null != headers){
            headers.forEach(httpHeaders::add);
        }

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
