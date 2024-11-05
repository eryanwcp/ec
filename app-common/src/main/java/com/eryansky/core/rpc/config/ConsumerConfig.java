package com.eryansky.core.rpc.config;

import com.eryansky.core.rpc.consumer.ConsumerScanAndFillListener;
import com.eryansky.core.rpc.consumer.EcServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class ConsumerConfig {

//    @Bean
//    public ConsumerScanAndFillListener consumerScanAndFillListener() {
//        //需要设置serverUrl
//        return new ConsumerScanAndFillListener();
//    }
//    @Bean
//    public RestTemplateHolder restTemplateHolder() {
//        return new RestTemplateHolder();
//    }


    @Scope(value = "prototype")
    @Bean
    public EcServiceClient ecServiceClient() {
        return new EcServiceClient();
    }

}
