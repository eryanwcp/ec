package com.eryansky.core.rpc.config;

import com.eryansky.core.rpc.provider.CommonHandlerUrl;
import com.eryansky.core.rpc.provider.ProviderScanAndReleaseListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class ProviderConfig {

    @Bean
    public CommonHandlerUrl commonHandlerUrl() {
        return new CommonHandlerUrl();
    }

    @Bean
    public ProviderScanAndReleaseListener providerScanAndReleaseListener(RequestMappingHandlerMapping requestMappingHandlerMapping, CommonHandlerUrl commonHandlerUrl) {
        return new ProviderScanAndReleaseListener(requestMappingHandlerMapping, commonHandlerUrl);
    }

}
