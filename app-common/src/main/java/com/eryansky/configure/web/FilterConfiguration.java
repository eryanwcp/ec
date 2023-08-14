package com.eryansky.configure.web;

import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.web.filter.CustomHttpServletRequestFilter;
import com.eryansky.common.web.filter.XssFilter;
import com.eryansky.core.web.filter.MySiteMeshFilter;
import com.eryansky.core.web.interceptor.ExceptionInterceptor;
import com.eryansky.core.web.filter.ChinesePathFilter;
import com.eryansky.utils.AppConstants;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;
import java.util.List;


/**
 * Filter注册配置
 *
 * @author Eryan
 * @date : 2019-01-23
 */
@Configuration
public class FilterConfiguration {



    /**
     * SiteMesh自定义Filter
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean<MySiteMeshFilter> mySiteMeshFilterFilterRegistrationBean() {
        MySiteMeshFilter filter = new MySiteMeshFilter();
        FilterRegistrationBean<MySiteMeshFilter> bean = new FilterRegistrationBean<>(filter);
        bean.addInitParameter("blackListURL", "/static/**;/api/**;/rest/**");
        bean.addInitParameter("whiteListURL", "/**");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 300);
        return bean;
    }


    /**
     * 自定义URL拦截
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean<CustomHttpServletRequestFilter> customHttpServletRequestFilterFilterRegistrationBean() {
        CustomHttpServletRequestFilter filter = new CustomHttpServletRequestFilter();
        FilterRegistrationBean<CustomHttpServletRequestFilter> bean = new FilterRegistrationBean<>(filter);
        bean.addInitParameter("blackListURL", "/static/**;/api/**;/rest/**");
        bean.addInitParameter("whiteListURL", "/a/sys/proxy/**");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 200);
        return bean;
    }



    /**
     * 中文
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean<ChinesePathFilter> chinesePathFilterFilterRegistrationBean() {
        ChinesePathFilter filter = new ChinesePathFilter();
        FilterRegistrationBean<ChinesePathFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setFilter(filter);
        bean.addInitParameter("blackListURL", "/static/**");
        bean.addInitParameter("whiteListURL", "/**");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        return bean;
    }

    /**
     * XSS
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistrationBean() {
        XssFilter filter = new XssFilter();
        FilterRegistrationBean<XssFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setFilter(filter);
        bean.addInitParameter("blackListURL", "/static/**;/api/**;/druid/**");
        bean.addInitParameter("whiteListURL", "/**");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 80);
        return bean;
    }

    /**
     * 自定义异常处理
     *
     * @return
     */
    @Bean
    public ExceptionInterceptor exceptionInterceptor() {
        ExceptionInterceptor bean = new ExceptionInterceptor();
        return bean;
    }


    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        List<String> allowedOrigins = AppConstants.getCorsAllowedOriginList();
        corsConfiguration.setAllowedOriginPatterns(Collections3.isNotEmpty(allowedOrigins) ? allowedOrigins:Collections.singletonList(CorsConfiguration.ALL));
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfiguration.setAllowCredentials(true);
        return corsConfiguration;
    }


    /**
     * 跨域配置
     * @return
     */
    @DependsOn(value = {"springContextHolder"})
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        CorsFilter filter = new CorsFilter(source);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setFilter(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 25);
        return bean;
    }

}
