package com.eryansky.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @date 2019-12-11
 */
public class ChinesePathFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(request.getScheme()).append("://").append(request.getServerName()).append(":")
                .append(request.getServerPort()).append(((HttpServletRequest) request).getContextPath()).append("/");
        request.setAttribute("baseUrl", pathBuilder.toString());
        request.setAttribute("ctx", ((HttpServletRequest) request).getContextPath());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
