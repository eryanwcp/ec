/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.listener;

import com.eryansky.common.web.listener.DefaultSystemInitListener;
import com.eryansky.core.security.SecurityType;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSessionEvent;

/**
 * 系统初始化监听 继承默认系统启动监听器.
 * 
 * @author Eryan
 * @date 2012-12-11 下午4:56:54
 */
public class SystemInitListener extends DefaultSystemInitListener{

	private static final Logger logger = LoggerFactory.getLogger(SystemInitListener.class);


	public SystemInitListener() {
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//核心线程池数量，方法: 返回可用处理器的Java虚拟机的数量。
		int processors = Runtime.getRuntime().availableProcessors();
		int initProcessors = processors < 4 ? processors - 1 : processors - 2;
		Integer configParallelism = AppConstants.getPoolParallelism();
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(null != configParallelism ? configParallelism : initProcessors));
		super.contextInitialized(sce);
		AppUtils.init(sce.getServletContext());


	}

	/**
	 * session销毁
	 */
	public void sessionDestroyed(HttpSessionEvent evt) {
		String sessionId = SecurityUtils.getNoSuffixSessionId(evt.getSession());
		logger.debug("sessionDestroyed {}",sessionId);
		SecurityUtils.removeSession(sessionId,SecurityType.logout_abnormal);
	}


}
