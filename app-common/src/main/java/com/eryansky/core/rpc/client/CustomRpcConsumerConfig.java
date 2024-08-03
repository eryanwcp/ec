package com.eryansky.core.rpc.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class CustomRpcConsumerConfig {

    @Bean
    public ConsumerScanAndFillListener consumerScanAndFillListener() {
        return new ConsumerScanAndFillListener();
    }

    //    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder){
//        return builder.build();
//    }

    @Bean
    public RpcRestTemplateUtils rpcRestTemplateUtils() {
        return new RpcRestTemplateUtils();
    }

}
