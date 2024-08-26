package com.eryansky.server.impl;

import com.eryansky.common.model.R;
import com.eryansky.common.orm.Page;
import com.eryansky.core.rpc.annotation.RPCProvider;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.server.DemoApi;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RPCProvider
@Component
public class DemoApiImpl implements DemoApi {
    @Override
    public String test1(String param1) {
        return param1;
    }

    @Override
    public String test1(String param1, String param2) {
        return param1+" "+param2;
    }

    @Override
    public int test2(String param1) {
        return 0;
    }

    @Override
    public R<Boolean> test10(String param1) {
        return new R<Boolean>(true);
    }

    @Override
    public R<Map<String,Object>> test11(String str, int pint, Map<String, Object> map, List<User> list, Page<User> page) {
        Map<String,Object> data = Maps.newHashMap();
        data.put("str",str);
        data.put("int",pint);
        data.put("map",map);
        data.put("list",list);
        data.put("page",page);
        return new R<>(data).setCode(R.SUCCESS);
    }

    @Override
    public Page<User> test12(Page<User> page) {
        return page;
    }
}
