package com.eryansky.core.rpc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ConsumerConfig {


    @Bean
    public RestTemplateHolder restTemplateHolder() {
        return new RestTemplateHolder();
    }

}
