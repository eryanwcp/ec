package com.eryansky.encrypt.advice;

import com.eryansky.common.orm.mybatis.sensitive.encrypt.AesSupport;
import com.eryansky.common.utils.encode.RSAUtil;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.io.IoUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.encrypt.anotation.DecryptRequestBody;
import com.eryansky.encrypt.enums.CipherMode;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public static String  ENCRYPT = "encrypt";
    public static String  ENCRYPT_KEY = "encrypt-key";


    @Override  
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        DecryptRequestBody decrypt = methodParameter.getMethodAnnotation(DecryptRequestBody.class);
        return null != decrypt;
    }

    @Override  
    public Object handleEmptyBody(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;  
    }  

    @Override  
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        HttpHeaders headers = httpInputMessage.getHeaders();
        String requestEncrypt = Collections3.getFirst(headers.get(ENCRYPT));
        String requestEncryptKey = Collections3.getFirst(headers.get(ENCRYPT_KEY));
        if (StringUtils.isNotBlank(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey) ){
            String key = RSAUtil.decryptBase64(requestEncryptKey);
            return new HttpInputMessage() {
                @Override
                public InputStream getBody() throws IOException {
                    String content = IoUtils.toString(httpInputMessage.getBody(), StandardCharsets.UTF_8);
                    try {
                        if(CipherMode.SM4.name().equals(requestEncrypt)){
                            return IOUtils.toInputStream(Sm4Utils.decrypt(key, content), StandardCharsets.UTF_8);
                        }else if(CipherMode.AES.name().equals(requestEncrypt)){
                            return IOUtils.toInputStream(new AesSupport(key).decrypt(content), StandardCharsets.UTF_8);
                        }
                        return IOUtils.toInputStream(content, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public HttpHeaders getHeaders() {
                    return headers;
                }
            };


        }
        return httpInputMessage;
    }  

    @Override  
    public Object afterBodyRead(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {  
        return o;  
    }  
}  