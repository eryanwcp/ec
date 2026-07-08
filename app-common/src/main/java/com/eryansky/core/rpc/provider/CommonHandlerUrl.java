package com.eryansky.core.rpc.provider;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.core.rpc.advice.EncryptRPCResponseBodyAdvice;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.rpc.utils.SerializerFactory;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.enums.CipherMode;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommonHandlerUrl {

    private static final Logger log = LoggerFactory.getLogger(CommonHandlerUrl.class);

    public static final Method HANDLE_CUSTOM_URL_METHOD;


    static {
        // 提前准备方法对象
        Method tempMethod = null;
        try {
            tempMethod = CommonHandlerUrl.class.getMethod("handlerUrl", HttpServletRequest.class, HttpServletResponse.class);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(),e);
        }
        HANDLE_CUSTOM_URL_METHOD = tempMethod;
    }

    @RestApi
    @EncryptResponseBody(defaultHandle = false,handle = EncryptRPCResponseBodyAdvice.HANDLE)
    @ResponseBody
    /**
     *  拦截自定义请求的url，可以做成统一的处理器
     */
    public Object handlerUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String rpcService = request.getHeader(RPCUtils.HEADER_API_SERVICE_NAME);
        String methodName = request.getHeader(RPCUtils.HEADER_API_SERVICE_METHOD);
        String encrypt = request.getHeader(RPCUtils.HEADER_ENCRYPT);
        String encryptKey = request.getHeader(RPCUtils.HEADER_ENCRYPT_KEY);
        String serializer = request.getHeader(RPCUtils.HEADER_RPC_SERIALIZER);
        // 获取请求体
        byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
        byte[] data = requestBody;
        //请求体解密
        if (StringUtils.isNotBlank(encrypt)){
            String key = null;
            if(CipherMode.SM4.name().equals(encrypt) && StringUtils.isNotBlank(encryptKey) ){
                try {
                    key = RSAUtils.decryptHexString(encryptKey);
                } catch (Exception e) {
                    key = encryptKey;
                }
                data = Sm4Utils.decrypt(key, requestBody);
            }else if(CipherMode.AES.name().equals(encrypt) && StringUtils.isNotBlank(encryptKey) ){
                try {
                    key = RSAUtils.decryptBase64String(encryptKey);
                } catch (Exception e) {
                    key = encryptKey;
                }
                data = Cryptos.aesECBDecrypt(requestBody, key);
            }else if(CipherMode.BASE64.name().equals(encrypt)){
                data = Base64.decodeBase64(requestBody);
            }
        }

        // 解析参数
        Object[] params = (Object[]) SerializerFactory.getSerializer(serializer).deserialize(data);
        // 执行方法
        return execute(rpcService, methodName, params);
    }

    /**
     * 执行方法
     *
     * @param rpcService
     * @param methodName
     * @param params
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object execute(String rpcService, String methodName, Object[] params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // 获取RpcProvider的相关信息
        ProviderHolder.ProviderInfo providerInfo = ProviderHolder.RPC_PROVIDER_MAP.get(rpcService);
        Object rpcBean = providerInfo.getRpcBean();
        List<ProviderHolder.RPCMethod> urlCoreMethod = providerInfo.getUrlCoreMethod();
        for (ProviderHolder.RPCMethod rm : urlCoreMethod) {
            if (rm.getAlias().equals(methodName)) {
                return rm.getMethod().invoke(rpcBean, params);
            }
        }
        return null;
    }
}
