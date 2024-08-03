package com.eryansky.core.rpc.demo;

import com.eryansky.core.rpc.CustomRpcProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@CustomRpcProvider
public class TestAPIImpl implements TestAPI {

    @Override
    public String testRpc1() {
        return "I am empty method....";
    }

    @Override
    public String testRpc2(String name) {
        return "hello, " + name;
    }

    @Override
    public Map<String, Object> testRpc3(Map<String, Object> map) {
        map.put("code", 500);
        return map;
    }

    @Override
    public String testRpc4() {
        return null;
    }
}
