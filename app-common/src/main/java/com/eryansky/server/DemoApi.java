package com.eryansky.server;

import com.eryansky.client.common.rpc.RPCExchange;
import com.eryansky.client.common.rpc.RPCMethodConfig;
import com.eryansky.client.common.rpc.RPCPermissions;
import com.eryansky.common.model.R;
import com.eryansky.common.orm.Page;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys.mapper.User;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

//@RPCExchange(name = "demo-service", serverUrl = "${ec.api.serverUrl:http://localhost:8080}",apiKey = "${ec.api.apiKey:}",encrypt = RPCExchange.ENCRYPT_AES)
@RPCExchange(name = "demo-service", serverUrl = "${ec.api.serverUrl:}",apiKey = "${ec.api.apiKey:}",encrypt = "${ec.api.encrypt:}")
public interface DemoApi {
    String test1(String param1);

    @RPCPermissions(value = {"123"})
    @RPCMethodConfig(alias = "test120",encrypt = RPCMethodConfig.ENCRYPT_NONE)
    String test1(String param1,String param2);
    int test2(String param1);
    R<Boolean> test10(String param1);
    R<Map<String,Object>> test11(String str, int pint, Map<String,Object> map, List<User> list, Page<User> page);
    Page<User> test12(Page<User> page);

    R<Object> encrypt(String param1);
}
