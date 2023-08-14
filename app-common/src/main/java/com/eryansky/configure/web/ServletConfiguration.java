package com.eryansky.configure.web;

import com.eryansky.core.cms.CKFinderConnectorServlet;
import com.eryansky.core.cms.CKFinderFilesServlet;
import com.eryansky.core.web.servlet.DownloadChartServlet;
import com.eryansky.core.web.servlet.StaticContentServlet;
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

    /**
     * 本地静态内容展示与下载的Servlet 可能存在安全问题 不推荐使用
     * @return
     */
//    @Bean
//    public ServletRegistrationBean<StaticContentServlet> getStaticContentServlet() {
//        StaticContentServlet servlet = new StaticContentServlet();
//        ServletRegistrationBean<StaticContentServlet> bean = new ServletRegistrationBean<>(servlet);
//        bean.addUrlMappings("/servlet/StaticContentServlet");
//        bean.addInitParameter("cacheChannel","cacheChannel");
//        bean.addInitParameter("cacheKey","contentInfoCache");
//        bean.addInitParameter("cacheFileData","true");
//        return bean;
//    }


    /**
     * HightChart download
     * @return
     */
    @Bean
    public ServletRegistrationBean<DownloadChartServlet> getDownloadChartServlet() {
        DownloadChartServlet servlet = new DownloadChartServlet();
        ServletRegistrationBean<DownloadChartServlet> bean = new ServletRegistrationBean<>(servlet);
        bean.addUrlMappings("/servlet/DownloadChartServlet");
        return bean;
    }


    /**
     * CKFinder Connector
     * @return
     */
    @Bean
    public ServletRegistrationBean<CKFinderConnectorServlet> getCKFinderConnectorServlet() {
        CKFinderConnectorServlet servlet = new CKFinderConnectorServlet();
        ServletRegistrationBean<CKFinderConnectorServlet> bean = new ServletRegistrationBean<>(servlet);
        bean.addUrlMappings("/servlet/CKFinderConnectorServlet");
        bean.addInitParameter("XMLConfig","/WEB-INF/ckfinder.xml");
        bean.addInitParameter("configuration","com.eryansky.core.cms.CKFinderConfig");
        bean.addInitParameter("debug","true");
        return bean;
    }

    /**
     * CKFinder Files
     * @return
     */
    @Bean
    public ServletRegistrationBean<CKFinderFilesServlet> getCKFinderFilesServlet() {
        CKFinderFilesServlet servlet = new CKFinderFilesServlet();
        ServletRegistrationBean<CKFinderFilesServlet> bean = new ServletRegistrationBean<>(servlet);
        bean.addUrlMappings("/userfiles/*");
        return bean;
    }
}
