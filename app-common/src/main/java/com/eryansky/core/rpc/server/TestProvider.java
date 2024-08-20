package com.eryansky.core.rpc.server;

import com.eryansky.core.rpc.annotation.RPCProvider;
import com.eryansky.core.rpc.client.TestAPI;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RPCProvider
public class TestProvider implements TestAPI {

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
