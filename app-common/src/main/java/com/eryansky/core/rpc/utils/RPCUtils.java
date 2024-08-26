package com.eryansky.core.rpc.utils;

import com.eryansky.client.common.rpc.RPCExchange;
import com.eryansky.client.common.rpc.RPCMethodConfig;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.core.rpc.consumer.ConsumerExecutor;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Map;

public class RPCUtils {
    public static final String AUTH_TYPE = "apiKey";
    public static final String HEADER_API_SERVICE_NAME = "Api-Service-Name";
    public static final String HEADER_API_SERVICE_METHOD = "Api-Service-method";
    public static final String HEADER_AUTH_TYPE = "Auth-Type";
    public static final String HEADER_X_API_KEY = "X-Api-Key";


    public static <T> T createProxyObj(String serverUrl, Class clazz) {
        if (!clazz.isInterface()) { // 接口才可以进行代理
            throw new IllegalArgumentException(clazz + " is not a interface!");
        }
        return (T) Enhancer.create(clazz, (MethodInterceptor) (o, method, objects, methodProxy) -> {
            // 获取服务发布接口上的注解
            RPCExchange annotation = (RPCExchange) clazz.getAnnotation(RPCExchange.class);
            String appName = annotation.name();
            StringBuilder url = new StringBuilder();
            url.append(serverUrl).append(annotation.urlPrefix()).append("/").append(appName).append("/");
            RPCMethodConfig methodAnnotation = method.getAnnotation(RPCMethodConfig.class);
            String requestMethodName = method.getName();
            if (methodAnnotation != null && StringUtils.hasLength(methodAnnotation.alias())) {
                requestMethodName = methodAnnotation.alias();
            }
            url.append(requestMethodName);
            Type returnType = method.getGenericReturnType();
            ParameterizedTypeReference<T> reference = ParameterizedTypeReference.forType(returnType);

            Map<String,String> headers = Maps.newHashMap();
            headers.put(HEADER_API_SERVICE_NAME,annotation.name());
            headers.put(HEADER_API_SERVICE_METHOD,requestMethodName);
            headers.put(HEADER_AUTH_TYPE,AUTH_TYPE);
            headers.put(HEADER_X_API_KEY, StringUtils.hasLength(annotation.apiKey()) ? resolve(null,annotation.apiKey()):AppConstants.getRPCClientApiKey());
            // 由于当前接口在服务消费方并没有实现类，不能对实现类增强，可以增加一个统一的切入点执行逻辑
            return  ConsumerExecutor.execute(url.toString(),headers, objects, reference);
        });

    }


    public static String resolve(ConfigurableBeanFactory beanFactory, String value) {
        if (StringUtils.hasText(value)) {
            if (beanFactory == null) {
                return SpringContextHolder.getApplicationContext().getEnvironment().resolvePlaceholders(value);
            }
            BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
            String resolved = beanFactory.resolveEmbeddedValue(value);
            if (resolver == null) {
                return resolved;
            }
            Object evaluateValue = resolver.evaluate(resolved, new BeanExpressionContext(beanFactory, null));
            if (evaluateValue != null) {
                return String.valueOf(evaluateValue);
            }
            return null;
        }
        return value;
    }

}
