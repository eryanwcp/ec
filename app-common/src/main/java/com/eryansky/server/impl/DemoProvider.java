package com.eryansky.server.impl;

import com.eryansky.common.model.R;
import com.eryansky.common.orm.Page;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.rpc.annotation.RPCProvider;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.server.DemoAPI;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RPCProvider
@Component
public class DemoProvider implements DemoAPI {

    @Autowired
    private UserService userService;

    @Logging(value = "rest1",logType = LogType.API)
    @Override
    public R<String> test1(String param1) {
        return new R<String>().setCode(R.SUCCESS).setData(param1);
    }

    @Override
    public R<String> test1(String param1, String param2) {
        return new R<String>().setCode(R.SUCCESS).setData( param1+" "+param2);
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

    @Override
    public Page<User> test001(Page<User> page, User user) {
        page = userService.findPage(page,user);
        return page;
    }

    @Override
    public void test003() {
        System.out.println("test003");
    }

    @Override
    public R<Object> encrypt(String param1) {
        return new R<>().setData(new User(User.SUPERUSER_ID)).setCode(R.SUCCESS);
    }
}
