package com.eryansky;

import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.server.DemoAPI;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest(classes = Application.class)
public class ApiTests {

    @Resource
    private DemoAPI demoApi;
    @Test
    public void contextLoads() {

        System.out.println(JsonMapper.toJsonString(demoApi.encrypt("1")));
        System.out.println(demoApi.test1("1"));
        System.out.println(demoApi.test1("1","2"));
        System.out.println(demoApi.test2("1"));
        System.out.println(JsonMapper.toJsonString(demoApi.test10("1")));

        Map<String,Object> map = Maps.newHashMap();
        map.put("str","maps");
        map.put("user",new User(User.SUPERUSER_ID));
        System.out.println(JsonMapper.toJsonString(demoApi.test11("1",1,map, Lists.newArrayList(new User(User.SUPERUSER_ID)),new Page<User>(1,2))));


    }


}
