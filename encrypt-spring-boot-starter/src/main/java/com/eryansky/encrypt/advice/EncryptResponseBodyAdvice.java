package com.eryansky.encrypt.advice;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import com.eryansky.encrypt.badger.HoneyBadgerEncrypt;
import com.eryansky.encrypt.enums.CipherMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice {

    private static final Logger log = LoggerFactory.getLogger(EncryptResponseBodyAdvice.class);

    public static String  AESKEY = "AES-RSA";
    public static String  SM4KEY = "SM4-RSA";
    @Autowired
    private HoneyBadgerEncrypt honeyBadgerEncrypt;
    @Override  
    public boolean supports(MethodParameter returnType, Class converterType) {
        Encrypt encrypt = returnType.getMethodAnnotation(Encrypt.class);
        //如果带有注解且标记为验签，测进行验签操作
        return null != encrypt;
    }  

    @Override  
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Encrypt encrypt = returnType.getMethodAnnotation(Encrypt.class);
        String content = null;
        try {
            content = JsonMapper.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if(CipherMode.AES.equals(encrypt.cipher())){
            return  honeyBadgerEncrypt.aesEncrypt(content);
        }else if(CipherMode.RSA.equals(encrypt.cipher())){
            return  honeyBadgerEncrypt.rsaEncrypt(content);
        }
        return body;  
    }  
}  