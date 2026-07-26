package com.eryansky.core.rpc.provider;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.rpc.advice.EncryptRPCResponseBodyAdvice;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.rpc.utils.SerializerFactory;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

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

        byte[] data = StreamUtils.copyToByteArray(request.getInputStream());
        
        if (StringUtils.isNotBlank(encrypt)) {
            data = RPCUtils.decryptDataByRequest(encrypt, encryptKey, data);
        }

        Object[] params = (Object[]) SerializerFactory.getSerializer(serializer).deserialize(data);
        return execute(rpcService, methodName, params);
    }


    /**
     * 执行方法
     */
    private Object execute(String rpcService, String methodName, Object[] params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ProviderHolder.ProviderInfo providerInfo = ProviderHolder.RPC_PROVIDER_MAP.get(rpcService);
        if (providerInfo == null) {
            log.warn("RPC service not found: {}", rpcService);
            return null;
        }

        Object rpcBean = providerInfo.getRpcBean();
        List<ProviderHolder.RPCMethod> urlCoreMethod = providerInfo.getUrlCoreMethod();
        
        return urlCoreMethod.stream()
                .filter(rm -> rm.getAlias().equals(methodName))
                .findFirst()
                .map(rm -> {
                    try {
                        return rm.getMethod().invoke(rpcBean, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Failed to invoke method: {}", methodName, e);
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);
    }
}
