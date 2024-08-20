package com.eryansky.core.rpc.utils;

import com.eryansky.core.rpc.annotation.RPCApp;
import com.eryansky.core.rpc.annotation.RPCMethodConfig;
import com.eryansky.core.rpc.consumer.ConsumerExecutor;
import org.springframework.util.StringUtils;

import java.lang.reflect.Proxy;

public class RPCProxyUtils {

    public static <T> T createProxyObj(String serverUrl,Class clazz) {
        if (!clazz.isInterface()) { // 接口才可以进行代理
            throw new IllegalArgumentException(clazz + " is not a interface!");
        }
        return (T) Proxy.newProxyInstance(RPCProxyUtils.class.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            // 获取服务发布接口上的@CustomRpcApp注解
            RPCApp annotation = (RPCApp) clazz.getAnnotation(RPCApp.class);
            // 获取到@CustomRpcApp注解相关属性拼接出url
            String appName = annotation.name();
            StringBuilder url = new StringBuilder();
            url.append(serverUrl).append(annotation.urlPrefix()).append("/").append(appName).append("/");
            RPCMethodConfig RPCMethodConfig = method.getAnnotation(RPCMethodConfig.class);
            // // 获取到@CustomRpcMethodConfig注解相关属性拼接出url
            if (RPCMethodConfig != null && StringUtils.hasLength(RPCMethodConfig.alias())) {
                url.append(RPCMethodConfig.alias());
            } else {
                url.append(method.getName());
            }
            // 由于当前接口在服务消费方并没有实现类，不能对实现类增强，可以增加一个统一的切入点执行逻辑
            return ConsumerExecutor.execute(url.toString(), args, method.getReturnType());
        });
    }

}
