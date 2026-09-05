package com.eryansky.encrypt.advice;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.EncodeUtils;
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
        return null != annotation && Boolean.parseBoolean(annotation.enable()) && annotation.handle().equals(this.getClass());
    }

    @Override  
    public Object handleEmptyBody(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;  
    }  

    @Override  
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        HttpHeaders headers = httpInputMessage.getHeaders();
//        String requestEncrypt = Collections3.getFirst(headers.get(RequestEncryptUtils.ENCRYPT));
//        String requestEncryptKey = Collections3.getFirst(headers.get(RequestEncryptUtils.ENCRYPT_KEY));
        HttpServletRequest request = SpringMVCHolder.getRequest();
        String requestEncrypt = WebUtils.getHeaderIgnoreCaseOrParameter(request,RequestEncryptUtils.ENCRYPT);
        String requestEncryptKey = WebUtils.getHeaderIgnoreCaseOrParameter(request,RequestEncryptUtils.ENCRYPT_KEY);

        if (StringUtils.isNotBlank(requestEncrypt)){
            return new HttpInputMessage() {
                @Override
                public InputStream getBody() throws IOException {
                    try {
                        if(StringUtils.isNotBlank(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey)){
                            byte[] data = null;
                            if (CipherMode.SM4.name().equals(requestEncrypt)) {
                                data = EncodeUtils.hexDecode(IOUtils.toCharArray(httpInputMessage.getBody(), StandardCharsets.UTF_8));
                            } else if (CipherMode.AES.name().equals(requestEncrypt)) {
                                data = EncodeUtils.base64Decode(IOUtils.toByteArray(httpInputMessage.getBody()));
                            } else if (CipherMode.BASE64.name().equals(requestEncrypt)) {
                                data =  Base64.decodeBase64(IOUtils.toByteArray(httpInputMessage.getBody()));
                            }
                            byte[] decryptData = RequestEncryptUtils.decryptDataByRequest(requestEncrypt,requestEncryptKey, data);
                            return new ByteArrayInputStream(decryptData);
                        }
                        return httpInputMessage.getBody();
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                        return httpInputMessage.getBody();
//                        throw new RuntimeException(e);
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