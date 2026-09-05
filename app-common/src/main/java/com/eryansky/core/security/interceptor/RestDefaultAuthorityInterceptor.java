/**
 * Copyright (c) 2012-2026 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security.interceptor;

import com.eryansky.common.model.R;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.rpc.advice.EncryptRPCResponseBodyAdvice;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.core.security.jwt.JWTUtils;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.util.RequestEncryptUtils;
import com.eryansky.modules.sys.vo.OAuth2Client;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rest权限拦截器（注解缓存优化版）
 *
 * @author Eryan
 * @date 2020-09-09
 */
public class RestDefaultAuthorityInterceptor implements AsyncHandlerInterceptor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String SESSION_KEY_REST_AUTHORITY = "REST_AUTHORITY";
    public static final String SESSION_TAG_NAME = "loginUser";
    public static final String SYSTEM_PREFIX_NAME = "内部系统";
    public static final String ACCESS_TOKEN = "access_token";

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
        final boolean defaultEncryptResponseBody;

        public RestAnnotationMetadata(RestApi restApi, boolean restApiRequired, boolean requiresUserSkip, boolean defaultEncryptResponseBody) {
            this.restApi = restApi;
            this.restApiRequired = restApiRequired;
            this.requiresUserSkip = requiresUserSkip;
            this.defaultEncryptResponseBody = defaultEncryptResponseBody;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession httpSession = request.getSession();
        Boolean handlerResult = (Boolean) httpSession.getAttribute(SESSION_KEY_REST_AUTHORITY);
        if (Boolean.TRUE.equals(handlerResult)) {
            return true;
        }

        String requestUrl = request.getRequestURI().replace("//", "/");
        if (logger.isDebugEnabled()) {
            logger.debug("{} {}", httpSession.getId(), requestUrl);
        }

        HandlerMethod handlerMethod = (handler instanceof HandlerMethod) ? (HandlerMethod) handler : null;
        if (handlerMethod == null) {
            return true;
        }

        RestAnnotationMetadata metadata = restAnnotationCache.computeIfAbsent(handlerMethod, this::parseRestAnnotationMetadata);

        boolean restEnable = AppConstants.getIsSystemRestEnable();
        if (!restEnable) {
            R<Boolean> result = R.fail(false, "系统维护中，请稍后再试！");
            renderJson(request, response, result, metadata.defaultEncryptResponseBody);
            return false;
        }

        // 注解校验处理
        handlerResult = this.defaultHandler(request, response, metadata, requestUrl);
        if (handlerResult != null) {
            httpSession.setAttribute(SESSION_KEY_REST_AUTHORITY, handlerResult);
            return handlerResult;
        }

        return true;
    }

    /**
     * 注解校验处理
     */
    private Boolean defaultHandler(HttpServletRequest request, HttpServletResponse response, RestAnnotationMetadata metadata, String requestUrl) throws Exception {
        if (metadata.restApi == null) {
            return null;
        }

        // 未开启 required 或明确设置 skip requiresUser，直接放行
        if (!metadata.restApiRequired || metadata.requiresUserSkip) {
            return true;
        }

        // 1. 全局 IP 访问限制
        String ip = IpUtils.getIpAddr0(request);
        if (checkGlobalIpLimit(ip)) {
            notPermittedPermission(request, response, requestUrl, "REST禁止访问：" + ip, metadata.defaultEncryptResponseBody);
            return false;
        }

        // 2. 认证类型与密钥校验
        String authType = WebUtils.getHeaderIgnoreCase(request, RPCUtils.HEADER_AUTH_TYPE);
        String encrypt = WebUtils.getHeaderIgnoreCase(request, RPCUtils.HEADER_ENCRYPT);
        String apiKey = WebUtils.getHeaderIgnoreCase(request, RPCUtils.HEADER_X_API_KEY);
        String applicationId = WebUtils.getHeaderIgnoreCase(request, RPCUtils.HEADER_APPLICATION_ID);

        // 内置 Auth 认证
        if (RPCUtils.AUTH_TYPE.equals(authType)) {
            if (apiKey == null) {
                notPermittedPermission(request, response, requestUrl, "未识别参数:Header['" + RPCUtils.HEADER_X_API_KEY + "']", metadata.defaultEncryptResponseBody);
                return false;
            }
            String defaultApiKey = AppConstants.getRestDefaultApiKey();
            if (!defaultApiKey.equals(apiKey)) {
                notPermittedPermission(request, response, requestUrl, "未授权访问:Header['" + RPCUtils.HEADER_X_API_KEY + "']=" + apiKey, metadata.defaultEncryptResponseBody);
                return false;
            }
        }
        // AccessToken 认证
        else if ("accessToken".equals(authType)) {
            String accessToken = WebUtils.getHeaderIgnoreCaseOrParameter(request, ACCESS_TOKEN);
            if (accessToken == null) {
                notPermittedPermission(request, response, requestUrl, "未识别参数:Header['" + ACCESS_TOKEN + "']", metadata.defaultEncryptResponseBody);
                return false;
            }

            String clientId;
            try {
                clientId = JWTUtils.getUsername(accessToken);
            } catch (Exception e) {
                notPermittedPermission(request, response, requestUrl, "AccessToken格式无效", metadata.defaultEncryptResponseBody);
                return false;
            }

            applicationId = clientId;
            List<OAuth2Client> oauth2Clients = AppConstants.getOauth2ClientList();
            OAuth2Client oAuth2Client = oauth2Clients.stream()
                    .filter(v -> StringUtils.isEquals(v.getClientId(), clientId))
                    .findFirst()
                    .orElse(null);

            if (oAuth2Client == null) {
                notPermittedPermission(request, response, requestUrl, "未授权应用：" + clientId, metadata.defaultEncryptResponseBody);
                return false;
            }

            // 应用客户端 IP 校验
            if (!isClientIpAllowed(ip, oAuth2Client.getClientIps())) {
                notPermittedPermission(request, response, requestUrl, "REST禁止访问：" + clientId + "," + ip, metadata.defaultEncryptResponseBody);
                return false;
            }

            boolean verify = JWTUtils.verify(accessToken, clientId, oAuth2Client.getClientSecret());
            if (!verify) {
                notPermittedPermission(request, response, requestUrl, "未授权应用：AccessToken无效" + clientId, metadata.defaultEncryptResponseBody);
                return false;
            }
        } else {
            notPermittedPermission(request, response, requestUrl, "未识别参数:Header['" + RPCUtils.HEADER_AUTH_TYPE + "']", metadata.defaultEncryptResponseBody);
            return false;
        }

        // 认证通过，保存 Session 信息
        HttpSession httpSession = request.getSession();
        String suffix = Optional.ofNullable(applicationId).map(id -> "[" + id + "]").orElse("");
        httpSession.setAttribute(SESSION_TAG_NAME, SYSTEM_PREFIX_NAME + suffix);
        httpSession.setAttribute(RPCUtils.HEADER_AUTH_TYPE, authType);
        httpSession.setAttribute(RPCUtils.HEADER_ENCRYPT, encrypt);

        return true;
    }

    /**
     * 根据客户端请求返回 JSON（判断是否需要加密）
     */
    private void renderJson(HttpServletRequest request, HttpServletResponse response, R<Boolean> r, boolean defaultEncryptResponseBody) {
        String requestUrl = request.getRequestURI().replace("//", "/");
        logger.warn("{} {} {}", IpUtils.getIpAddr0(request), JsonMapper.toJsonString(WebUtils.getHeaders(request)), requestUrl);

        String encrypt = WebUtils.getHeaderIgnoreCase(request, RPCUtils.HEADER_ENCRYPT);
        String encryptKey = WebUtils.getHeaderIgnoreCase(request, RPCUtils.HEADER_ENCRYPT_KEY);

        if (defaultEncryptResponseBody && StringUtils.isNotBlank(encrypt) && StringUtils.isNotBlank(encryptKey)) {
            try {
                byte[] encryptData = RequestEncryptUtils.encryptDataByRequest(encrypt, encryptKey, JsonMapper.getInstance().writeValueAsBytes(r));
                WebUtils.render(response, WebUtils.JSON_TYPE, encryptData);
                return;
            } catch (Exception e) {
                logger.error("加密渲染响应失败: {}", e.getMessage(), e);
                WebUtils.renderJson(response, r);
                return;
            }
        }

        WebUtils.renderJson(response, r);
    }

    /**
     * 解析 HandlerMethod 及 Class 上的 RestApi 和 RequiresUser 注解
     */
    private RestAnnotationMetadata parseRestAnnotationMetadata(HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();

        RestApi restApi = handlerMethod.getMethodAnnotation(RestApi.class);
        if (restApi == null) {
            restApi = AppUtils.getAnnotation(beanType, RestApi.class);
        }

        RequiresUser requiresUser = handlerMethod.getMethodAnnotation(RequiresUser.class);
        if (requiresUser == null) {
            requiresUser = AppUtils.getAnnotation(beanType, RequiresUser.class);
        }

        EncryptResponseBody encryptResponseBody = handlerMethod.getMethodAnnotation(EncryptResponseBody.class);
        if (encryptResponseBody == null) {
            encryptResponseBody = AppUtils.getAnnotation(beanType, EncryptResponseBody.class);
        }

        boolean restApiRequired = restApi != null && restApi.required();
        boolean requiresUserSkip = requiresUser != null && !requiresUser.required();
        boolean defaultEncryptResponseBody = encryptResponseBody != null
                && Boolean.parseBoolean(encryptResponseBody.enable())
                && (encryptResponseBody.handle() == EncryptRPCResponseBodyAdvice.class);

        return new RestAnnotationMetadata(restApi, restApiRequired, requiresUserSkip, defaultEncryptResponseBody);
    }

    /**
     * 全局 IP 是否被限制拦截
     */
    private boolean checkGlobalIpLimit(String ip) {
        boolean isRestLimitEnable = AppConstants.getIsSystemRestLimitEnable();
        if (!isRestLimitEnable) {
            return false;
        }

        // 包含常见的 IPv4 与 IPv6 回环地址
        if ("127.0.0.1".equals(ip) || "localhost".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return false;
        }

        List<String> ipList = AppConstants.getRestLimitIpWhiteList();
        if (Collections3.isNotEmpty(ipList)) {
            boolean matches = ipList.stream().anyMatch(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip));
            return !matches; // 不在白名单内，表示被限制访问
        }

        return false;
    }

    /**
     * 客户端 IP 是否在允许的白名单内
     */
    private boolean isClientIpAllowed(String ip, List<String> configWhiteList) {
        if (CollectionUtils.isEmpty(configWhiteList)) {
            return true;
        }
        return configWhiteList.stream().anyMatch(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip));
    }

    /**
     * 未授权/拒绝权限响应处理
     */
    private void notPermittedPermission(HttpServletRequest request, HttpServletResponse response, String requestUrl, String msg, boolean defaultEncryptResponseBody) throws ServletException, IOException {
        R<Boolean> result = new R<>(false).setCode(R.NO_PERMISSION).setMsg(msg);
        renderJson(request, response, result, defaultEncryptResponseBody);
    }
}