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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    // 提取的 JSON 字段常量
    private static final String CLAIM_EXP = "exp";
    private static final String CLAIM_ISS = "iss";
    private static final String CLAIM_USERNAME = "username";

    private String redirectURL = "/"; // 登录验证地址
    private List<String> excludeUrls = Lists.newArrayList(); // 不需要拦截的资源

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String token = WebUtils.getParameter(request, REQUEST_NAME, REQUEST_NAME_PRIVATE);
        if (StringUtils.isBlank(token)) {
            token = WebUtils.getHeader(request, REQUEST_NAME, REQUEST_NAME_PRIVATE);
        }

        // 1. 卫语句：如果不包含 Token，直接放行（交给后续权限拦截器处理）
        if (StringUtils.isBlank(token)) {
            return true;
        }

        logger.debug("token:{}", token);

        // 2. 检查用户是否已登录
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        request.setAttribute(ATTR_SESSIONINFO, sessionInfo);
        if (sessionInfo != null) {
            return true;
        }

        // 3. 不拦截的 URL (白名单) 校验
        String requestUrl = request.getRequestURI().replace("//", "/");
        if (Collections3.isNotEmpty(excludeUrls)) {
            for (String excludeUrl : excludeUrls) {
                if (StringUtils.simpleWildcardMatch(excludeUrl, requestUrl)) {
                    return true;
                }
            }
        }

        // 4. Token 解密
        String json;
        try {
            json = Sm4Utils.decrypt(EncryptProvider.aesKey(), token);
        } catch (Exception e) {
            logger.warn("SSO Token 解密失败, token: {}", token); // 增加日志记录
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SSO Token 非法或已被篡改");
            return false;
        }

        // 5. 解析 Payload 字段
        Map<String, Object> payload = JsonMapper.getInstance().toMap(json);
        if (payload == null) {
            logger.warn("SSO Token 格式异常: {}", json);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SSO Token 格式异常");
            return false;
        }

        // 6. 有效时间 (exp) 校验
        Object expObj = payload.get(CLAIM_EXP);
        if (expObj == null) {
            logger.warn("SSO Token 缺失过期时间字段: {}", json);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SSO Token 缺失过期时间字段");
            return false;
        }

        long exp;
        try {
            // 优化：优先使用 Number 类型转换，避免多余的 String 解析
            exp = (expObj instanceof Number) ? ((Number) expObj).longValue() : Long.parseLong(String.valueOf(expObj));
        } catch (NumberFormatException e) {
            logger.warn("SSO Token 过期时间格式错误: {}", json);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "SSO Token 过期时间格式错误");
            return false;
        }

        if (System.currentTimeMillis() > exp) {
            logger.warn("Token 已过期: {}", json);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 已过期");
            return false;
        }

        // 7. 签发者 (iss) 校验
        String iss = (String) payload.get(CLAIM_ISS);
        String currentAppId = SpringContextHolder.getApplicationContext().getId();
        if (!(AppConstants.getSSOIssuer().equals(iss) || StringUtils.isEquals(currentAppId, iss))) {
            logger.warn("Token 签发者不合法: {}", iss);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "签发者不合法");
            return false;
        }

        // 8. 用户身份校验
        String loginName = (String) payload.get(CLAIM_USERNAME);
        User user = UserUtils.getUserByLoginName(loginName);
        if (user == null) {
            logger.warn("SSO Token校验失败，用户不存在: {}", loginName);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token校验失败，用户不存在：" + loginName);
            return false;
        }

        // 9. 用户状态检查
        if (StatusState.LOCK.getValue().equals(user.getStatus())) {
            String msg = "用户[" + loginName + "]已被锁定"; // 缩小 msg 的作用域
            logger.warn("统一平台单点登录失败，{}，跳转到页面:{}.", msg, redirectURL);
            response.sendRedirect(request.getContextPath() + redirectURL);
            return false;
        }

        // 10. 校验通过，写入 Session 
        SecurityUtils.putUserToSession(request, user);
        UserUtils.recordLogin(user.getId());

        sessionInfo = SecurityUtils.getCurrentSessionInfo();
        request.setAttribute(ATTR_SESSIONINFO, sessionInfo);

        logger.info("统一平台单点登录成功：{}，IP:{}", user.getLoginName(), IpUtils.getIpAddr(request));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

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
