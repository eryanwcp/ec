package com.eryansky.core.security.interceptor;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * URL限制请求拦截器
 **/
public class UrlLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UrlLimitInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        if (!AppConstants.isLimitUrlEnable()) {
            return true;
        }
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (null == sessionInfo) {//自动跳过
            return true;
        }
        String url = StringUtils.replaceOnce(request.getRequestURI().replaceAll("//", "/"), request.getContextPath(), "");
        if (urlIsLock(sessionInfo.getUserId(), url)) {
            log.warn("禁止访问：{} {} {}", sessionInfo.getLoginName(), url, UserAgentUtils.getHTTPUserAgent(request));
            Result result = Result.noPermissionResult().setMsg("禁止访问：" + sessionInfo.getLoginName() + "," + url);
            WebUtils.renderJson(response, result);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    /**
     * 判断URL是否被禁用
     *
     * @param userId
     * @param url
     */
    private Boolean urlIsLock(String userId, String url) {
        //黑名单 缓存
        Set<String> urls = SecurityUtils.getUrlLimitByUserId(userId);
        return Collections3.isNotEmpty(urls) && urls.contains(url);
    }


}