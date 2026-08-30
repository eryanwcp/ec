package com.eryansky.encrypt.advice;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.util.RequestEncryptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 默认加密策略 返回值为Result
 */
@RestControllerAdvice
public class EncryptResultResponseBodyAdvice implements ResponseBodyAdvice<Result> {

    private static final Logger log = LoggerFactory.getLogger(EncryptResultResponseBodyAdvice.class);

    @Override  
    public boolean supports(MethodParameter returnType, Class converterType) {
        EncryptResponseBody encrypt = returnType.getMethodAnnotation(EncryptResponseBody.class);
        //如果带有注解且标记为验签，则进行验签操作
        return null != encrypt && Boolean.parseBoolean(encrypt.enable()) && encrypt.handle().getName().equals(this.getClass().getName());
    }  

    @Override  
    public Result beforeBodyWrite(Result body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        String requestEncrypt = Collections3.getFirst(headers.get(RequestEncryptUtils.ENCRYPT));
        String requestEncryptKey = Collections3.getFirst(headers.get(RequestEncryptUtils.ENCRYPT_KEY));
        if (StringUtils.isNotBlank(requestEncrypt)){
            if(body != null && body.getData() != null){
                try {
                    byte[] data = JsonMapper.getInstance().writeValueAsBytes(body.getData());
                    body.setData(RequestEncryptUtils.encryptDataByRequest(requestEncrypt,requestEncryptKey,data));
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    throw new RuntimeException(e);
                }
            }
            if(body != null && body.getObj() != null){
                try {
                    byte[] obj = JsonMapper.getInstance().writeValueAsBytes(body.getObj());
                    body.setObj(RequestEncryptUtils.encryptDataByRequest(requestEncrypt,requestEncryptKey,obj));
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    throw new RuntimeException(e);
                }
            }
        }

        return body;  
    }

}