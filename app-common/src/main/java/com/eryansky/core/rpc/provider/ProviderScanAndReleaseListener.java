package com.eryansky.core.rpc.provider;

import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.client.common.rpc.RPCExchange;
import com.eryansky.client.common.rpc.RPCMethodConfig;
import com.eryansky.core.rpc.annotation.RPCProvider;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ClassUtils;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProviderScanAndReleaseListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ProviderScanAndReleaseListener.class);
    /**
     * 标识事件监听器是否已经注册，避免重复注册
     */
    private volatile AtomicBoolean flag = new AtomicBoolean(false);

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final CommonHandlerUrl commonHandlerUrl;

    public ProviderScanAndReleaseListener(RequestMappingHandlerMapping requestMappingHandlerMapping, CommonHandlerUrl commonHandlerUrl) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.commonHandlerUrl = commonHandlerUrl;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (flag.compareAndSet(false, true)) {
            // 扫描所有rpcProvider注册的bean
            scanRpcProviderBeans(event.getApplicationContext());
            // 注册所有扫描到的自定义rpcProvider
            registerUrls();
            log.info("RPC服务端启动:{}",ProviderHolder.RPC_PROVIDER_MAP.size());
        }
    }
    /**
     * 扫描所有rpcProvider bean信息
     *
     * @param applicationContext
     */
    private void scanRpcProviderBeans(ApplicationContext applicationContext) {
        // 找到所有标识@CustomRpcProvider注解的bean
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RPCProvider.class);
        if (!CollectionUtils.isEmpty(beans)) {
            // 遍历所有标识了@CustomRpcProvider注解的bean
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                String beanName = entry.getKey();
                Object bean = entry.getValue();
                Class beanType = bean.getClass();
//                Class<?>[] interfaces = beanType.getInterfaces();
                Class<?>[] interfaces = ClassUtils.getAllInterfaces(bean);
                if (interfaces != null && interfaces.length != 0) {
                    for (Class clazz : interfaces) {
                        RPCExchange app = (RPCExchange) clazz.getAnnotation(RPCExchange.class);
                        if (app != null) { // 判断当前类上是否有标识指定发布接口的应用名称
                            // 如果符合我们的自定义发布规范
                            ProviderHolder.ProviderInfo providerInfo = new ProviderHolder.ProviderInfo();
                            providerInfo.setName(app.name());
                            providerInfo.setRpcBeanName(beanName);
                            providerInfo.setUrlPrefix(app.urlPrefix()+"/" +app.name()); // url前缀取接口名称
                            providerInfo.setRpcBean(bean);

                            Method[] methods = clazz.getMethods(); //获取所有方法
                            if (methods != null && methods.length != 0) {
                                List<ProviderHolder.RPCMethod> methodList = new ArrayList<>();
                                for (Method m : methods) {
                                    ProviderHolder.RPCMethod rm = new ProviderHolder.RPCMethod();
                                    RPCMethodConfig annotation = m.getAnnotation(RPCMethodConfig.class);
                                    if (annotation != null) { // 判断方法是否有@CustomRpcMethodConfig注解
                                        if (annotation.isForbidden()) { // 方法如果禁用，则不保存发布信息
                                            continue;
                                        }
                                        if (StringUtils.hasLength(annotation.alias())) {
                                            rm.setAlias(annotation.alias());
                                        }else {
                                            rm.setAlias(m.getName());
                                        }
                                        rm.setEncrypt(annotation.encrypt());
                                    } else {
                                        rm.setAlias(m.getName());
                                    }
                                    rm.setMethod(m);
                                    methodList.add(rm);
                                }
                                providerInfo.setUrlCoreMethod(methodList);
                            }
                            log.debug(JsonMapper.toJsonString(providerInfo));
                            ProviderHolder.RPC_PROVIDER_MAP.put(app.name(), providerInfo);
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
        if (!CollectionUtils.isEmpty(ProviderHolder.RPC_PROVIDER_MAP)) {
            Collection<ProviderHolder.ProviderInfo> values = ProviderHolder.RPC_PROVIDER_MAP.values();
            List<String> urlMappingList = Lists.newArrayList();
            for (ProviderHolder.ProviderInfo providerInfo : values) {
                String urlPrefix = providerInfo.getUrlPrefix();
                List<ProviderHolder.RPCMethod> urlCores = providerInfo.getUrlCoreMethod();
                if (!CollectionUtils.isEmpty(urlCores)) {
                    for (ProviderHolder.RPCMethod rm : urlCores) {
                        // 构建请求映射对象
                        String path = urlPrefix + "/" + rm.getAlias();
                        RequestMappingInfo requestMappingInfo = RequestMappingInfo
                                .paths(path) // 请求URL
                                .methods(RequestMethod.POST) // 请求方法，可以指定多个
                                .build();
                        urlMappingList.add(path);
                        // 发布url，指定一下url的处理器
//                        log.debug("{}",JsonMapper.toJsonString(requestMappingInfo.getDirectPaths()));
                        requestMappingHandlerMapping.registerMapping(requestMappingInfo, commonHandlerUrl, CommonHandlerUrl.HANDLE_CUSTOM_URL_METHOD);
                    }
                }
            }
            List<String> checkUrlMappingList =  urlMappingList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if(Collections3.isNotEmpty(checkUrlMappingList)){
                log.error("映射路径重复：{}",JsonMapper.toJsonString(checkUrlMappingList));
            }

        }
    }


}
