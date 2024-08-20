package com.eryansky.core.rpc.client;

import com.eryansky.core.rpc.annotation.RPCConsumer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TestConsumer {

    @RPCConsumer // 标识这是一个远程RPC调用
    private TestAPI testprovider;


    public TestAPI getTestprovider() {
        return testprovider;
    }

    public String testRpc1() {
        return testprovider.testRpc1();
    }

    public String testRpc2(String name) {
        return testprovider.testRpc2(name);
    }

    public Map<String, Object> testRpc3(Map<String, Object> map) {
        return testprovider.testRpc3(map);
    }

}
