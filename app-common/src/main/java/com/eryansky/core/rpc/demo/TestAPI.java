package com.eryansky.core.rpc.demo;


import com.eryansky.core.rpc.CustomRpcApp;

import java.util.Map;

@CustomRpcApp(name = "myrpcdemo", contentPath = "/myrpc")
public interface TestAPI {

    String testRpc1();

    String testRpc2(String name);

    Map<String, Object> testRpc3(Map<String, Object> map);

    String testRpc4();

}
