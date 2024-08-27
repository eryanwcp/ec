package com.eryansky.core.rpc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

public class RestTemplateHolder {

    private static RestTemplateHolder restTemplateHolder;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplateHolder = this;
    }

    public static RestTemplate restTemplate() {
        return restTemplateHolder.restTemplate;
    }

}
