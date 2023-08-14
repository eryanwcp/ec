/**
 * Copyright (c) 2012-2018 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security.interceptor;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;


/**
 * Rest权限拦截器
 * @author wencp
 * @date 2020-09-09
 */
public class RestDefaultAuthorityInterceptor implements AsyncHandlerInterceptor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public static final String CACHE_REST_PREFIX = "Rest_Authority_";
    public static final String REST_AUTHORITY_HEADER_NAME = "X-Api-Key";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String sessionId = request.getSession().getId();
        String cacheKey = CACHE_REST_PREFIX + sessionId;
        Boolean handlerResult = CacheUtils.get(cacheKey);
        if (null != handlerResult) {
            return handlerResult;
        }
        String requestUrl = request.getRequestURI().replaceAll("//", "/");
        if (logger.isDebugEnabled()) {
            logger.debug("{} {} {}",request.getSession().getId(),request.getHeader("Authorization"),requestUrl);
        }
        boolean restEnable = AppConstants.getIsSystemRestEnable();
        if (!restEnable) {
            Result result = Result.errorApiResult().setMsg("系统维护中，请稍后再试！");
            logger.warn("{} {} {}",IpUtils.getIpAddr0(request) ,result,requestUrl);
            WebUtils.renderJson(response, result);
            return false;
        }

        //注解处理
        handlerResult = this.defaultaHndler(request, response, o, requestUrl);
        CacheUtils.put(cacheKey, handlerResult);
        if (null != handlerResult) {
            return handlerResult;
        }
        return true;
    }


    /**
     * 注解处理
     * @param request
     * @param response
     * @param handler
     * @param requestUrl
     * @return
     * @throws Exception
     */
    private Boolean defaultaHndler(HttpServletRequest request, HttpServletResponse response, Object handler, String requestUrl) throws Exception {
        HandlerMethod handlerMethod = null;
        //注解处理 满足设置不拦截
        if(handler instanceof HandlerMethod) {
            handlerMethod = (HandlerMethod) handler;
        }

        if (handlerMethod != null) {
            //权限校验
            RestApi restApi = handlerMethod.getMethodAnnotation(RestApi.class);
            if (restApi == null) {
                restApi = this.getAnnotation(handlerMethod.getBean().getClass(), RestApi.class);
            }
            if (restApi != null) {//方法注解处理
                if (!restApi.required()) {
                    return true;
                }
                String apiKey = request.getHeader(REST_AUTHORITY_HEADER_NAME);
                if (null == apiKey) {
                    notPermittedPermission(request, response, requestUrl, "未识别参数:Header['X-API-Key']=" + apiKey);
                    return false;
                }
                //密钥认证
                String DEFAULT_API_KEY = AppConstants.getRestDefaultApiKey();
                if (!DEFAULT_API_KEY.equals(apiKey)) {
                    notPermittedPermission(request, response, requestUrl, "未授权访问:Header['X-API-Key']=" + apiKey);
                    return false;
                }

                //IP访问限制
                boolean isRestLimitEnable = AppConstants.getIsSystemRestLimitEnable();
                if (isRestLimitEnable) {
                    String ip = IpUtils.getIpAddr0(request);
                    if("127.0.0.1".equals(ip) || "localhost".equals(ip)){
                        return true;
                    }
                    List<String> ipList = AppConstants.getRestLimitIpWhiteList();
                    if (null == ipList.stream().filter(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip)).findAny().orElse(null)) {
                        notPermittedPermission(request, response, requestUrl, "REST禁止访问：" + ip);
                        return false;
                    }
                }
                return true;
            }

        }
        return null;
    }

    /**
     * 未授权权限
     * @param request
     * @param response
     * @param requestUrl
     * @throws ServletException
     * @throws IOException
     */
    private void notPermittedPermission(HttpServletRequest request, HttpServletResponse response, String requestUrl, String msg) throws ServletException, IOException {
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        Result result = Result.noPermissionResult().setMsg(msg);
        logger.warn(IpUtils.getIpAddr0(request) + " " + result.toString() + " " + requestUrl);
        WebUtils.renderJson(response, result);
    }


    private <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationType) {
        T result = clazz.getAnnotation(annotationType);
        if (result == null) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getAnnotation(superclass, annotationType);
            } else {
                return null;
            }
        } else {
            return result;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        if (e != null) {

        }
    }

}
