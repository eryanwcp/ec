package com.eryansky.encrypt.advice;

import com.eryansky.encrypt.anotation.Encrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Override  
    public boolean supports(MethodParameter returnType, Class converterType) {
        Encrypt encrypt = returnType.getMethodAnnotation(Encrypt.class);
        //如果带有注解且标记为验签，测进行验签操作
        return null != encrypt;
    }  

    @Override  
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 拿到密钥 设置到响应头 前端获取 通过RSA解密获取AES密钥 再通过AES解密器对密文解密
//        response.getHeaders().set(AESKEY, HoneyBadgerEncrypt.getAesKeyRSACiphertext());
        //拿到密钥 设置到响应头 前端获取 通过RSA解密获取SM4密钥 再通过SM4解密器对密文解密
//        response.getHeaders().set(SM4KEY,HoneyBadgerEncrypt.getSm4KeyRSACiphertext());
        return body;  
    }  
}  