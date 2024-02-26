package com.eryansky.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义http body信息保存
 * <br>将该Filter配置在web.xml 在SpringMVC DispatcherServlet之上
 */
public class CustomHttpServletRequestFilter extends BaseFilter{


    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new CustomHttpServletRequestWrapper(request), response);
    }

}