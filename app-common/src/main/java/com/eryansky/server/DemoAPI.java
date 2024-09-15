package com.eryansky.server;

import com.eryansky.client.common.rpc.RPCExchange;
import com.eryansky.client.common.rpc.RPCMethodConfig;
import com.eryansky.client.common.rpc.RPCPermissions;
import com.eryansky.common.model.R;
import com.eryansky.common.orm.Page;
import com.eryansky.modules.sys.mapper.User;

import java.util.List;
import java.util.Map;

//@RPCExchange(name = "demo-service", serverUrl = "${ec.api.serverUrl:http://localhost:8080}",apiKey = "${ec.api.apiKey:}",encrypt = RPCExchange.ENCRYPT_AES)
@RPCExchange(name = "demo-service", serverUrl = "${ec.api.serverUrl:http://localhost:8080/ec}",apiKey = "${ec.api.apiKey:}",encrypt = "${ec.api.encrypt:}")
public interface DemoAPI {
    String test1(String param1);


    @RPCMethodConfig(alias = "test120",encrypt = RPCMethodConfig.ENCRYPT_NONE)
    String test1(String param1,String param2);

    int test2(String param1);

    R<Boolean> test10(String param1);

    R<Map<String,Object>> test11(String str, int pint, Map<String,Object> map, List<User> list, Page<User> page);

    Page<User> test12(Page<User> page);

    @RPCPermissions(value = {"123"})
    Page<User> test001(Page<User> page,User user);

    String test002();
    void test003();

    R<Object> encrypt(String param1);
}
