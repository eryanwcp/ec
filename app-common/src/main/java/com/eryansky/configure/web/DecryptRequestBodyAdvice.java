package com.eryansky.configure.web;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.badger.HoneyBadgerEncrypt;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

//@RestControllerAdvice
public class DecryptRequestBodyAdvice implements RequestBodyAdvice {

    private static final Logger log = LoggerFactory.getLogger(DecryptRequestBodyAdvice.class);

    public static String  AESKEY = "AES-RSA";
    public static String  SM4KEY = "SM4-RSA";

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
        HttpHeaders headers = httpInputMessage.getHeaders();
        String aesKey = Collections3.getFirst(headers.get(AESKEY));
        String sm4Key = Collections3.getFirst(headers.get(SM4KEY));
        if (StringUtils.isNotBlank(aesKey) && !StringUtils.equals(aesKey,"null") ){
            HoneyBadgerEncrypt.setRSACiphertextForAESKey(aesKey);
        }
        if (StringUtils.isNotBlank(sm4Key) && !StringUtils.equals(sm4Key,"null") ){
            HoneyBadgerEncrypt.setRSACiphertextForSM4Key(sm4Key);
        }
        return httpInputMessage;
    }  

    @Override  
    public Object afterBodyRead(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {  
        return o;  
    }  
}  