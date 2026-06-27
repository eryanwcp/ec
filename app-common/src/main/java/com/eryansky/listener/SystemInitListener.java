/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.listener;

import com.eryansky.common.web.listener.DefaultSystemInitListener;
import com.eryansky.core.security.SecurityType;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.http.HttpSessionEvent;

/**
 * 系统初始化监听 继承默认系统启动监听器.
 *
 * @author Eryan
 * @date 2012-12-11
 */
public class SystemInitListener extends DefaultSystemInitListener {

	private static final Logger logger = LoggerFactory.getLogger(SystemInitListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// 获取可用处理器的核心数
		int processors = Runtime.getRuntime().availableProcessors();

		// 优化点：防范单核 CPU 导致计算出 0 或负数，确保并行度至少为 1
		int initProcessors = processors < 4 ? Math.max(1, processors - 1) : processors - 2;

		// 读取配置的并行度
		Integer configParallelism = AppConstants.getPoolParallelism();
		int finalParallelism = (configParallelism != null) ? configParallelism : initProcessors;

		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(finalParallelism));

		super.contextInitialized(sce);
		AppUtils.init(sce.getServletContext());
	}

	/**
	 * Session 销毁（用户异常退出或超时）
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent evt) {
		String sessionId = SecurityUtils.getNoSuffixSessionId(evt.getSession());
		if (logger.isDebugEnabled()) {
			logger.debug("sessionDestroyed: {}", sessionId);
		}
		SecurityUtils.removeSession(sessionId, SecurityType.logout_abnormal);
	}
}