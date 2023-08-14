package com.eryansky;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(value = SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class SeneitiveTests {

	@Autowired
	private UserService userService;

	@Test
	public void get() {
		User user = userService.get(User.SUPERUSER_ID);
		System.out.println(JsonMapper.toJsonString(user));
	}

}
