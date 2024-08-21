package com.eryansky.core.rpc.client;


import com.eryansky.core.rpc.annotation.RPCExchange;

import java.util.Map;

@RPCExchange(name = "test-service")
public interface TestAPI {

    String testRpc1();

    String testRpc2(String name);

    Map<String, Object> testRpc3(Map<String, Object> map);

    String testRpc4();

}
