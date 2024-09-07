package com.eryansky.core.rpc.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.web.client.RestTemplate;

public class RestTemplateHolder {

    private static RestTemplateHolder restTemplateHolder;

    @Resource
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplateHolder = this;
    }

    public static RestTemplate restTemplate() {
        return restTemplateHolder.restTemplate;
    }

}
