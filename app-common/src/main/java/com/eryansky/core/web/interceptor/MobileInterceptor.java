/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.web.interceptor;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.utils.AppUtils;
import com.google.common.collect.Lists;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.annotation.MobileValue;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 手机端视图拦截器（注解缓存优化版）
 * @author Eryan
 * @date : 2015-07-10
 */
public class MobileInterceptor implements HandlerInterceptor {

	/**
	 * 包含的URL
	 */
	private List<String> includeUrls = Lists.newArrayList();

	/**
	 * 排除的URL
	 */
	private List<String> excludeUrls = Lists.newArrayList();

	/**
	 * Mobile 注解解析结果缓存，避免高并发下重复反射解析
	 */
	private final Map<HandlerMethod, MobileAnnotationMetadata> mobileAnnotationCache = new ConcurrentHashMap<>();

	/**
	 * Mobile 注解解析元数据封装
	 */
	private static class MobileAnnotationMetadata {
		final Boolean flag;
		final MobileValue mobileValue;

		public MobileAnnotationMetadata(Boolean flag, MobileValue mobileValue) {
			this.flag = flag;
			this.mobileValue = mobileValue;
		}
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (modelAndView != null && !StringUtils.startsWithIgnoreCase(modelAndView.getViewName(), "redirect:")) {
			Boolean flag = null;
			MobileValue mobileValue = null;

			if (handler instanceof HandlerMethod) {
				HandlerMethod handlerMethod = (HandlerMethod) handler;
				// 从缓存获取解析后的注解元信息
				MobileAnnotationMetadata metadata = mobileAnnotationCache.computeIfAbsent(handlerMethod, this::parseMobileAnnotationMetadata);
				flag = metadata.flag;
				mobileValue = metadata.mobileValue;
			}

			String requestUrl = request.getRequestURI();
			if (flag == null) {
				if (Collections3.isNotEmpty(excludeUrls)) {
					for (String excludeUrl : excludeUrls) {
						flag = !StringUtils.simpleWildcardMatch(excludeUrl, requestUrl);
						break;
					}
				}
			}

			if (flag == null) {
				if (Collections3.isNotEmpty(includeUrls)) {
					for (String includeUrl : includeUrls) {
						flag = StringUtils.simpleWildcardMatch(includeUrl, requestUrl);
						break;
					}
				}
			}

			if (flag != null && flag) {
				if (MobileValue.ALL.equals(mobileValue)) {
					if (UserAgentUtils.isMobileOrTablet(request)) {
						modelAndView.setViewName("mobile/" + modelAndView.getViewName());
					}
				} else if (MobileValue.MOBILE.equals(mobileValue)) {
					modelAndView.setViewName("mobile/" + modelAndView.getViewName());
				}
			}
		}
	}

	/**
	 * 解析 HandlerMethod 及 Class 上的 Mobile 注解（首次调用时触发）
	 */
	private MobileAnnotationMetadata parseMobileAnnotationMetadata(HandlerMethod handlerMethod) {
		Boolean flag = null;
		MobileValue mobileValue = null;

		// 1. 方法注解处理优先
		Mobile methodMobile = handlerMethod.getMethodAnnotation(Mobile.class);
		if (methodMobile != null) {
			flag = methodMobile.mobile();
			mobileValue = methodMobile.value();
		} else {
			// 2. 类注解处理兜底
			Mobile classMobile = AppUtils.getAnnotation(handlerMethod.getBeanType(), Mobile.class);
			if (classMobile != null) {
				flag = classMobile.mobile();
				mobileValue = classMobile.value();
			}
		}

		return new MobileAnnotationMetadata(flag, mobileValue);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
	}

	public List<String> getIncludeUrls() {
		return includeUrls;
	}

	public void setIncludeUrls(List<String> includeUrls) {
		this.includeUrls = includeUrls;
	}

	public List<String> getExcludeUrls() {
		return excludeUrls;
	}

	public void setExcludeUrls(List<String> excludeUrls) {
		this.excludeUrls = excludeUrls;
	}
}