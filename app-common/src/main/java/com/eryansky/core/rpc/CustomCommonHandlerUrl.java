package com.eryansky.core.rpc;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomCommonHandlerUrl {

    public static final Method HANDLE_CUSTOM_URL_METHOD;

    static {
        // 提前准备方法对象
        Method tempMethod = null;
        try {
            tempMethod = CustomCommonHandlerUrl.class.getMethod("handlerCustomUrl", HttpServletRequest.class, HttpServletResponse.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        HANDLE_CUSTOM_URL_METHOD = tempMethod;
    }

    @ResponseBody
    /**
     *  拦截自定义请求的url，可以做成统一的处理器
     */
    public Object handlerCustomUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 解析请求url
        List<String> pathSegments = UriComponentsBuilder.fromUriString(request.getRequestURI()).build().getPathSegments();
        String rpcService = null;
        String methodName = null;
        // url默认格式是 接口名称/方法名称
        if (pathSegments.size() == 2) {
            rpcService = pathSegments.get(0);
            methodName = pathSegments.get(1);
        } else if (pathSegments.size() == 3) { // 可能配置了contentpath，这里偷懒简单判断一下
            rpcService = pathSegments.get(1);
            methodName = pathSegments.get(2);
        }
        // 获取请求体
        String requestBodyJsonString = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        // 解析参数
        Object[] params = resolveParams(requestBodyJsonString, rpcService, methodName);
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
    private Object execute(String rpcService, String methodName, Object[] params) throws InvocationTargetException, IllegalAccessException {
        // 获取RpcProvider的相关信息
        RpcProviderHolder.RpcProviderInfo rpcProviderInfo = RpcProviderHolder.RPC_PROVIDER_MAP.get(rpcService);
        Object rpcBean = rpcProviderInfo.getRpcBean();
        List<RpcProviderHolder.RpcMethod> urlCoreMethod = rpcProviderInfo.getUrlCoreMethod();
        for (RpcProviderHolder.RpcMethod rm : urlCoreMethod) {
            if (rm.getAlias().equals(methodName)) {
                return rm.getMethod().invoke(rpcBean, params); // 找到该方法，然后执行
            }
        }
        return null;
    }

    /**
     * 解析参数
     *
     * @param requestBodyJsonString
     * @param rpcService
     * @param methodName
     * @return
     */
    private Object[] resolveParams(String requestBodyJsonString, String rpcService, String methodName) {
        // 如果没有请求体，参数直接返回null
        if (!StringUtils.hasLength(requestBodyJsonString)) {
            return null;
        }
        List<Object> paramList = new ArrayList<>();
        // 判断当前需要调用的RPCProvider是否存在
        RpcProviderHolder.RpcProviderInfo rpcProviderInfo = RpcProviderHolder.RPC_PROVIDER_MAP.get(rpcService);
        if (rpcProviderInfo == null) {
            throw new RuntimeException("no service : " + rpcService);
        }
        // 解析参数，默认是JSON数组 TODO
        List<Object> objects = JsonMapper.fromJsonForObjectList(requestBodyJsonString);
        List<RpcProviderHolder.RpcMethod> urlCoreMethod = rpcProviderInfo.getUrlCoreMethod();
        if (!CollectionUtils.isEmpty(urlCoreMethod)) {
            for (RpcProviderHolder.RpcMethod rm : urlCoreMethod) { // 寻找当前请求对应的需要执行的方法信息
                if (rm.getAlias().equals(methodName)) {
                    Class<?>[] parameterTypes = rm.getMethod().getParameterTypes();
                    if (objects.size() != parameterTypes.length) { // 判断方法参数和方法对象中的参数个数是否匹配
                        throw new RuntimeException(rpcService + " method : " + methodName + " match error!");
                    }
                    for (int i = 0; i < objects.size(); i++) { // 通过参数类型去解析参数，并保存到list中进行返回，后续执行真正的调用
                        Object obj = objects.get(i);
                        if (obj instanceof JsonNode) {
                            Object parse = JsonMapper.getInstance().toJavaObject(parameterTypes[i], parameterTypes[i].getClass());
                            paramList.add(parse);
                        } else {
                            paramList.add(obj);
                        }
                    }
                    return paramList.toArray();
                }
            }
            throw new RuntimeException(rpcService + "no method : " + methodName);
        } else {
            throw new RuntimeException(rpcService + "no method : " + methodName);
        }
    }
}
