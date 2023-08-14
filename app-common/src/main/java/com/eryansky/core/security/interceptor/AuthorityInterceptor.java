/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security.interceptor;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.google.common.collect.Lists;
import com.eryansky.core.security.SecurityConstants;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security._enum.Logical;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.security.annotation.RequiresRoles;
import com.eryansky.core.security.annotation.RequiresUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
 * 权限拦截器
 * 优先级：注解>数据库权限配置
 * @author Eryan
 * @date 2015-01-21 12:23
 */
public class AuthorityInterceptor implements AsyncHandlerInterceptor {


    protected Logger logger = LoggerFactory.getLogger(getClass());

    public static final String ATTR_SESSIONINFO = "sessionInfo";
    public static final String ATTR_AUTHORIZATION = "Authorization";


    /**
     * 登录验证地址
     */
    private String redirectURL = "/";
    /**
     * 不需要拦截的资源
     */
    private List<String> excludeUrls = Lists.newArrayList();
    /**
     * 验证数据库标记URL 默认值：false
     */
    private Boolean authorMarkUrl = false;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        //登录用户
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        request.setAttribute(ATTR_SESSIONINFO,sessionInfo);
        if(null != sessionInfo){
            response.setHeader(ATTR_AUTHORIZATION, sessionInfo.getToken());
        }
        String requestUrl = request.getRequestURI();
        requestUrl = requestUrl.replaceAll("//","/");
        if(logger.isDebugEnabled()){
            logger.debug(request.getSession().getId() + ":" + request.getHeader("Authorization") + ":" + requestUrl);
        }
        //注解处理
        Boolean annotationHandler = this.annotationHandler(request,response,o,sessionInfo,requestUrl);
        if(annotationHandler != null){
            return annotationHandler;
        }
        //数据库处理
        return this.dbHandler(request,response,o,sessionInfo,requestUrl);
    }


    /**
     * 注解处理
     * @param request
     * @param response
     * @param handler
     * @param sessionInfo
     * @param requestUrl
     * @return
     * @throws Exception
     */
    private Boolean annotationHandler(HttpServletRequest request, HttpServletResponse response, Object handler,
                                      SessionInfo sessionInfo,String requestUrl) throws Exception {
        HandlerMethod handlerMethod = null;
        //注解处理 满足设置不拦截
        if(handler instanceof HandlerMethod) {
            handlerMethod = (HandlerMethod) handler;
        }
        if(null == handlerMethod){
            return null;
        }
        //需要登录
        RequiresUser methodRequiresUser = handlerMethod.getMethodAnnotation(RequiresUser.class);
        if (methodRequiresUser != null && !methodRequiresUser.required()) {
            return true;
        }

        if(methodRequiresUser == null){//类注解处理
            RequiresUser classRequiresUser =  this.getAnnotation(handlerMethod.getBean().getClass(),RequiresUser.class);
            if (classRequiresUser != null && !classRequiresUser.required()) {
                return true;
            }
        }

        //角色注解
        RequiresRoles requiresRoles = handlerMethod.getMethodAnnotation(RequiresRoles.class);
        if(requiresRoles == null){
            requiresRoles = this.getAnnotation(handlerMethod.getBean().getClass(),RequiresRoles.class);
        }
        if (requiresRoles != null) {//方法注解处理
            String[] roles = requiresRoles.value();
            boolean permittedRole = false;
            for (String role : roles) {
                permittedRole = SecurityUtils.isPermittedRole(role);
                if (Logical.AND.equals(requiresRoles.logical())) {
                    if (!permittedRole) {
                        notPermittedRole(request,response,sessionInfo,requestUrl,role);
                        return false;
                    }
                } else {
                    if (permittedRole) {
                        break;
                    }
                }
            }
            if(!permittedRole){
                notPermittedPermission(request,response,sessionInfo,requestUrl,null);
                return false;
            }
        }

        //资源/权限注解
        RequiresPermissions requiresPermissions = handlerMethod.getMethodAnnotation(RequiresPermissions.class);
        if(requiresPermissions == null){
            requiresPermissions = this.getAnnotation(handlerMethod.getBean().getClass(),RequiresPermissions.class);
        }
        if (requiresPermissions != null) {//方法注解处理
            String[] permissions = requiresPermissions.value();
            boolean permittedResource = false;
            for (String permission : permissions) {
                permittedResource = SecurityUtils.isPermitted(permission);
                if (Logical.AND.equals(requiresPermissions.logical())) {
                    if (!permittedResource) {
                        notPermittedPermission(request,response,sessionInfo,requestUrl,permission);
                        return false;
                    }
                } else {
                    if (permittedResource) {
                        break;
                    }
                }
            }
            if(!permittedResource){
                notPermittedPermission(request,response,sessionInfo,requestUrl,null);
                return false;
            }
        }
        return null;
    }

    /**
     * 未授权资源权限
     * @param request
     * @param response
     * @param sessionInfo
     * @param requestUrl
     * @param permission
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    private void notPermittedPermission(HttpServletRequest request,HttpServletResponse response,
                                        SessionInfo sessionInfo,String requestUrl,String permission) throws ServletException, IOException {
        String loginName = null;
        if(sessionInfo != null){
            loginName = sessionInfo.getLoginName();
            logger.warn("用户[{},{}]无权访问URL:{}，未被授权资源:{}", new Object[]{loginName,SpringMVCHolder.getIp(), requestUrl,permission});
        }
        request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_PAGE).forward(request, response);
    }


    /**
     * 未授权资源权限
     * @param request
     * @param response
     * @param sessionInfo
     * @param requestUrl
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    private void notPermitted(HttpServletRequest request,HttpServletResponse response,
                              SessionInfo sessionInfo,String requestUrl) throws ServletException, IOException {
        String loginName = null;
        if(sessionInfo != null){
            loginName = sessionInfo.getLoginName();
            logger.warn("用户{}未被授权URL:{}！", new Object[]{loginName, requestUrl});
        }
        request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_PAGE).forward(request, response);
    }

    /**
     * 数据库权限处理
     * @param request
     * @param response
     * @param handler
     * @param sessionInfo
     * @param requestUrl
     * @return
     * @throws Exception
     */
    private Boolean dbHandler(HttpServletRequest request, HttpServletResponse response, Object handler,
                              SessionInfo sessionInfo,String requestUrl) throws Exception {
        // 不拦截的URL
        if (Collections3.isNotEmpty(excludeUrls)) {
            for(String excludeUrl:excludeUrls){
                boolean flag = StringUtils.simpleWildcardMatch(request.getContextPath()+excludeUrl, requestUrl);
                if(flag){
                    return true;
                }
            }
        }

        if(sessionInfo != null){
            //清空session中清空未被授权的访问地址
//            Object unAuthorityUrl = request.getSession().getAttribute(SecurityConstants.SESSION_UNAUTHORITY_URL);
//            if(unAuthorityUrl != null){
//                request.getSession().setAttribute(SecurityConstants.SESSION_UNAUTHORITY_URL,null);
//            }

            //检查用户是否授权该URL
            if (authorMarkUrl){
                String url = StringUtils.replaceOnce(requestUrl, request.getContextPath(), "");
                boolean isAuthority = SecurityUtils.isPermittedUrl(url);
                if(!isAuthority){
                    notPermitted(request,response,sessionInfo,requestUrl);
                    return false; //返回到登录页面
                }

            }

            return true;
        }else{
            logger.debug("[{},{}]未授权[{}]",new Object[]{SpringMVCHolder.getIp(),request.getSession().getId(),requestUrl});
            //返回校验不通过页面
            try {
                if(!response.isCommitted()){
                    String authorization = request.getHeader("Authorization");
                    if(StringUtils.isBlank(authorization)){
                        authorization = SpringMVCHolder.getRequest().getParameter("Authorization");
                    }
                    if(WebUtils.isAjaxRequest(request) || StringUtils.startsWith(authorization,"Bearer ")){
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        WebUtils.renderJson(response, new Result().setCode(401).setMsg("未授权或会话信息已失效"));
                    }else{
                        //返回校验不通过页面
                        response.sendRedirect(request.getContextPath()+redirectURL);
                    }

                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return false; // 返回到登录页面
        }
    }

    /**
     * 未授权角色
     * @param request
     * @param response
     * @param sessionInfo
     * @param requestUrl
     * @param role
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    private void notPermittedRole(HttpServletRequest request,HttpServletResponse response,
                                  SessionInfo sessionInfo,String requestUrl,String role) throws ServletException, IOException {
        if(sessionInfo != null){
            String loginName = sessionInfo.getLoginName();
            logger.warn("用户[{},{}]无权访问URL:{}，未被授权角色:{}", new Object[]{loginName,SpringMVCHolder.getIp(), requestUrl,role});
        }
        request.getRequestDispatcher(SecurityConstants.SESSION_UNAUTHORITY_PAGE).forward(request, response);
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
        if(e != null){

        }
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public AuthorityInterceptor setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
        return this;
    }

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public AuthorityInterceptor setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
        return this;
    }

    public AuthorityInterceptor addExcludeUrl(String excludeUrl) {
        this.excludeUrls.add(excludeUrl);
        return this;
    }

    public Boolean getAuthorMarkUrl() {
        return authorMarkUrl;
    }

    public AuthorityInterceptor setAuthorMarkUrl(Boolean authorMarkUrl) {
        this.authorMarkUrl = authorMarkUrl;
        return this;
    }
}
