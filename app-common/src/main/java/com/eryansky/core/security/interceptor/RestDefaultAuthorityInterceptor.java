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
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.core.security.jwt.JWTUtils;
import com.eryansky.modules.sys.vo.OAuth2Client;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    protected Logger logger = LoggerFactory.getLogger(getClass());

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

        String requestUrl = request.getRequestURI().replace("//", "/");
        if (logger.isDebugEnabled()) {
            logger.debug("{} {}", request.getSession().getId(), requestUrl);
        }

        boolean restEnable = AppConstants.getIsSystemRestEnable();
        if (!restEnable) {
            R<Boolean> result = R.fail(false, "系统维护中，请稍后再试！");
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
     *
     * @param request
     * @param response
     * @param r
     */
    private void renderJson(HttpServletRequest request, HttpServletResponse response, R<Boolean> r) {
        String requestUrl = request.getRequestURI().replace("//", "/");
        logger.warn("{} {} {}", IpUtils.getIpAddr0(request), JsonMapper.toJsonString(WebUtils.getHeaders(request)), requestUrl);
//        WebUtils.renderJson(response, r);

        // 返回接口 数据加密
        String encrypt = WebUtils.getHeaderIgnoreCase(request,RPCUtils.HEADER_ENCRYPT);
        String encryptKey = WebUtils.getHeaderIgnoreCase(request,RPCUtils.HEADER_ENCRYPT_KEY);
        if(StringUtils.isNotBlank(encrypt) && StringUtils.isNotBlank(encryptKey)){
            try {
                byte[] encryptData = RPCUtils.encryptDataByRequest(encrypt,encryptKey,JsonMapper.getInstance().writeValueAsBytes(r));
                WebUtils.render(response, WebUtils.JSON_TYPE,encryptData);
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                WebUtils.renderJson(response, r);
            }
        }
    }

    /**
     * 注解处理（带缓存机制）
     *
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

                // IP访问限制 全局
                String ip = IpUtils.getIpAddr0(request);
                if (checkGobalIpLimit(ip)) {
                    notPermittedPermission(request, response, requestUrl, "REST禁止访问：" + ip);
                    return false;
                }

                // 请求密钥
                String authType = WebUtils.getHeaderIgnoreCase(request,RPCUtils.HEADER_AUTH_TYPE);
                String encrypt = WebUtils.getHeaderIgnoreCase(request,RPCUtils.HEADER_ENCRYPT);
                String apiKey = WebUtils.getHeaderIgnoreCase(request,RPCUtils.HEADER_X_API_KEY);
                String applicationId = WebUtils.getHeaderIgnoreCase(request,RPCUtils.HEADER_APPLICATION_ID);

                //内置认证
                if (RPCUtils.AUTH_TYPE.equals(authType)) {
                    if (null == apiKey) {
                        notPermittedPermission(request, response, requestUrl, "未识别参数:Header['" + RPCUtils.HEADER_X_API_KEY + "']");
                        return false;
                    }
                    // 密钥认证
                    String DEFAULT_API_KEY = AppConstants.getRestDefaultApiKey();
                    if (!DEFAULT_API_KEY.equals(apiKey)) {
                        notPermittedPermission(request, response, requestUrl, "未授权访问:Header['" + RPCUtils.HEADER_X_API_KEY + "']=" + apiKey);
                        return false;
                    }
                } else if ("accessToken".equals(authType)) {
                    String accessToken = WebUtils.getHeaderIgnoreCaseOrParameter(request, ACCESS_TOKEN);
                    if (null == accessToken) {
                        notPermittedPermission(request, response, requestUrl, "未识别参数:Header['" + ACCESS_TOKEN + "']");
                        return false;
                    }
                    String clientId = JWTUtils.getUsername(accessToken);
                    applicationId = clientId;
                    List<OAuth2Client> oauth2Clients = AppConstants.getOauth2ClientList();
                    OAuth2Client oAuth2Client = oauth2Clients.stream().filter(v -> StringUtils.isEquals(v.getClientId(), clientId)).findFirst().orElse(null);
                    if (oAuth2Client == null) {
                        notPermittedPermission(request, response, requestUrl, "未授权应用：" + clientId);
                        return false;
                    }
                    //应用IP校验
                    if (checkClientIpLimit(ip, oAuth2Client.getClientIps())) {
                        notPermittedPermission(request, response, requestUrl, "REST禁止访问：" + clientId + "," + ip);
                        return false;
                    }
                    boolean verify = JWTUtils.verify(accessToken, clientId, oAuth2Client.getClientSecret());

                    if (!verify) {
                        notPermittedPermission(request, response, requestUrl, "未授权应用：AccessToken无效" + clientId);
                        return false;
                    }
                } else {
                    notPermittedPermission(request, response, requestUrl, "未识别参数:Header['" + RPCUtils.HEADER_AUTH_TYPE + "']");
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

    /**
     * 全局白名单
     *
     * @param ip
     * @return
     */
    private boolean checkGobalIpLimit(String ip) {
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
     * 客户端应用白名单
     *
     * @param ip
     * @param configWhiteList
     * @return
     */
    private boolean checkClientIpLimit(String ip, List<String> configWhiteList) {
        if (!CollectionUtils.isEmpty(configWhiteList)) {
            boolean isAllowedIp = configWhiteList.stream()
                    .anyMatch(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip));
            if (!isAllowedIp) {
                return false;
            }
        }
        return true;
    }

    /**
     * 未授权权限
     *
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