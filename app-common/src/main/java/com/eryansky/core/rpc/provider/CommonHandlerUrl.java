package com.eryansky.core.rpc.provider;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.security.annotation.RestApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;

public class CommonHandlerUrl {

    private static final Logger log = LoggerFactory.getLogger(CommonHandlerUrl.class);

    public static final Method HANDLE_CUSTOM_URL_METHOD;

    public static final JsonMapper jsonmapper = new JsonMapper().enable(INCLUDE_SOURCE_IN_LOCATION);

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
    @ResponseBody
    /**
     *  拦截自定义请求的url，可以做成统一的处理器
     */
    public Object handlerUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 解析请求url
        List<String> pathSegments = UriComponentsBuilder.fromUriString(request.getRequestURI()).build().getPathSegments();
        String rpcService = null;
        String methodName = null;
        // url默认格式是 接口名称/方法名称
        if (pathSegments.size() == 3) {
            rpcService = pathSegments.get(1);
            methodName = pathSegments.get(2);
        } else if (pathSegments.size() == 4) {
            rpcService = pathSegments.get(2);
            methodName = pathSegments.get(3);
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
        ProviderHolder.ProviderInfo providerInfo = ProviderHolder.RPC_PROVIDER_MAP.get(rpcService);
        Object rpcBean = providerInfo.getRpcBean();
        List<ProviderHolder.RPCMethod> urlCoreMethod = providerInfo.getUrlCoreMethod();
        for (ProviderHolder.RPCMethod rm : urlCoreMethod) {
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
    private Object[] resolveParams(String requestBodyJsonString, String rpcService, String methodName) throws ClassNotFoundException {
        // 如果没有请求体，参数直接返回null
        if (!StringUtils.hasLength(requestBodyJsonString)) {
            return null;
        }
        List<Object> paramList = new ArrayList<>();
        // 判断当前需要调用的RPCProvider是否存在
        ProviderHolder.ProviderInfo providerInfo = ProviderHolder.RPC_PROVIDER_MAP.get(rpcService);
        if (providerInfo == null) {
            throw new RuntimeException("no service : " + rpcService);
        }
        // 解析参数，默认是JSON数组
        ArrayNode objects = jsonmapper.toArrayNode(requestBodyJsonString);
        List<ProviderHolder.RPCMethod> urlCoreMethod = providerInfo.getUrlCoreMethod();
        if (!CollectionUtils.isEmpty(urlCoreMethod)) {
            for (ProviderHolder.RPCMethod rm : urlCoreMethod) { // 寻找当前请求对应的需要执行的方法信息
                if (rm.getAlias().equals(methodName)) {
                    Type[] genericParameterTypes = rm.getMethod().getGenericParameterTypes();
                    if (objects.size() != genericParameterTypes.length) { // 判断方法参数和方法对象中的参数个数是否匹配
                        throw new RuntimeException(rpcService + " method : " + methodName + " match error!");
                    }
                    for (int i = 0; i < objects.size(); i++) { // 通过参数类型去解析参数，并保存到list中进行返回，后续执行真正的调用
                        JsonNode obj = objects.get(i);
                        Object parse = null;
                        if(null != obj){
                            parse = jsonmapper.toJavaObject(obj, jsonmapper.getTypeFactory().constructType(genericParameterTypes[i]));
                        }
                        paramList.add(parse);
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
