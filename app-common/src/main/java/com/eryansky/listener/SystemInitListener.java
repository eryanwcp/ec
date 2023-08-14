/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.listener;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.listener.DefaultSystemInitListener;
import com.eryansky.core.security.SecurityType;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.server.IApiWebService;
import com.eryansky.server.impl.ApiWebServiceImpl;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.xml.ws.Endpoint;

/**
 * 系统初始化监听 继承默认系统启动监听器.
 * 
 * @author Eryan
 * @date 2012-12-11 下午4:56:54
 */
public class SystemInitListener extends DefaultSystemInitListener{

	private static final Logger logger = LoggerFactory.getLogger(SystemInitListener.class);

	private static Endpoint endpoint;
	/**
	 * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
	 */
	public static final class Static {
		public static final IApiWebService apiWebService = new ApiWebServiceImpl();
	}

	public SystemInitListener() {
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//核心线程池数量，方法: 返回可用处理器的Java虚拟机的数量。
		int processors = Runtime.getRuntime().availableProcessors();
		int initProcessors = processors < 4 ? processors - 1 : processors - 2;
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(initProcessors));
		super.contextInitialized(sce);
		AppUtils.init(sce.getServletContext());
		logger.info("当前启动系统：{}-V{}",AppConstants.getAppFullName(),AppConstants.getAppVersion());
		clearTempDir();

		//WebService发布
		String webServiceUrl = AppConstants.getWebServiceUrl();
		if(StringUtils.isNotBlank(webServiceUrl)){
			logger.info("WebService发布...");
			try {
				endpoint = Endpoint.publish(webServiceUrl, Static.apiWebService);
			} catch (Exception e) {
				logger.error("WebService发布失败，"+e.getMessage(),e);
			}
			logger.info("WebService发布成功，发布地址：{}",webServiceUrl);
		}

		logger.info("默认访问地址：{}",AppConstants.getAppURL());
	}

	/**
	 * session销毁
	 */
	public void sessionDestroyed(HttpSessionEvent evt) {
		logger.debug("sessionDestroyed");
		String sessionId = SecurityUtils.getNoSuffixSessionId(evt.getSession());
		SecurityUtils.removeSessionInfoFromSession(sessionId,SecurityType.logout_abnormal);
	}


	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(null != endpoint) {
			try {
				endpoint.stop();
				logger.error("WebService 已停止服务！");
			} catch (Exception e) {
				logger.error("WebService 停止服务失败，" + e.getMessage(), e);
			}
		}
		super.contextDestroyed(sce);
	}

	private void clearTempDir(){
		logger.info("清空缓存目录[{}]...", AppConstants.getDiskTempDir());
		DiskUtils.clearTempDir();
		logger.info("清空缓存目录完毕");
	}
}
