package com.eryansky.encrypt.advice;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.encrypt.anotation.DecryptRequestBody;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.encrypt.util.RequestEncryptUtils;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@RestControllerAdvice
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {

    private static final Logger log = LoggerFactory.getLogger(DecryptRequestBodyAdvice.class);

    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        DecryptRequestBody annotation = methodParameter.getMethodAnnotation(DecryptRequestBody.class);
        if (annotation == null) {
            return false;
        }
        return Boolean.parseBoolean(annotation.enable()) && annotation.handle() == this.getClass();
    }

    @Override  
    public Object handleEmptyBody(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;  
    }  

    @Override  
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        HttpServletRequest request = SpringMVCHolder.getRequest();
        if (request == null) {
            return httpInputMessage;
        }
        String requestEncrypt = WebUtils.getHeaderIgnoreCaseOrParameter(request,RequestEncryptUtils.ENCRYPT);
        String requestEncryptKey = WebUtils.getHeaderIgnoreCaseOrParameter(request,RequestEncryptUtils.ENCRYPT_KEY);
        if (StringUtils.isBlank(requestEncrypt) || StringUtils.isBlank(requestEncryptKey)) {
            return httpInputMessage;
        }

        byte[] decryptBytes;
        try {
            decryptBytes = RequestEncryptUtils.decryptEncodeDataByRequest(request, IOUtils.toByteArray(httpInputMessage.getBody()));
        } catch (Exception e) {
            log.error("请求体解密异常, URI: {}, EncryptType: {}, Error: {}",
                    request.getRequestURI(), requestEncrypt, e.getMessage(), e);
            throw new IllegalArgumentException("请求体数据解密失败: " + e.getMessage(), e);
        }

        return new DecryptedHttpInputMessage(httpInputMessage.getHeaders(), decryptBytes);
    }

    @Override
    public Object afterBodyRead(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;
    }

    /**
     * 内部解密数据 HttpInputMessage 包装类
     */
    private static class DecryptedHttpInputMessage implements HttpInputMessage {
        private final HttpHeaders headers;
        private final byte[] bodyBytes;

        public DecryptedHttpInputMessage(HttpHeaders headers, byte[] bodyBytes) {
            this.headers = headers;
            this.bodyBytes = bodyBytes != null ? bodyBytes : new byte[0];
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(bodyBytes);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}  