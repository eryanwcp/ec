/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.eryansky.configure;

import com.eryansky.core.ureport.DataSourceContext;
import com.eryansky.core.ureport.LocationRegisterBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import com.bstek.ureport.console.UReportServlet;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 集成ureport2 报表功能
 * @author huanzhou
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "ec.ureport", name = "enabled", havingValue="true")
@ImportResource("classpath:ureport-console-context.xml")
public class UReport2Configurer {

	/**
	 * 配置报表
	 * @author ZhouZhou
	 * @return
	 */
    @Bean
    public ServletRegistrationBean<UReportServlet> initUReport() {
        return new ServletRegistrationBean<>(new UReportServlet(), "/ureport/*");
    }

	@Primary
	@Bean
	public LocationRegisterBean locationRegisterBean(@Qualifier("dataSource") DataSource dataSource) {
		DataSourceContext.addDefaultDataSource(dataSource);
		LocationRegisterBean bean = new LocationRegisterBean();
		return bean;
	}

}