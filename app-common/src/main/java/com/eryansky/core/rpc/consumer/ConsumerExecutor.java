package com.eryansky.core.rpc.consumer;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.http.HttpCompoents;
import com.eryansky.common.utils.http.HttpPoolCompoents;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.encrypt.enums.CipherMode;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;

public class ConsumerExecutor {

    private static final Logger log = LoggerFactory.getLogger(ConsumerExecutor.class);
    private static final JsonMapper jsonMapper = JsonMapper.getInstance().enable(INCLUDE_SOURCE_IN_LOCATION);
    private static final HttpPoolCompoents httpCompoents = HttpPoolCompoents.getInstance();

    public static <T> T  execute(String url, Map<String,String> headers, Object[] params, ParameterizedTypeReference responseType) throws Exception {
        // 获取RestTemplate对象
        // 构建请求体
        String httpEntity = createHttpBody(params,headers);
        // 返回接口 数据解密
        String requestEncrypt = headers.get(RPCUtils.HEADER_ENCRYPT);
        String requestEncryptKey = headers.get(RPCUtils.HEADER_ENCRYPT_KEY);
        String data = httpCompoents.post(url,httpEntity,headers,StandardCharsets.UTF_8.name());
        if(StringUtils.isBlank(data)){
            return null;
        }
        String rData = data;
        JavaType javaType = jsonMapper.getTypeFactory().constructType(responseType.getType());
        if (StringUtils.isNotBlank(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey) ){
            if(CipherMode.SM4.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        String key = RSAUtils.decryptHexString(requestEncryptKey);
                        rData = Sm4Utils.decrypt(key,StringUtils.startsWith(data,"\"") ? data.substring(1,data.length()-1):data);
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                        throw new RuntimeException(e);
                    }
                }
            }else if(CipherMode.AES.name().equals(requestEncrypt)){
                if(StringUtils.isNotBlank(data) && !StringUtils.equals(data,"null")){
                    try {
                        String key = RSAUtils.decryptBase64String(requestEncryptKey);
                        rData = Cryptos.aesECBDecryptBase64String(data,key);
                    } catch (Exception e) {
                        log.error(e.getMessage(),e);
                        throw new RuntimeException(e);
                    }
                }

            }
        }
        if(StringUtils.isBlank(rData)){
            return null;
        }
        return jsonMapper.toJavaObject(rData,javaType);
    }

    /**
     * 构建请求体，默认是JSON数组
     *
     * @param params
     * @return
     */
    private static String createHttpBody(Object[] params, Map<String,String> headers) throws Exception {
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
            }
            return data;
        } else {
            return StringUtils.EMPTY;
        }
    }

}
