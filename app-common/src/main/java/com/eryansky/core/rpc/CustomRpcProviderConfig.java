package com.eryansky.core.rpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class CustomRpcProviderConfig {

    @Bean
    public CustomCommonHandlerUrl customCommonHandlerUrl() {
        return new CustomCommonHandlerUrl();
    }

    @Bean
    public RpcProviderScanAndReleaseListener rpcProviderReleaseListener(RequestMappingHandlerMapping requestMappingHandlerMapping, CustomCommonHandlerUrl customCommonHandlerUrl) {
        return new RpcProviderScanAndReleaseListener(requestMappingHandlerMapping, customCommonHandlerUrl);
    }

}
