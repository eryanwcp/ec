package com.eryansky.core.rpc.config;

import com.eryansky.core.rpc.provider.CommonHandlerUrl;
import com.eryansky.core.rpc.provider.ProviderScanAndReleaseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class ProviderConfig {

    private static final Logger log = LoggerFactory.getLogger(ProviderConfig.class);

    @Bean
    public CommonHandlerUrl commonHandlerUrl() {
        return new CommonHandlerUrl();
    }

    @Bean
    public ProviderScanAndReleaseListener providerScanAndReleaseListener(RequestMappingHandlerMapping requestMappingHandlerMapping, CommonHandlerUrl commonHandlerUrl) {
        log.info("RPC服务端启动。");
        return new ProviderScanAndReleaseListener(requestMappingHandlerMapping, commonHandlerUrl);
    }

}
