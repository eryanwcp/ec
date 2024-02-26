package com.eryansky.configure.web;

import com.eryansky.common.web.servlet.ValidateCodeServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Servlet注册 配置
 *
 * @author Eryan
 * @date : 2019-01-23
 */
@Configuration
public class ServletConfiguration {

    /**
     * 验证码
     * @return
     */
    @Bean
    public ServletRegistrationBean<ValidateCodeServlet> getValidateCodeServlet() {
        ValidateCodeServlet servlet = new ValidateCodeServlet();
        ServletRegistrationBean<ValidateCodeServlet> bean = new ServletRegistrationBean<>(servlet);
        bean.addUrlMappings("/servlet/ValidateCodeServlet");
        return bean;
    }


}
