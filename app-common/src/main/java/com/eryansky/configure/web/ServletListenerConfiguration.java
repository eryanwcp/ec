package com.eryansky.configure.web;

import com.eryansky.listener.SystemInitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.util.IntrospectorCleanupListener;


/**
 * Servlet注册 配置
 *
 * @author Eryan
 * @date : 2019-01-23
 */
@Configuration
public class ServletListenerConfiguration {


    @Bean
    public ServletListenerRegistrationBean<IntrospectorCleanupListener> getIntrospectorCleanupListener() {
        return new ServletListenerRegistrationBean<>(new IntrospectorCleanupListener());
    }

    @Bean
    public ServletListenerRegistrationBean<RequestContextListener> getRequestContextListener() {
        return new ServletListenerRegistrationBean<>(new RequestContextListener());
    }

    /**
     * 系统启动、关闭监听
     * @return
     */
    @DependsOn(value = {"springContextHolder"})
    @Bean("systemInitListener")
    @ConditionalOnMissingBean(name = "systemInitListener")
    public ServletListenerRegistrationBean<SystemInitListener> getSystemInitListener() {
        return new ServletListenerRegistrationBean<>(new SystemInitListener());
    }

}
