package com.eryansky.core.rpc.consumer;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.config.RestTemplateHolder;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.rpc.utils.SerializerFactory;
import com.eryansky.encrypt.enums.CipherMode;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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
        String serializer = headers.get(RPCUtils.HEADER_RPC_SERIALIZER);

        if (StringUtils.isNotBlank(requestEncrypt)){
//            responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);//多一层引号““””
//            responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Serializable.class);

            responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, byte[].class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("RPC请求异常：{} {} {}", url, responseEntity.getStatusCode().value(), responseEntity.getBody());
                throw new RuntimeException("RPC请求异常：" + url + " " + responseEntity.getStatusCode().value()+" "+ JsonMapper.toJsonString(responseEntity));
            }

            byte[] data = null;
            Object body = responseEntity.getBody();
            try {
                data = (byte[]) body;
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                log.error("RPC请求异常：{} {} {}", responseEntity.getStatusCode().value(),url,JsonMapper.toJsonString(responseEntity));
                throw new RuntimeException("RPC请求异常：" + url + " " + responseEntity.getStatusCode().value() +" "+ JsonMapper.toJsonString(responseEntity));
            }

            if(CipherMode.SM4.name().equals(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey)){
                try {
                    String key = null;
                    try {
                        key = RSAUtils.decryptHexString(requestEncryptKey);
                    } catch (Exception e) {
                        key = requestEncryptKey;
                    }
                    return (T) SerializerFactory.getSerializer(serializer).deserialize(Sm4Utils.decrypt(key,data));
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    log.error("RPC请求异常：{} {} {}", responseEntity.getStatusCode().value(),url,JsonMapper.toJsonString(responseEntity));
                    throw new RuntimeException(e);
                }

            }else if(CipherMode.AES.name().equals(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey)){
                try {
                    String key = null;
                    try {
                        key = RSAUtils.decryptBase64String(requestEncryptKey);
                    } catch (Exception e) {
                        key = requestEncryptKey;
                    }
                    return (T) SerializerFactory.getSerializer(serializer).deserialize(Cryptos.aesECBDecrypt(data,key));
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    log.error("RPC请求异常：{} {} {}", responseEntity.getStatusCode().value(),url,JsonMapper.toJsonString(responseEntity));
                    throw new RuntimeException(e);
                }

            }else if(CipherMode.BASE64.name().equals(requestEncrypt)){
                try {
                    return (T) SerializerFactory.getSerializer(serializer).deserialize(Base64.decodeBase64(data));
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    log.error("RPC请求异常：{} {} {}", responseEntity.getStatusCode().value(),url,JsonMapper.toJsonString(responseEntity));
                    throw new RuntimeException(e);
                }

            }
        }else{
            //未加密
            try {
                responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, responseType);
            }catch (Exception exception){
                log.error(exception.getMessage());
                log.warn("RPC请求异常：{} {} {}", url, responseEntity.getStatusCode().value(), JsonMapper.toJsonString(responseEntity));

                //支持范型
                JavaType javaType = jsonMapper.getTypeFactory().constructType(responseType.getType());
                responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Object.class);
                String json = (String)responseEntity.getBody();
                try {
                    return jsonMapper.toJavaObject(json,javaType);
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    log.error("RPC请求异常：{} {} {}", responseEntity.getStatusCode().value(),url,json);
                    throw new RuntimeException(e);
                }
            }


        }
        if(log.isDebugEnabled()){
            log.debug(JsonMapper.toJsonString(headers));
            log.debug(JsonMapper.toJsonString(responseEntity.getHeaders()));
        }
        if(!HttpStatus.OK.equals(responseEntity.getStatusCode())){
            log.error("RPC请求异常：{} {} {}",url,responseEntity.getStatusCode(),JsonMapper.toJsonString(responseEntity.getBody()));
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
        if(null != headers){
            headers.forEach(httpHeaders::add);
        }
        //加密处理
        String encrypt = headers.get(RPCUtils.HEADER_ENCRYPT);
        String serializer = headers.get(RPCUtils.HEADER_RPC_SERIALIZER);
        if (StringUtils.isNotBlank(encrypt)){
            httpHeaders.setContentType(MediaType.parseMediaType("application/x-"+serializer+"-secure"));
            httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON,MediaType.parseMediaType("application/x-"+serializer+"-secure")));
        }else {
//            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setContentType(MediaType.parseMediaType("application/x-"+serializer));
            httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON,MediaType.parseMediaType("application/x-"+serializer)));
        }

        String encryptKey =  null;
        String key = null;
        if (StringUtils.isNotBlank(encrypt)){
            if(CipherMode.SM4.name().equals(encrypt)){
                key = Sm4Utils.generateHexKeyString();
                encryptKey = RSAUtils.encryptHexString(key);
            }else if(CipherMode.AES.name().equals(encrypt)){
                key = Cryptos.getBase64EncodeKey();
                encryptKey = RSAUtils.encryptBase64String(key);
            }
            headers.put(RPCUtils.HEADER_ENCRYPT_KEY, encryptKey);
            httpHeaders.put(RPCUtils.HEADER_ENCRYPT_KEY, Lists.newArrayList(encryptKey));
        }
        byte[] bytes = SerializerFactory.getSerializer(serializer).serialize(params);
        byte[] data = bytes;
        if (StringUtils.isNotBlank(encrypt)){
            if(CipherMode.SM4.name().equals(encrypt)){
                data = Sm4Utils.encrypt(key, bytes);
            }else if(CipherMode.AES.name().equals(encrypt)){
                data = Cryptos.aesECBEncrypt(bytes, key);
            }else if(CipherMode.BASE64.name().equals(encrypt)){
                data = Base64.encodeBase64(bytes);
            }
        }
        return new HttpEntity<>(data, httpHeaders);
    }
}
