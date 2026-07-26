/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security.interceptor;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.j2cache.lock.DefaultLockCallback;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppUtils;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟OAuth2认证拦截器（注解缓存优化版）
 * @author Eryan
 * @date 2021-09-09
 */
public class AuthorityOauth2Interceptor implements AsyncHandlerInterceptor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 不需要拦截的资源
     */
    private List<String> excludeUrls = Lists.newArrayList();

    /**
     * PrepareOauth2 注解解析结果缓存，避免重复反射
     */
    private final Map<HandlerMethod, Oauth2AnnotationMetadata> oauth2AnnotationCache = new ConcurrentHashMap<>();

    /**
     * OAuth2 注解元数据封装
     */
    private static class Oauth2AnnotationMetadata {
        final boolean enable;        // 是否开启 OAuth2 准备
        final String authType;       // 认证类型

        public Oauth2AnnotationMetadata(boolean enable, String authType) {
            this.enable = enable;
            this.authType = authType;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 已登录用户
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo(request, false);
        if (null != sessionInfo) {
            return true;
        }

        // 注解处理 满足设置不拦截
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // 优先获取缓存中的注解解析结果，避免高并发下频繁反射调用
            Oauth2AnnotationMetadata metadata = oauth2AnnotationCache.computeIfAbsent(handlerMethod, this::parseOauth2AnnotationMetadata);

            // 未开启 OAuth2 直接放行
            if (!metadata.enable) {
                return true;
            }

            // 非内置用户 自动跳过
            if (null != metadata.authType && !PrepareOauth2.DEFAULT_AUTH_TYPE.equals(metadata.authType)) {
                return true;
            }

            String token = AppUtils.extractToken(request);
            if (StringUtils.isNotBlank(token)) {
                String lockKey = "lock_oauth2_token:" + Encrypt.md5(token);
                CacheUtils.getCacheChannel().lock(lockKey, 5, 10, new DefaultLockCallback<Boolean>(false, false) {
                    @Override
                    public Boolean handleObtainLock() {
                        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo(request, false);
                        if (null != sessionInfo) {
                            return true;
                        }
                        String requestUrl = request.getRequestURI();
                        String loginName = null;
                        try {
                            loginName = SecurityUtils.getLoginNameByToken(token);
                        } catch (Exception e) {
                            if (!(e instanceof TokenExpiredException)) {
                                logger.error("Token校验失败：{},{},{},{},{}", loginName, SpringMVCHolder.getIp(), requestUrl, token, e.getMessage());
                            }
                        }
                        if (StringUtils.isBlank(loginName)) {
                            return true;
                        }
                        // 自动登录
                        boolean verify = false;
                        User user = null;
                        try {
                            user = UserUtils.getUserByLoginName(loginName);
                            if (null == user) {
                                logger.warn("Token校验失败（用户不存在）：{},{},{}", loginName, requestUrl, token);
                                return true;
                            }
                            verify = SecurityUtils.verifySessionInfoToken(token, loginName, user.getPassword());
                        } catch (Exception e) {
                            if (!(e instanceof TokenExpiredException)) {
                                logger.error("Token校验失败：{},{},{},{},{}", loginName, SpringMVCHolder.getIp(), requestUrl, token, e.getMessage());
                            }
                        }
                        if (verify) {
                            SecurityUtils.putUserToSession(request, user);
                            UserUtils.recordLogin(user.getId());
                            logger.debug("自动登录成功：{},{},{} {}", loginName, IpUtils.getIpAddr0(request), requestUrl, lockKey);
                        }
                        return true;
                    }
                });
            }
        }
        return true;
    }

    /**
     * 解析 HandlerMethod 及 Class 上的 PrepareOauth2 注解（首次调用时触发）
     */
    private Oauth2AnnotationMetadata parseOauth2AnnotationMetadata(HandlerMethod handlerMethod) {
        PrepareOauth2 prepareOauth2Method = handlerMethod.getMethodAnnotation(PrepareOauth2.class);
        PrepareOauth2 prepareOauth2Class = AppUtils.getAnnotation(handlerMethod.getBeanType(), PrepareOauth2.class);

        // 方法级别禁用，或类级别禁用
        boolean enable = true;
        if ((prepareOauth2Method != null && !prepareOauth2Method.enable()) ||
                (prepareOauth2Class != null && !prepareOauth2Class.enable())) {
            enable = false;
        }

        // 决定 authType 优先级：方法注解 > 类注解
        String authType = null != prepareOauth2Method ? prepareOauth2Method.authType() : null;
        if (null == authType) {
            authType = null != prepareOauth2Class ? prepareOauth2Class.authType() : null;
        }

        return new Oauth2AnnotationMetadata(enable, authType);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        if (e != null) {
        }
    }

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public AuthorityOauth2Interceptor setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
        return this;
    }

    public AuthorityOauth2Interceptor addExcludeUrl(String excludeUrl) {
        this.excludeUrls.add(excludeUrl);
        return this;
    }
}