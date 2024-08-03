package com.eryansky.core.rpc.client;

import com.eryansky.core.rpc.CustomRpcApp;
import com.eryansky.core.rpc.CustomRpcMethodConfig;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxyUtils {

    public static <T> T createProxyObj(Class clazz) {
        if (!clazz.isInterface()) { // 接口才可以进行代理
            throw new IllegalArgumentException(clazz + " is not a interface!");
        }
        return (T) Proxy.newProxyInstance(RpcProxyUtils.class.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            // 获取服务发布接口上的@CustomRpcApp注解
            CustomRpcApp annotation = (CustomRpcApp) clazz.getAnnotation(CustomRpcApp.class);
            // 获取到@CustomRpcApp注解相关属性拼接出url
            String appName = annotation.name();
            String contentPath = annotation.contentPath();
            StringBuilder urlSB = new StringBuilder().append("/");
            if (StringUtils.hasLength(contentPath)) {
                urlSB.append(contentPath).append("/");
            }
            urlSB.append(clazz.getSimpleName().toLowerCase()).append("/");
            CustomRpcMethodConfig customRpcMethodConfig = method.getAnnotation(CustomRpcMethodConfig.class);
            // // 获取到@CustomRpcMethodConfig注解相关属性拼接出url
            if (customRpcMethodConfig != null && StringUtils.hasLength(customRpcMethodConfig.alias())) {
                urlSB.append(customRpcMethodConfig.alias());
            } else {
                urlSB.append(method.getName());
            }
            String url = urlSB.toString();
            // 由于当前接口在服务消费方并没有实现类，不能对实现类增强，可以增加一个统一的切入点执行逻辑
            return RpcConsumerExecutor.execute(appName + url, args, method.getReturnType());
        });
    }

}
