/**
 * Copyright (c) 2012-2018 http://www.eryansky.com
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;


/**
 * Rest权限拦截器
 * @author Eryan
 * @date 2020-09-09
 */
public class RestDefaultAuthorityInterceptor implements AsyncHandlerInterceptor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public static final String SESSION_KEY_REST_AUTHORITY = "REST_AUTHORITY";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        HttpSession httpSession = request.getSession();
        Boolean handlerResult = (Boolean) httpSession.getAttribute(SESSION_KEY_REST_AUTHORITY);
        if (null != handlerResult && handlerResult) {
            return handlerResult;
        }
        String requestUrl = request.getRequestURI().replaceAll("//", "/");
        if (logger.isDebugEnabled()) {
            logger.debug("{} {}",request.getSession().getId(),requestUrl);
        }
        boolean restEnable = AppConstants.getIsSystemRestEnable();
        if (!restEnable) {
            R<Boolean> result = R.rest(false).setMsg("系统维护中，请稍后再试！");
            renderJson(request,response, result);
            return false;
        }

        //注解处理
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
    private void renderJson(HttpServletRequest request, HttpServletResponse response, R<Boolean> r){
        String requestUrl = request.getRequestURI().replaceAll("//", "/");
        logger.warn("{} {} {}",IpUtils.getIpAddr0(request) ,JsonMapper.toJsonString(WebUtils.getHeaders(request)),requestUrl);
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
     * 注解处理
     * @param request
     * @param response
     * @param handler
     * @param requestUrl
     * @return
     * @throws Exception
     */
    private Boolean defaultHandler(HttpServletRequest request, HttpServletResponse response, Object handler, String requestUrl) throws Exception {
        HandlerMethod handlerMethod = null;
        //注解处理 满足设置不拦截
        if(handler instanceof HandlerMethod) {
            handlerMethod = (HandlerMethod) handler;
        }

        if (handlerMethod != null) {
            Object bean = handlerMethod.getBean();
            //权限校验
            RestApi restApi = handlerMethod.getMethodAnnotation(RestApi.class);
            if (restApi == null) {
                restApi = this.getAnnotation(bean.getClass(), RestApi.class);
            }
            RequiresUser requiresUser = handlerMethod.getMethodAnnotation(RequiresUser.class);
            if (requiresUser == null) {
                requiresUser = this.getAnnotation(bean.getClass(), RequiresUser.class);
            }

            if (restApi != null) {//方法注解处理
                if (!restApi.required()) {
                    return true;
                }

                if (null != requiresUser && !requiresUser.required()) {
                    return true;
                }
                //IP访问限制
                String ip = IpUtils.getIpAddr0(request);
                if (checkIpLimit(ip)) {
                    notPermittedPermission(request, response, requestUrl, "REST禁止访问：" + ip);
                    return false;
                }
                //请求密钥
                String authType = request.getHeader(RPCUtils.HEADER_AUTH_TYPE);
                String apiKey = request.getHeader(RPCUtils.HEADER_X_API_KEY);
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
                request.getSession().setAttribute("loginUser","内部系统[" + ip + "]");
                return true;
            }

        }
        return null;
    }

    private boolean checkIpLimit(String ip){
        //IP访问限制
        boolean isRestLimitEnable = AppConstants.getIsSystemRestLimitEnable();
        boolean isLimit = false;
        if (isRestLimitEnable) {
            isLimit = true;
            List<String> ipList = AppConstants.getRestLimitIpWhiteList();
            if (Collections3.isNotEmpty(ipList) && (null == ipList.stream().filter(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip)).findAny().orElse(null))) {
                isLimit = false;
            }
            if("127.0.0.1".equals(ip) || "localhost".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)){
                isLimit = false;
            }


        }
        return  isLimit;
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
//        response.setStatus(HttpStatus.FORBIDDEN.value());
        R<Boolean> result = new R<>(false).setCode(R.NO_PERMISSION).setMsg(msg);
        renderJson(request,response, result);
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
