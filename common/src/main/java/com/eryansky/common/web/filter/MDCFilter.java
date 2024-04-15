/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.web.filter;

import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.net.IpUtils;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MDC日志自定义实现过滤器
 * 在日志上下文增加参数，在日志中可以打印该参数，如requestId，则日志配置：%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{requestId}] [%thread] %-5level %logger{36} - %msg%n
 *  * 则 requestId 会打印在中括号里，%X{参数名}  是格式
 *
 * @author Eryan
 * @date 2024-04-15
 */
public class MDCFilter extends BaseFilter {

    /**
     * 请求流水号
     */
    private static final String KEY_RID = "requestId";

    /**
     * 客户端IP
     */
    private static final String KEY_IP = "ip";


    /**
     * 用户身份
     */
    private static final String KEY_UID = "userPrincipalId";
    /**
     * 租户ID
     */
    private static final String KEY_TID = "tenantId";


    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String requestId = request.getHeader("RequestId");
            if (requestId == null) {
                requestId = request.getParameter(KEY_RID);
            }
            if (requestId == null) {
                requestId = Identities.uuid2();
            }
            MDC.put(KEY_RID, requestId);
            response.setHeader("RequestId", requestId);

            // 还有更加准确的方法
            String ip = IpUtils.getIpAddr0(request);
            MDC.put(KEY_IP, ip);

            String userPrincipalId = request.getHeader("UserPrincipalId");
            if (userPrincipalId == null) {
                userPrincipalId = request.getParameter(KEY_UID);
            }
            MDC.put(KEY_UID, userPrincipalId);


            String tenantId = request.getHeader("TenantId");
            if (tenantId == null) {
                tenantId = request.getParameter(KEY_TID);
            }
            MDC.put(KEY_TID, tenantId);

            chain.doFilter(request, response);
        } finally {
            MDC.remove(KEY_RID);
            MDC.remove(KEY_IP);
            MDC.remove(KEY_UID);
            MDC.remove(KEY_TID);
            MDC.clear();
        }
    }
}