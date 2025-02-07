package com.eryansky;

import com.eryansky.core.rpc.EnableRPCClients;
import com.eryansky.core.rpc.EnableRPCServer;
import com.eryansky.encrypt.anotation.EnableEncrypt;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@EnableEncrypt
@EnableRPCServer
@EnableRPCClients(basePackages = {"com.eryansky.server"})
@SpringBootApplication(
        scanBasePackages = {"com.eryansky.j2cache.autoconfigure",
                "com.eryansky.server",
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
//                RedisAutoConfiguration.class,
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

