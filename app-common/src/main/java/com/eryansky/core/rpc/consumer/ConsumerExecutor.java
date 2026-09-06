package com.eryansky.core.rpc.consumer;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.config.RestTemplateHolder;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.rpc.utils.SerializerFactory;
import com.eryansky.encrypt.util.RequestEncryptUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class ConsumerExecutor {
    private static final Logger log = LoggerFactory.getLogger(ConsumerExecutor.class);
    private static final JsonMapper jsonMapper = JsonMapper.getInstance();

    public static <T> T execute(String url, Map<String, String> headers, Object[] params, ParameterizedTypeReference<T> responseType) throws Exception {
        RestTemplate restTemplate = RestTemplateHolder.restTemplate();

        String requestEncrypt = headers != null ? headers.get(RPCUtils.HEADER_ENCRYPT) : null;
        String serializer = headers != null ? headers.get(RPCUtils.HEADER_RPC_SERIALIZER) : "json";
        RPCUtils.EncryptRequestKey encryptRequestKey = RPCUtils.generateEncryptKey(requestEncrypt);

        HttpEntity<?> httpEntity = createHttpEntity(params, headers, encryptRequestKey, serializer, requestEncrypt);
        boolean isEncrypted = StringUtils.isNotBlank(requestEncrypt);

        try {
            if (isEncrypted) {
                // 加密请求处理
                ResponseEntity<byte[]> byteResponse = restTemplate.exchange(url, HttpMethod.POST, httpEntity, byte[].class);
                checkResponseStatus(url, byteResponse);

                byte[] data = byteResponse.getBody();
                if (data != null && data.length > 0) {
                    byte[] decryptedData = RequestEncryptUtils.decryptData(requestEncrypt, encryptRequestKey.getKey(), data);
                    return (T) SerializerFactory.getSerializer(serializer).deserialize(decryptedData);
                }
                log.debug("RPC请求成功，但返回的数据为空: {}", url);
                return null; // 避免原代码中数据为空时，再次发起危险的二次 POST 请求
            } else {
                // 非加密请求处理
                ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, responseType);
                checkResponseStatus(url, responseEntity);
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.warn("RPC请求出现异常或泛型解析失败，尝试降级解析. URL: {}", url);
            // 降级处理：针对泛型解析失败的情况
            return executeFallback(url, httpEntity, restTemplate, responseType);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Request Headers: {}", headers != null ? JsonMapper.toJsonString(headers) : "null");
            }
        }
    }

    /**
     * 降级解析逻辑：当带有复杂泛型的 exchange 失败时，尝试转为 Object 接收并手动映射
     */
    private static <T> T executeFallback(String url, HttpEntity<?> httpEntity, RestTemplate restTemplate, ParameterizedTypeReference<T> responseType) {
        ResponseEntity<Object> fallbackResponse = null;
        try {
            fallbackResponse = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Object.class);
            checkResponseStatus(url, fallbackResponse);

            // 避免强转 (String) 导致 ClassCastException，直接转换为 JSON 字符串再解析
            String json = jsonMapper.toJsonString(fallbackResponse.getBody());
            JavaType javaType = jsonMapper.getTypeFactory().constructType(responseType.getType());
            return jsonMapper.toJavaObject(json, javaType);
        } catch (Exception ex) {
            Object responseBody = fallbackResponse != null ? fallbackResponse.getBody() : "null";
            log.error("RPC降级解析依然失败: URL={}, ResponseBody={}", url, responseBody, ex);
            throw new RuntimeException("RPC请求或解析异常: " + url, ex);
        }
    }

    /**
     * 统一校验 HTTP 状态码
     */
    private static void checkResponseStatus(String url, ResponseEntity<?> responseEntity) {
        if (responseEntity == null) {
            throw new RuntimeException("RPC请求异常: responseEntity is null, URL=" + url);
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            // 性能优化：不要使用 JsonMapper 序列化整个 ResponseEntity
            log.error("RPC请求异常: URL={}, Status={}, Body={}",
                    url, responseEntity.getStatusCode().value(), responseEntity.getBody());
            throw new RuntimeException("RPC请求异常: URL=" + url + ", 状态码=" + responseEntity.getStatusCode().value());
        }
    }

    /**
     * 构建请求体
     */
    private static HttpEntity<?> createHttpEntity(Object[] params, Map<String, String> headers,
                                                  RPCUtils.EncryptRequestKey encryptRequestKey,
                                                  String serializer, String encrypt) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }

        if (StringUtils.isNotBlank(encrypt)) {
            httpHeaders.setContentType(MediaType.parseMediaType("application/x-" + serializer + "-secure"));
            httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON, MediaType.parseMediaType("application/x-" + serializer + "-secure")));
        } else {
            httpHeaders.setContentType(MediaType.parseMediaType("application/x-" + serializer));
            httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON, MediaType.parseMediaType("application/x-" + serializer)));
        }

        if (StringUtils.isNotBlank(encryptRequestKey.getEncryptKey())) {
            headers.put(RPCUtils.HEADER_ENCRYPT_KEY, encryptRequestKey.getEncryptKey());
            httpHeaders.put(RPCUtils.HEADER_ENCRYPT_KEY, Lists.newArrayList(encryptRequestKey.getEncryptKey()));
        }

        byte[] bytes = SerializerFactory.getSerializer(serializer).serialize(params);
        byte[] data = RequestEncryptUtils.encryptData(encrypt, encryptRequestKey.getKey(), bytes);

        return new HttpEntity<>(data, httpHeaders);
    }
}