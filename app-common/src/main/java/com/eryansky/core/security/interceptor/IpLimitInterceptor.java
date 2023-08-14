package com.eryansky.core.security.interceptor;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * IP限制请求拦截器
 **/
public class IpLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IpLimitInterceptor.class);

    public static final String LOCK_IP_LIMIT_REGION = "lock_ip_limit";
    public static final String LOCK_IP_LIMIT_WHITELIST_REGION = "lock_ip_limit_whitelist";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        if(!AppConstants.isLimitIpEnable()){
            return true;
        }
        String ip = IpUtils.getIpAddr(request);
        log.debug("请求IP={},URL={}", ip,request.getRequestURL());
        if (ipIsLock(ip)) {
            log.warn("禁止访问：{} {} {}", ip,request.getRequestURL(), UserAgentUtils.getHTTPUserAgent(request));
            Result result = Result.noPermissionResult().setMsg("禁止访问：" + ip);
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
     * 判断ip是否被禁用
     *
     * @param ip
     */
    private Boolean ipIsLock(String ip) {
        //默认白名单 本机不限制
        if("127.0.0.1".equals(ip) || "localhost".equals(ip)){
            return false;
        }
        //白名单 缓存
        Collection<String> whiteList = CacheUtils.keys(LOCK_IP_LIMIT_WHITELIST_REGION);
        if (null != whiteList && null != whiteList.stream().filter(v-> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v,ip)).findAny().orElse(null)) {
            return false;
        }
        //白名单 配置文件
        Collection<String> configWhiteList = AppConstants.getLimitIpWhiteList();
        if (null != configWhiteList && null != configWhiteList.stream().filter(v-> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v,ip)).findAny().orElse(null)) {
            return false;
        }
        if(AppConstants.isLimitIpWhiteEnable()){
            return true;
        }else{
            //黑名单 缓存
            Boolean value = CacheUtils.get(LOCK_IP_LIMIT_REGION, ip);
            if(null != value){
                return  true;
            }

            //黑名单 配置文件
            Collection<String> blackWhiteList = AppConstants.getLimitIpBlackList();
            if (null != blackWhiteList && null != blackWhiteList.stream().filter(v->com.eryansky.j2cache.util.IpUtils.checkIPMatching(v,ip)).findAny().orElse(null)) {
                return true;
            }
            return false;
        }


    }


}