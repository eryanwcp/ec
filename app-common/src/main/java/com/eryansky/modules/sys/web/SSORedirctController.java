/**
 *  Copyright (c) 2012-2018 http://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 */
package com.eryansky.modules.sys.web;


import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.interceptor.AuthorityInterceptor;
import com.eryansky.utils.AppUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 内部应用单点跳转
 * @author Eryan
 * @date : 2021-11-03
 */
@Controller
@RequestMapping(value = "${adminPath}/sso")
public class SSORedirctController extends SimpleController {

    /**
     * SSO跳转
     *
     * @param request
     * @throws IOException
     */
    @GetMapping(value = {"**"})
    public String redirect(HttpServletRequest request) throws Exception {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String requestUrl = request.getRequestURI();
        String param = AppUtils.joinParasWithEncodedValue(WebUtils.getParametersStartingWith(request, null));//请求参数
        String url = StringUtils.substringAfterLast(requestUrl,"/a/sso/");
        if (StringUtils.isNotBlank(param)) {
            url += "?" + param;
        }
        url += (StringUtils.containsAny(url, "?") ? "&" : "?") + AuthorityInterceptor.ATTR_AUTHORIZATION + "=" + sessionInfo.getRefreshToken();
        return "redirect:"+ url;
    }


}
