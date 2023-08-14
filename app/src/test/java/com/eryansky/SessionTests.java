package com.eryansky;

import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.List;

@RunWith(value = SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class SessionTests {

	@Autowired
	private UserService userService;
	@Test
	public void test() {
		String userId= "585b4521f0ce474c9fe31fa906950a63";
		User user = userService.get(userId);
		SessionInfo sessionInfo = SecurityUtils.putUserToSession("sessionId",user);
		System.out.println(JsonMapper.toJsonString(sessionInfo));
		System.out.println(SecurityUtils.isPermitted(userId,"sys:session:view"));

    }




}
