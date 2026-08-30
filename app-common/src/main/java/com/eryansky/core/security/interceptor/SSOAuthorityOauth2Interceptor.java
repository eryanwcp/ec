/**
 * Copyright (c) 2012-2026 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security.interceptor;

import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;


/**
 * 外部用户传递用户单点登录 权限拦截器
 * @author Eryan
 * @date 2026-05-25
 */
public class SSOAuthorityOauth2Interceptor implements AsyncHandlerInterceptor {


    protected Logger logger = LoggerFactory.getLogger(getClass());

    private static final String ATTR_SESSIONINFO = "sessionInfo";
    private static final String REQUEST_NAME = "sso_token";
    private static final String REQUEST_NAME_PRIVATE = "ec_sso_token";
    private String redirectURL = "/";//登录验证地址
    private List<String> excludeUrls = Lists.newArrayList();// 不需要拦截的资源

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String token = WebUtils.getParameter(request,REQUEST_NAME,REQUEST_NAME_PRIVATE);
        if(StringUtils.isBlank(token)){
            token = WebUtils.getHeader(request,REQUEST_NAME,REQUEST_NAME_PRIVATE);
        }

        if (StringUtils.isNotBlank(token)) {//包含该参数，则为单点登录
            logger.debug("token:{}", token);
            //登录用户
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            request.setAttribute(ATTR_SESSIONINFO, sessionInfo);
            if (sessionInfo != null) {
                return true;
            }

            String requestUrl = request.getRequestURI().replace("//", "/");
            //数据库处理
            // 不拦截的URL
            if (Collections3.isNotEmpty(excludeUrls)) {
                for (String excludeUrl : excludeUrls) {
                    boolean flag = StringUtils.simpleWildcardMatch(excludeUrl, requestUrl);
                    if (flag) {
                        return true;
                    }
                }
            }
            String msg = null;
            String json;
            try {
                json = Sm4Utils.decrypt(EncryptProvider.aesKey(), token);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 非法或已被篡改");
                return false;
            }

            // 2. 解析字段
            Map<String, Object> payload = JsonMapper.getInstance().toMap(json);
            if (payload == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 非法或已被篡改：" + token);
                return false;
            }

            // 3. 有效时间校验
            Object expObj = payload.get("exp");
            if (expObj == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 缺失过期时间字段");
                return false;
            }

            long exp;
            try {
                exp = Long.parseLong(String.valueOf(expObj));
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 过期时间格式错误");
                return false;
            }

            if (System.currentTimeMillis() > exp) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 已过期");
                return false;
            }

            // 4. 签发者必须是我们信任的认证中心 或自己签发
            if (!(AppConstants.getSSOIssuer().equals(payload.get("iss")) || StringUtils.isEquals(SpringContextHolder.getApplicationContext().getId(), (String) payload.get("iss")))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "签发者不合法");
                return false;
            }

            // 5. 校验通过，把用户信息放进自己的会话，从此跟 A 没关系
            String loginName = (String) payload.get("username");
            User user = UserUtils.getUserByLoginName(loginName);
            if (null == user) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户不存在：" + loginName);
                return false;
            }
            if (StatusState.LOCK.getValue().equals(user.getStatus())) {
                msg = "用户[" + loginName + "]已被锁定";
                logger.warn("统一平台单点登录失败，{}，跳转到页面:{}.", msg, redirectURL);
                response.sendRedirect(request.getContextPath() + redirectURL);
                return false; // 返回到登录页面
            }

            //单点登录成功 将用户信息放入session中
            SecurityUtils.putUserToSession(request, user);
            UserUtils.recordLogin(user.getId());
            sessionInfo = SecurityUtils.getCurrentSessionInfo();
            request.setAttribute(ATTR_SESSIONINFO, sessionInfo);
            logger.info("统一平台单点登录成功：{}，IP:{}", user.getLoginName(), IpUtils.getIpAddr(request));
            return true;
        }
        return true;
    }


    public void postHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        if (e != null) {

        }
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public SSOAuthorityOauth2Interceptor setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
        return this;
    }

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public SSOAuthorityOauth2Interceptor setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
        return this;
    }

    public SSOAuthorityOauth2Interceptor addExcludeUrl(String excludeUrl) {
        this.excludeUrls.add(excludeUrl);
        return this;
    }
}
