package com.eryansky.core.rpc.consumer;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.config.RestTemplateHolder;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.rpc.utils.SerializerFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Lists;
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
        // 返回接口 数据解密
        String requestEncrypt = headers.get(RPCUtils.HEADER_ENCRYPT);
        String serializer = headers.get(RPCUtils.HEADER_RPC_SERIALIZER);
        RPCUtils.EncryptRequestKey encryptRequestKey = RPCUtils.generateEncryptKey(requestEncrypt);
        // 构建请求体
        HttpEntity<?> httpEntity = createHttpEntity(params,headers,encryptRequestKey);

        // 进行远程rpc请求
        ResponseEntity responseEntity = null;


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

            if(StringUtils.isNotBlank(requestEncrypt) && data != null && data.length > 0){
                return (T) SerializerFactory.getSerializer(serializer).deserialize(RPCUtils.decryptData(requestEncrypt,encryptRequestKey.getKey(),data));
            }else {
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
    private static HttpEntity<?> createHttpEntity(Object[] params, Map<String,String> headers, RPCUtils.EncryptRequestKey encryptRequestKey) throws Exception {
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

        if (StringUtils.isNotBlank(encryptRequestKey.getEncryptKey())){
            headers.put(RPCUtils.HEADER_ENCRYPT_KEY, encryptRequestKey.getEncryptKey());
            httpHeaders.put(RPCUtils.HEADER_ENCRYPT_KEY, Lists.newArrayList(encryptRequestKey.getEncryptKey()));
        }
        byte[] bytes = SerializerFactory.getSerializer(serializer).serialize(params);
        byte[] data = RPCUtils.encryptData(encrypt, encryptRequestKey.getKey(), bytes);
        return new HttpEntity<>(data, httpHeaders);
    }
}
