package com.eryansky.core.rpc.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

public class RpcRestTemplateUtils {

    private static RpcRestTemplateUtils rpcRestTemplateUtils;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        rpcRestTemplateUtils = this;
    }

    public static RestTemplate restTemplate() {
        return rpcRestTemplateUtils.restTemplate;
    }

}
