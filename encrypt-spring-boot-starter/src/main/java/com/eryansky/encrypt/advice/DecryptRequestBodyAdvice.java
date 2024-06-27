package com.eryansky.encrypt.advice;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.badger.HoneyBadgerEncrypt;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@RestControllerAdvice
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {

    private static final Logger log = LoggerFactory.getLogger(DecryptRequestBodyAdvice.class);

    public static String  AESKEY = "AES-RSA";
    public static String  SM4KEY = "SM4-RSA";

    @Autowired
    private HoneyBadgerEncrypt honeyBadgerEncrypt;
    @Override  
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        Decrypt decrypt = methodParameter.getMethodAnnotation(Decrypt.class);
        return null != decrypt;
    }

    @Override  
    public Object handleEmptyBody(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;  
    }  

    @Override  
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        Decrypt decrypt = methodParameter.getMethodAnnotation(Decrypt.class);
        HttpHeaders headers = httpInputMessage.getHeaders();
        String content = IOUtils.toString(httpInputMessage.getBody(), StandardCharsets.UTF_8);
        String aesKey = Collections3.getFirst(headers.get(AESKEY));
        String sm4Key = Collections3.getFirst(headers.get(SM4KEY));
        if (StringUtils.isNotBlank(aesKey) && !StringUtils.equals(aesKey,"null") ){
            return new DecryptHttpInputMessage(httpInputMessage,honeyBadgerEncrypt.aesDecrypt(content), StandardCharsets.UTF_8.name());
        }
        if (StringUtils.isNotBlank(sm4Key) && !StringUtils.equals(sm4Key,"null") ){
            return new DecryptHttpInputMessage(httpInputMessage,honeyBadgerEncrypt.sm4Decrypt(content), StandardCharsets.UTF_8.name());
        }
        return httpInputMessage;
    }  

    @Override  
    public Object afterBodyRead(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {  
        return o;  
    }  
}

class DecryptHttpInputMessage implements HttpInputMessage {
    private Logger log = LoggerFactory.getLogger(DecryptRequestBodyAdvice.class);
    private HttpHeaders headers;
    private InputStream body;

    public DecryptHttpInputMessage(HttpInputMessage inputMessage, String content, String charset) {
        this.headers = inputMessage.getHeaders();
        long startTime = System.currentTimeMillis();
        // JSON 数据格式的不进行解密操作
        String decryptBody = "";
        if (content.startsWith("{")) {
            decryptBody = content;
        }
        long endTime = System.currentTimeMillis();
        log.debug("Decrypt Time:" + (endTime - startTime));
        this.body = IOUtils.toInputStream(decryptBody, charset);
    }

    @Override
    public InputStream getBody() throws IOException {
        return body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }
}