/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.eryansky.common.mail.auth;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * 简单的用户名/密码认证方式
 *
 * @author Eryan
 * @date 2015-09-14
 */
public class SimpleAuthenticator extends Authenticator {
	private final String user;
	private final String password;

	public SimpleAuthenticator(String user, String password) {
		this.user = user;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.user, this.password);
	}
}
