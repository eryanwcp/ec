package com.eryansky.encrypt.advice;

import com.eryansky.common.model.Result;
import com.eryansky.common.orm.mybatis.sensitive.encrypt.AesSupport;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.RSAUtil;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.enums.CipherMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


@RestControllerAdvice
public class EncryptResultResponseBodyAdvice implements ResponseBodyAdvice<Result> {

    private static final Logger log = LoggerFactory.getLogger(EncryptResultResponseBodyAdvice.class);

    public static String  ENCRYPT = "encrypt";
    public static String  ENCRYPT_KEY = "encrypt-key";

    @Override  
    public boolean supports(MethodParameter returnType, Class converterType) {
        EncryptResponseBody encrypt = returnType.getMethodAnnotation(EncryptResponseBody.class);
        //如果带有注解且标记为验签，测进行验签操作
        return null != encrypt;
    }  

    @Override  
    public Result beforeBodyWrite(Result body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        String requestEncrypt = Collections3.getFirst(headers.get(ENCRYPT));
        String requestEncryptKey = Collections3.getFirst(headers.get(ENCRYPT_KEY));
        if (StringUtils.isNotBlank(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey) ){
            String key = RSAUtil.decryptBase64(requestEncryptKey);
            String data = JsonMapper.toJsonString(body.getData());
            String obj = JsonMapper.toJsonString(body.getObj());
            if(CipherMode.SM4.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        body.setData(Sm4Utils.encrypt(key,data));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if(StringUtils.isNotBlank(obj) && !StringUtils.equals(obj,"null")){
                    try {
                        body.setObj(Sm4Utils.encrypt(key,obj));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }else if(CipherMode.AES.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        body.setData(new AesSupport(key).encryptECB(data));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if(StringUtils.isNotBlank(obj) && !StringUtils.equals(obj,"null")){
                    try {
                        body.setObj(new AesSupport(key).encryptECB(obj));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return body;  
    }

}