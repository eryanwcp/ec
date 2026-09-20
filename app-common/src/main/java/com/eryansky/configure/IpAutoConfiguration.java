package com.eryansky.configure;

import com.eryansky.core.ip.IpAPI;
import com.eryansky.core.ip.IpDefaultProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IpAPI.class)
    public IpAPI defaultIpAPI() {
        return new IpDefaultProvider();
    }
}