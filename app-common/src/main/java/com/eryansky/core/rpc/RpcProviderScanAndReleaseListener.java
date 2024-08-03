package com.eryansky.core.rpc;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RpcProviderScanAndReleaseListener implements ApplicationListener<WebServerInitializedEvent> {

    /**
     * 标识事件监听器是否已经注册，避免重复注册
     */
    private volatile AtomicBoolean flag = new AtomicBoolean(false);

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final CustomCommonHandlerUrl customCommonHandlerUrl;

    public RpcProviderScanAndReleaseListener(RequestMappingHandlerMapping requestMappingHandlerMapping, CustomCommonHandlerUrl customCommonHandlerUrl) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.customCommonHandlerUrl = customCommonHandlerUrl;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (flag.compareAndSet(false, true)) {
            // 扫描所有rpcProvider注册的bean
            scanRpcProviderBeans(event);
            // 注册所有扫描到的自定义rpcProvider
            registerUrls();
        }

    }

    /**
     * 扫描所有rpcProvider bean信息
     *
     * @param event
     */
    private void scanRpcProviderBeans(WebServerInitializedEvent event) {
        // 找到所有标识@CustomRpcProvider注解的bean
        Map<String, Object> beans = event.getApplicationContext().getBeansWithAnnotation(CustomRpcProvider.class);
        if (!CollectionUtils.isEmpty(beans)) {
            // 遍历所有标识了@CustomRpcProvider注解的bean
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                String beanName = entry.getKey();
                Object bean = entry.getValue();
                Class beanType = bean.getClass();
                Class<?>[] interfaces = beanType.getInterfaces();
                if (interfaces != null && interfaces.length != 0) {
                    for (Class clazz : interfaces) {
                        CustomRpcApp customRpcApp = (CustomRpcApp) clazz.getAnnotation(CustomRpcApp.class);
                        if (customRpcApp != null) { // 判断当前类上是否有标识指定发布接口的应用名称
                            // 如果符合我们的自定义发布规范
                            RpcProviderHolder.RpcProviderInfo rpcProviderInfo = new RpcProviderHolder.RpcProviderInfo();
                            rpcProviderInfo.setName(StringUtils.hasLength(customRpcApp.name()) ?
                                    customRpcApp.name()
                                    : event.getApplicationContext().getEnvironment().getProperty("spring.application.name"));
                            rpcProviderInfo.setRpcBeanName(beanName);
                            rpcProviderInfo.setUrlPrefix("/" + clazz.getSimpleName().toLowerCase()); // url前缀取接口名称
                            rpcProviderInfo.setRpcBean(bean);

                            Method[] methods = clazz.getMethods(); //获取所有方法
                            if (methods != null && methods.length != 0) {
                                List<RpcProviderHolder.RpcMethod> methodList = new ArrayList<>();
                                for (Method m : methods) {
                                    RpcProviderHolder.RpcMethod rm = null;
                                    CustomRpcMethodConfig annotation = m.getAnnotation(CustomRpcMethodConfig.class);
                                    if (annotation != null) { // 判断方法是否有@CustomRpcMethodConfig注解
                                        if (annotation.isForbidden()) { // 方法如果禁用，则不保存发布信息
                                            continue;
                                        }
                                        if (StringUtils.hasLength(annotation.alias())) {
                                            rm = new RpcProviderHolder.RpcMethod();
                                            rm.setAlias(annotation.alias());
                                        }
                                    } else {
                                        rm = new RpcProviderHolder.RpcMethod();
                                        rm.setAlias(m.getName());
                                    }
                                    rm.setMethod(m);
                                    methodList.add(rm);
                                }
                                rpcProviderInfo.setUrlCoreMethod(methodList);
                            }

                            RpcProviderHolder.RPC_PROVIDER_MAP.put(clazz.getSimpleName().toLowerCase(), rpcProviderInfo);
                        }
                    }
                }
            }
        }
    }

    /**
     * 注册所有自定义rpcProvider的url信息
     */
    private void registerUrls() {
        if (!CollectionUtils.isEmpty(RpcProviderHolder.RPC_PROVIDER_MAP)) {
            Collection<RpcProviderHolder.RpcProviderInfo> values = RpcProviderHolder.RPC_PROVIDER_MAP.values();
            for (RpcProviderHolder.RpcProviderInfo rpcProviderInfo : values) {
                String urlPrefix = rpcProviderInfo.getUrlPrefix();
                List<RpcProviderHolder.RpcMethod> urlCores = rpcProviderInfo.getUrlCoreMethod();
                if (!CollectionUtils.isEmpty(urlCores)) {
                    for (RpcProviderHolder.RpcMethod rm : urlCores) {
                        // 构建请求映射对象
                        RequestMappingInfo requestMappingInfo = RequestMappingInfo
                                .paths(urlPrefix + "/" + rm.getAlias()) // 请求URL
                                .methods(RequestMethod.POST) // 请求方法，可以指定多个
                                .build();
                        // 发布url，指定一下url的处理器
                        requestMappingHandlerMapping.registerMapping(requestMappingInfo, customCommonHandlerUrl, CustomCommonHandlerUrl.HANDLE_CUSTOM_URL_METHOD);
                    }
                }
            }
        }
    }
}
