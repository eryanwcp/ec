package com.eryansky.core.rpc.consumer;

import com.eryansky.client.common.rpc.RPCExchange;
import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.model.R;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.rpc.provider.ProviderHolder;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Type;
import java.util.*;

public class EcServiceClient {

    private static final Logger log = LoggerFactory.getLogger(EcServiceClient.class);

    private String serviceName;

    private String serviceMethod;

    public void init(String serviceName, String serviceMethod) {
        this.serviceMethod = serviceMethod;
        this.serviceName = serviceName;
    }


    public R callService(EcServiceContext ecServiceContext, Object... params) {
        if (StringUtils.isBlank(this.serviceName)) {
            throw new ServiceException("serviceName not allow null");
        }
        if (StringUtils.isBlank(this.serviceMethod)) {
            throw new ServiceException("methodName not allow null");
        }


        R r;
        boolean isLocalService = ProviderHolder.RPC_PROVIDER_MAP.containsKey(serviceName);
        if (isLocalService) {//本地微服务
            try {
                ProviderHolder.ProviderInfo providerInfo = ProviderHolder.RPC_PROVIDER_MAP.get(serviceName);
                Object rpcBean = providerInfo.getRpcBean();
                List<ProviderHolder.RPCMethod> urlCoreMethod = providerInfo.getUrlCoreMethod();
                ProviderHolder.RPCMethod rm = urlCoreMethod.stream().filter(v -> v.getAlias().equals(serviceMethod)).findFirst().orElse(null);

//                Boolean isPermitted = SecurityUtils.isPermitted(providerInfo.getRpcBean().getClass(), rm.getMethod());
                Boolean isPermitted = RPCUtils.isPermitted(rm.getMethod().getDeclaringClass(), rm.getMethod());
                if (null != isPermitted && !isPermitted) {
                    r = R.rest(false).setMsg("未授权或会话信息已失效！");
                    return r;
                }

                r = new R().setCode(R.SUCCESS).setData(rm.getMethod().invoke(rpcBean, params));
            } catch (Exception e) {
                String message = e.getMessage();
                r = R.rest(false).setMsg(message);
            }
        } else {//远程微服务
            ConsumerHolder.ConsumerInfo consumerInfo = ConsumerHolder.RPC_CONSUMER_MAP.get(serviceName);
            String serverUrl = consumerInfo.getServerUrl();

            ConsumerHolder.RPCMethod rm = consumerInfo.getUrlCoreMethod().stream().filter(v -> v.getAlias().equals(serviceMethod)).findFirst().orElse(null);

            Boolean isPermitted = RPCUtils.isPermitted(rm.getClass(), rm.getMethod());
            if (null != isPermitted && !isPermitted) {
                r = R.rest(false).setMsg("未授权或会话信息已失效！");
                return r;
            }

            Type returnType = rm.getMethod().getGenericReturnType();
            ParameterizedTypeReference reference = ParameterizedTypeReference.forType(returnType);
            Map<String, String> headers = Maps.newHashMap();
            headers.put(RPCUtils.HEADER_API_SERVICE_NAME, serviceName);
            headers.put(RPCUtils.HEADER_API_SERVICE_METHOD, serviceMethod);
            headers.put(RPCUtils.HEADER_AUTH_TYPE, RPCUtils.AUTH_TYPE);
            headers.put(RPCUtils.HEADER_X_API_KEY, org.springframework.util.StringUtils.hasLength(consumerInfo.getApiKey()) ? consumerInfo.getApiKey() : AppConstants.getRPCClientApiKey());

            //加密参数 加密方式
            String encrypt = StringUtils.isNotBlank(rm.getEncrypt()) ? rm.getEncrypt() : consumerInfo.getEncrypt();
            if (org.springframework.util.StringUtils.hasLength(encrypt) && !RPCExchange.ENCRYPT_NONE.equals(encrypt)) {
                headers.put(RPCUtils.HEADER_ENCRYPT, encrypt);
                log.debug("RPC服务传输数据加密：{} {}", serverUrl, encrypt);
            }

            StringBuilder url = new StringBuilder();
            url.append(serverUrl).append(consumerInfo.getUrlPrefix()).append("/").append(consumerInfo.getName()).append("/").append(serviceMethod);


            try {
                // 由于当前接口在服务消费方并没有实现类，不能对实现类增强，可以增加一个统一的切入点执行逻辑
                Object obj = ConsumerExecutor.execute(url.toString(), headers, params, reference);
                r = new R().setCode(R.SUCCESS).setData(obj);
            } catch (Exception e) {
                String message = e.getMessage();
                r = R.rest(false).setMsg(message);
            }
        }
        return r;
    }
}
