package com.eryansky.core.rpc.config;

import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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
