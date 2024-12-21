//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.eryansky.core.security.interceptor;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.modules.sys._enum.UserType;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.google.common.collect.Lists;
import java.lang.annotation.Annotation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class AuthorityOauth2Interceptor implements AsyncHandlerInterceptor {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<String> excludeUrls = Lists.newArrayList();

    public AuthorityOauth2Interceptor() {
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getParameter("Authorization");
        if (StringUtils.isBlank(authorization)) {
            authorization = request.getHeader("Authorization");
        }

        String token = StringUtils.replaceOnce(StringUtils.replaceOnce(authorization, "Bearer ", ""),"Bearer","");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (null != sessionInfo && request.getSession().getId().equals(sessionInfo.getId()) && null != UserType.getByValue(sessionInfo.getUserType())) {
            if (StringUtils.isBlank(token) || StringUtils.equals(token, sessionInfo.getToken()) || StringUtils.equals(token, sessionInfo.getRefreshToken())) {
                return true;
            }

            sessionInfo = null;
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            PrepareOauth2 prepareOauth2Method = (PrepareOauth2)handlerMethod.getMethodAnnotation(PrepareOauth2.class);
            PrepareOauth2 prepareOauth2Class = (PrepareOauth2)this.getAnnotation(handlerMethod.getBean().getClass(), PrepareOauth2.class);
            if (prepareOauth2Method != null && !prepareOauth2Method.enable() || prepareOauth2Class != null && !prepareOauth2Class.enable()) {
                return true;
            }

            String authType = null != prepareOauth2Method ? prepareOauth2Method.authType() : null;
            if (null == authType) {
                authType = null != prepareOauth2Class ? prepareOauth2Class.authType() : null;
            }

            if (null != authType && !"user".equals(authType)) {
                return true;
            }

            if (StringUtils.isNotBlank(authorization)) {
                String requestUrl = request.getRequestURI();
                boolean verify = false;
                if (null == sessionInfo) {
                    sessionInfo = SecurityUtils.getSessionInfoByTokenOrRefreshToken(token);
                }

                if (null != sessionInfo && null == UserType.getByValue(sessionInfo.getUserType())) {
                    return true;
                }

                String loginName = null;
                User user = null;

                try {
                    loginName = SecurityUtils.getLoginNameByToken(token);
                    user = UserUtils.getUserByLoginName(loginName);
                    if (null == user) {
                        this.logger.warn("Token校验失败（用户不存在）：{},{},{}", new Object[]{loginName, requestUrl, token});
                        return true;
                    }

                    verify = SecurityUtils.verifySessionInfoToken(token, loginName, user.getPassword());
                } catch (Exception var16) {
                    if (!(var16 instanceof TokenExpiredException)) {
                        this.logger.error("Token校验失败：{},{},{},{},{}", new Object[]{loginName, SpringMVCHolder.getIp(), requestUrl, token, var16.getMessage()});
                    }
                }

                if (verify) {
                    if (null != sessionInfo) {
                        SecurityUtils.addExtendSession(request.getSession().getId(), sessionInfo.getId());
                        this.logger.debug("自动跳过登录：{},{},{},{},{}", new Object[]{loginName, IpUtils.getIpAddr0(request), requestUrl, request.getSession().getId(), sessionInfo.getId()});
                    } else {
                        SecurityUtils.putUserToSession(request, user);
                        UserUtils.recordLogin(user.getId());
                        this.logger.debug("自动登录成功：{},{},{}", new Object[]{loginName, IpUtils.getIpAddr0(request), requestUrl});
                    }
                }
            }
        }

        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        if (e != null) {
        }

    }

    public List<String> getExcludeUrls() {
        return this.excludeUrls;
    }

    public AuthorityOauth2Interceptor setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
        return this;
    }

    public AuthorityOauth2Interceptor addExcludeUrl(String excludeUrl) {
        this.excludeUrls.add(excludeUrl);
        return this;
    }

    private <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationType) {
        T result = clazz.getAnnotation(annotationType);
        if (result == null) {
            Class<?> superclass = clazz.getSuperclass();
            return superclass != null ? this.getAnnotation(superclass, annotationType) : null;
        } else {
            return result;
        }
    }
}
