package com.eryansky.core.web.filter;


import com.eryansky.common.web.filter.BaseFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Eryan
 * @date 2019-12-11
 */
public class ChinesePathFilter extends BaseFilter {

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(request.getScheme()).append("://").append(request.getServerName()).append(":")
                .append(request.getServerPort()).append((request).getContextPath()).append("/");
        request.setAttribute("baseUrl", pathBuilder.toString());
        request.setAttribute("ctx", (request).getContextPath());
        chain.doFilter(request, response);
    }
}
