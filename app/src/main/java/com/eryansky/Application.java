package com.eryansky;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@SpringBootApplication(
        scanBasePackages = {"com.eryansky.j2cache.autoconfigure",
                "com.eryansky.common.spring",
                "com.eryansky.configure",
                "com.eryansky.modules.**.aop",
                "com.eryansky.modules.**.task",
                "com.eryansky.modules.**.event",
                "com.eryansky.modules.**.service",
                "com.eryansky.modules.**.quartz",
                "com.eryansky.modules.**.web"
        },
        exclude = {MybatisAutoConfiguration.class,
                FreeMarkerAutoConfiguration.class,
//                DataSourceAutoConfiguration.class,
//                DruidDataSourceAutoConfigure.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                LiquibaseAutoConfiguration.class
        })
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
    }
}
