package com.eryansky.core.rpc.consumer;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.config.RestTemplateHolder;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.encrypt.enums.CipherMode;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConsumerExecutor {

    private static final Logger log = LoggerFactory.getLogger(ConsumerExecutor.class);
    private static final JsonMapper jsonMapper = JsonMapper.getInstance();

    public static <T> T  execute(String url, Map<String,String> headers, Object[] params, ParameterizedTypeReference responseType) throws Exception {
        // 获取RestTemplate对象
        RestTemplate restTemplate = RestTemplateHolder.restTemplate();
        // 构建请求体
        HttpEntity<?> httpEntity = createHttpEntity(params,headers);
        // 进行远程rpc请求
        ResponseEntity responseEntity = null;
        // 返回接口 数据解密
        String requestEncrypt = headers.get(RPCUtils.HEADER_ENCRYPT);
        String requestEncryptKey = headers.get(RPCUtils.HEADER_ENCRYPT_KEY);
        if (StringUtils.isNotBlank(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey) ){
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            String data = (String) responseEntity.getBody();
            JavaType javaType = jsonMapper.getTypeFactory().constructType(responseType.getType());
            if(CipherMode.SM4.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        String key = RSAUtils.decryptHexString(requestEncryptKey);
                        return jsonMapper.toJavaObject(Sm4Utils.decrypt(key,data),javaType);
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                        throw new RuntimeException(e);
                    }
                }
            }else if(CipherMode.AES.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        String key = RSAUtils.decryptBase64String(requestEncryptKey);
                        return jsonMapper.toJavaObject(Cryptos.aesECBDecryptBase64String(data,key),javaType);
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                        throw new RuntimeException(e);
                    }
                }

            }
        }else{
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, responseType);
        }
        if(!HttpStatus.OK.equals(responseEntity.getStatusCode())){
            log.error("{}",JsonMapper.toJsonString(responseEntity.getBody()));
        }
        return (T) responseEntity.getBody();
    }

    /**
     * 构建请求体，默认是JSON数组
     *
     * @param params
     * @return
     */
    private static HttpEntity<?> createHttpEntity(Object[] params, Map<String,String> headers) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if(null != headers){
            headers.forEach(httpHeaders::add);
        }

        if (params != null && params.length != 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int i = 0; i < params.length; i++) {
                builder.append(jsonMapper.toJson(params[i]));
                if (i != params.length - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");

            //加密处理
            String encrypt = headers.get(RPCUtils.HEADER_ENCRYPT);
            String encryptKey =  null;
            String data = builder.toString();
            if (StringUtils.isNotBlank(encrypt)){
                if(CipherMode.SM4.name().equals(encrypt)){
                    String key = Sm4Utils.generateHexKeyString();
                    encryptKey = RSAUtils.encryptHexString(key);
                    data = Sm4Utils.encrypt(key, builder.toString());
                }else if(CipherMode.AES.name().equals(encrypt)){
                    String key = Cryptos.getBase64EncodeKey();
                    encryptKey = RSAUtils.encryptBase64String(key);
                    data = Cryptos.aesECBEncryptBase64String(builder.toString(),key);
                }
                headers.put(RPCUtils.HEADER_ENCRYPT_KEY, encryptKey);
                httpHeaders.put(RPCUtils.HEADER_ENCRYPT_KEY, Lists.newArrayList(encryptKey));
            }
            return new HttpEntity<>(data.getBytes(StandardCharsets.UTF_8), httpHeaders);
        } else {
            return new HttpEntity<>(httpHeaders);
        }
    }
}
