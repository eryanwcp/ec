package com.eryansky.core.rpc.config;

import com.eryansky.core.rpc.consumer.ConsumerScanAndFillListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ConsumerConfig {

//    @Bean
//    public ConsumerScanAndFillListener consumerScanAndFillListener() {
//        return new ConsumerScanAndFillListener();
//    }
    @Bean
    public RestTemplateHolder restTemplateHolder() {
        return new RestTemplateHolder();
    }

}
