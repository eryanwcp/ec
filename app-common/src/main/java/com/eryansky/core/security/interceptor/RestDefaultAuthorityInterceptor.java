/**
 * Copyright (c) 2012-2026 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security.interceptor;

import com.eryansky.common.model.R;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rest权限拦截器（注解缓存优化版）
 * @author Eryan
 * @date 2020-09-09
 */
public class RestDefaultAuthorityInterceptor implements AsyncHandlerInterceptor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public static final String SESSION_KEY_REST_AUTHORITY = "REST_AUTHORITY";
    private static final String SESSION_TAG_NAME = "loginUser";
    private static final String SYSTEM_PREFIX_NAME = "内部系统";

    /**
     * Rest 权限注解解析结果缓存，避免重复反射
     */
    private final Map<HandlerMethod, RestAnnotationMetadata> restAnnotationCache = new ConcurrentHashMap<>();

    /**
     * REST 权限注解元数据封装
     */
    private static class RestAnnotationMetadata {
        final RestApi restApi;
        final boolean restApiRequired;
        final boolean requiresUserSkip; // requiresUser != null && !requiresUser.required()

        public RestAnnotationMetadata(RestApi restApi, boolean restApiRequired, boolean requiresUserSkip) {
            this.restApi = restApi;
            this.restApiRequired = restApiRequired;
            this.requiresUserSkip = requiresUserSkip;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        HttpSession httpSession = request.getSession();
        Boolean handlerResult = (Boolean) httpSession.getAttribute(SESSION_KEY_REST_AUTHORITY);
        if (null != handlerResult && handlerResult) {
            return handlerResult;
        }

        String requestUrl = request.getRequestURI().replaceAll("//", "/");
        if (logger.isDebugEnabled()) {
            logger.debug("{} {}", request.getSession().getId(), requestUrl);
        }

        boolean restEnable = AppConstants.getIsSystemRestEnable();
        if (!restEnable) {
            R<Boolean> result = R.rest(false).setMsg("系统维护中，请稍后再试！");
            renderJson(request, response, result);
            return false;
        }

        // 注解处理
        handlerResult = this.defaultHandler(request, response, o, requestUrl);
        httpSession.setAttribute(SESSION_KEY_REST_AUTHORITY, handlerResult);
        if (null != handlerResult) {
            return handlerResult;
        }

        return true;
    }

    /**
     * 根据客户端请求返回（是否加密）
     * @param request
     * @param response
     * @param r
     */
    private void renderJson(HttpServletRequest request, HttpServletResponse response, R<Boolean> r) {
        String requestUrl = request.getRequestURI().replaceAll("//", "/");
        logger.warn("{} {} {}", IpUtils.getIpAddr0(request), JsonMapper.toJsonString(WebUtils.getHeaders(request)), requestUrl);
        WebUtils.renderJson(response, r);

        // 返回接口 数据解密
//        String rpcService = request.getHeader(RPCUtils.HEADER_API_SERVICE_NAME);
//        String methodName = request.getHeader(RPCUtils.HEADER_API_SERVICE_METHOD);
//        String encrypt = request.getHeader(RPCUtils.HEADER_ENCRYPT);
//        String encryptKey = request.getHeader(RPCUtils.HEADER_ENCRYPT_KEY);

//        String data = JsonMapper.toJsonString(r);
//        String encryptData = data;
        //返回数据加密
//        if (StringUtils.isNotBlank(encrypt)) {
//            if (CipherMode.SM4.name().equals(encrypt) && StringUtils.isNotBlank(encryptKey)) {
//                if (StringUtils.isNotBlank(data) && !StringUtils.equals(data, "null")) {
//                    try {
//                        String key = null;
//                        try {
//                            key = RSAUtils.decryptHexString(encryptKey, EncryptProvider.privateKeyBase64());
//                        } catch (Exception e) {
//                            key = encryptKey;
//                        }
//                        encryptData = Sm4Utils.encrypt(key, data);
//                    } catch (Exception e) {
//                        logger.error(e.getMessage(), e);
//                    }
//                }
//            } else if (CipherMode.AES.name().equals(encrypt) && StringUtils.isNotBlank(encryptKey)) {
//                if (StringUtils.isNotBlank(data) && !StringUtils.equals(data, "null")) {
//                    try {
//                        String key = null;
//                        try {
//                            key = RSAUtils.decryptBase64String(encryptKey, EncryptProvider.privateKeyBase64());
//                        } catch (Exception e) {
//                            key = encryptKey;
//                        }
//                        encryptData = Cryptos.aesECBEncryptBase64String(data, key);
//                    } catch (Exception e) {
//                        logger.error(e.getMessage(), e);
//                    }
//                }
//
//            } else if (CipherMode.BASE64.name().equals(encrypt)) {
//                if (StringUtils.isNotBlank(data) && !StringUtils.equals(data, "null")) {
//                    try {
//                        encryptData = EncodeUtils.base64Encode(data.getBytes(StandardCharsets.UTF_8));
//                    } catch (Exception e) {
//                        logger.error(e.getMessage(), e);
//                    }
//                }
//
//            }
//        }

//        WebUtils.renderJson(response, encryptData);
    }

    /**
     * 注解处理（带缓存机制）
     * @param request
     * @param response
     * @param handler
     * @param requestUrl
     * @return
     * @throws Exception
     */
    private Boolean defaultHandler(HttpServletRequest request, HttpServletResponse response, Object handler, String requestUrl) throws Exception {
        HandlerMethod handlerMethod = null;
        if (handler instanceof HandlerMethod) {
            handlerMethod = (HandlerMethod) handler;
        }

        if (handlerMethod != null) {
            // 优先获取缓存中的注解解析元数据
            RestAnnotationMetadata metadata = restAnnotationCache.computeIfAbsent(handlerMethod, this::parseRestAnnotationMetadata);

            if (metadata.restApi != null) {
                // 方法/类注解配置处理：未开启 required，直接放行
                if (!metadata.restApiRequired) {
                    return true;
                }
                if (metadata.requiresUserSkip) {
                    return true;
                }

                // IP访问限制
                String ip = IpUtils.getIpAddr0(request);
                if (checkIpLimit(ip)) {
                    notPermittedPermission(request, response, requestUrl, "REST禁止访问：" + ip);
                    return false;
                }

                // 请求密钥
                String authType = request.getHeader(RPCUtils.HEADER_AUTH_TYPE);
                String encrypt = request.getHeader(RPCUtils.HEADER_ENCRYPT);
                String apiKey = request.getHeader(RPCUtils.HEADER_X_API_KEY);
                String applicationId = request.getHeader(RPCUtils.HEADER_APPLICATION_ID);
                if (null == apiKey) {
                    notPermittedPermission(request, response, requestUrl, "未识别参数:Header['X-API-Key']=" + apiKey);
                    return false;
                }

                // 密钥认证
                String DEFAULT_API_KEY = AppConstants.getRestDefaultApiKey();
                if (!DEFAULT_API_KEY.equals(apiKey)) {
                    notPermittedPermission(request, response, requestUrl, "未授权访问:Header['X-API-Key']=" + apiKey);
                    return false;
                }

                HttpSession httpSession = request.getSession();
                String suffix = Optional.ofNullable(applicationId).map(id -> "[" + id + "]").orElse("");
                httpSession.setAttribute(SESSION_TAG_NAME, SYSTEM_PREFIX_NAME + suffix);
                httpSession.setAttribute(RPCUtils.HEADER_AUTH_TYPE, authType);
                httpSession.setAttribute(RPCUtils.HEADER_ENCRYPT, encrypt);
                return true;
            }
        }
        return null;
    }

    /**
     * 解析 HandlerMethod 及 Class 上的 RestApi 和 RequiresUser 注解（首次调用时触发）
     */
    private RestAnnotationMetadata parseRestAnnotationMetadata(HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();

        // 获取 RestApi 注解（方法优先，类注解兜底）
        RestApi restApi = handlerMethod.getMethodAnnotation(RestApi.class);
        if (restApi == null) {
            restApi = AppUtils.getAnnotation(beanType, RestApi.class);
        }

        // 获取 RequiresUser 注解（方法优先，类注解兜底）
        RequiresUser requiresUser = handlerMethod.getMethodAnnotation(RequiresUser.class);
        if (requiresUser == null) {
            requiresUser = AppUtils.getAnnotation(beanType, RequiresUser.class);
        }

        boolean restApiRequired = restApi != null && restApi.required();
        boolean requiresUserSkip = requiresUser != null && !requiresUser.required();

        return new RestAnnotationMetadata(restApi, restApiRequired, requiresUserSkip);
    }

    private boolean checkIpLimit(String ip) {
        // IP访问限制
        boolean isRestLimitEnable = AppConstants.getIsSystemRestLimitEnable();
        boolean isLimit = false;
        if (isRestLimitEnable) {
            isLimit = true;
            List<String> ipList = AppConstants.getRestLimitIpWhiteList();
            if (Collections3.isNotEmpty(ipList) && (null == ipList.stream().filter(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip)).findAny().orElse(null))) {
                isLimit = false;
            }
            if ("127.0.0.1".equals(ip) || "localhost".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                isLimit = false;
            }
        }
        return isLimit;
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
        R<Boolean> result = new R<>(false).setCode(R.NO_PERMISSION).setMsg(msg);
        renderJson(request, response, result);
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