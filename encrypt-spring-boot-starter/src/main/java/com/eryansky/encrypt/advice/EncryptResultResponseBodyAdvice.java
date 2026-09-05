package com.eryansky.encrypt.advice;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.util.RequestEncryptUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 默认加密策略 返回值为Result
 * 注：需客户端自行传递加密方式以及加密密钥参数
 */
@RestControllerAdvice
public class EncryptResultResponseBodyAdvice implements ResponseBodyAdvice<Result> {

    private static final Logger log = LoggerFactory.getLogger(EncryptResultResponseBodyAdvice.class);
    private static final JsonMapper jsonMapper = JsonMapper.getInstance();

    @Override  
    public boolean supports(MethodParameter returnType, Class converterType) {
        EncryptResponseBody annotation = returnType.getMethodAnnotation(EncryptResponseBody.class);
        if (annotation == null) {
            return false;
        }
        return Boolean.parseBoolean(annotation.enable()) && annotation.handle() == this.getClass();
    }

    @Override  
    public Result beforeBodyWrite(Result body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 1. 前置防御校验：body 或 body.data 为空时直接返回
        if (body == null) {
            return body;
        }

        // 2. 安全获取 HttpServletRequest
        if (!(request instanceof ServletServerHttpRequest servletServerHttpRequest)) {
            return body;
        }
        HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
        String requestEncrypt = WebUtils.getHeaderIgnoreCaseOrParameter(servletRequest,RequestEncryptUtils.ENCRYPT);
        String requestEncryptKey = WebUtils.getHeaderIgnoreCaseOrParameter(servletRequest,RequestEncryptUtils.ENCRYPT_KEY);
        try {
            if(body.getData() != null){
                byte[] dataBytes = jsonMapper.writeValueAsBytes(body.getData());
                String encryptedData = RequestEncryptUtils.encryptDataStringByRequest(requestEncrypt, requestEncryptKey, dataBytes);
                body.setData(encryptedData);
            }
            if(body.getObj() != null){
                byte[] objBytes = jsonMapper.writeValueAsBytes(body.getObj());
                String encryptedObj = RequestEncryptUtils.encryptDataStringByRequest(requestEncrypt, requestEncryptKey, objBytes);
                body.setObj(encryptedObj);
            }
        } catch (Exception e) {
            log.error("响应数据加密异常, URI: {}, EncryptType: {}, Error: {}",
                    servletRequest.getRequestURI(), requestEncrypt, e.getMessage(), e);
            throw new IllegalStateException("响应数据加密失败: " + e.getMessage(), e);
        }

        return body;  
    }

}