package com.eryansky.encrypt.advice;

import com.eryansky.common.model.R;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.nio.charset.StandardCharsets;

/**
 * 默认加密策略 返回值为R
 */
@RestControllerAdvice
public class EncryptRResponseBodyAdvice implements ResponseBodyAdvice<R<Object>> {

    private static final Logger log = LoggerFactory.getLogger(EncryptRResponseBodyAdvice.class);

    public static final String  ENCRYPT = "Encrypt";
    public static final String  ENCRYPT_KEY = "Encrypt-Key";

    @Override  
    public boolean supports(MethodParameter returnType, Class converterType) {
        EncryptResponseBody encrypt = returnType.getMethodAnnotation(EncryptResponseBody.class);
        //如果带有注解且标记为验签，则进行验签操作
        return (null != encrypt && encrypt.defaultHandle());
    }  

    @Override
    public R<Object> beforeBodyWrite(R<Object> body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        String requestEncrypt = Collections3.getFirst(headers.get(ENCRYPT));
        String requestEncryptKey = Collections3.getFirst(headers.get(ENCRYPT_KEY));
        if (StringUtils.isNotBlank(requestEncrypt)){
            String data = JsonMapper.toJsonString(body.getData());
            if(CipherMode.SM4.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        String key = RSAUtils.decryptHexString(requestEncryptKey, EncryptProvider.privateKeyBase64());
                        body.setData(Sm4Utils.encrypt(key,data));
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                        throw new RuntimeException(e);
                    }
                }
            }else if(CipherMode.AES.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        String key = RSAUtils.decryptBase64String(requestEncryptKey, EncryptProvider.privateKeyBase64());
                        body.setData(Cryptos.aesECBEncryptBase64String(data, key));
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                        throw new RuntimeException(e);
                    }
                }

            }else if(CipherMode.BASE64.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        body.setData(EncodeUtils.base64Encode(data.getBytes(StandardCharsets.UTF_8)));
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return body;
    }
}