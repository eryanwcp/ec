package com.eryansky.core.rpc.advice;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
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

/**
 * 默认加密策略 返回值为
 */
@RestControllerAdvice
public class EncryptRPCResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(EncryptRPCResponseBodyAdvice.class);

    public static final String ENCRYPT = "Encrypt";
    public static final String ENCRYPT_KEY = "Encrypt-Key";
    public static final String HANDLE = "RPC";
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        EncryptResponseBody encrypt = returnType.getMethodAnnotation(EncryptResponseBody.class);
        //如果带有注解且标记为验签，则进行验签操作
        return null != encrypt && !encrypt.defaultHandle() && HANDLE.equals(encrypt.handle());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        String requestEncrypt = Collections3.getFirst(headers.get(ENCRYPT));
        String requestEncryptKey = Collections3.getFirst(headers.get(ENCRYPT_KEY));
        if (StringUtils.isNotBlank(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey)) {
            String data = JsonMapper.toJsonString(body);
            if (CipherMode.SM4.name().equals(requestEncrypt)) {
                if (StringUtils.isNotBlank(data) && !StringUtils.equals(data, "null")) {
                    try {
                        return Sm4Utils.encrypt(RSAUtils.decryptHexString(requestEncryptKey), data);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            } else if (CipherMode.AES.name().equals(requestEncrypt)) {
                if (StringUtils.isNotBlank(data) && !StringUtils.equals(data, "null")) {
                    try {
                        return Cryptos.aesECBEncryptBase64String(data, RSAUtils.decryptBase64String(requestEncryptKey));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }

            }
        }

        return body;
    }

}