/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.web.filter;

import com.eryansky.common.utils.Identities;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MDC日志自定义实现过滤器
 * @author Eryan
 * @date 2022-10-10
 */
public class LogMdcFilter extends BaseFilter {

    // TID 链路追踪标识
    private static final String TID = "tid";
    // UID 操作用户标识
    private static final String UID = "uid";


    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String token = request.getHeader("token");
            MDC.put(TID, Identities.uuid2());
            MDC.put(UID, token);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TID);
            MDC.remove(UID);
        }
    }
}