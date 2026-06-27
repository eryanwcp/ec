/**
 * Copyright (c) 2012-2026 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.configure;

import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.core.security.annotation.RequiresRoles;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * OpenAPI (Swagger v3) 接口文档配置
 *
 * @author Eryan
 * @date 2024-03-01
 */
@Configuration
public class OpenAPIConfigurer {

    // 定义管理端接口需要识别的安全注解列表，便于后续扩展
    private static final List<Class<? extends Annotation>> SECURITY_ANNOTATIONS = List.of(
            RequiresUser.class,
            RequiresPermissions.class,
            RequiresRoles.class
    );

    /**
     * 公开接口分组（无需登录/前端开放接口）
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/public/**", "/f/**")
                .build();
    }

    /**
     * 管理端接口分组（包含权限控制的接口）
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/a/**", "/m/**", "/rest/**", "/api/**")
                // 优化点：利用 Stream 匹配，使安全注解的扩展更加优雅，避免臃肿的 || 条件分支
                .addOpenApiMethodFilter(method -> SECURITY_ANNOTATIONS.stream().anyMatch(method::isAnnotationPresent))
                .build();
    }

    /**
     * OpenAPI 全局基础信息配置
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(AppConstants.getAppFullName())
                        .description(String.format("%s 平台系统接口文档（支持基于注解的权限接口隔离）", AppConstants.getAppFullName()))
                        .version("V" + AppConstants.getAppVersion())
                        .contact(new Contact().name("Eryan").url(AppConstants.getAppProductURL())) // 丰富文档所有者信息
                        .license(new License()
                                .name("Apache 2.0")
                                .url(AppConstants.getAppProductURL())
                        )
                )
                .externalDocs(new ExternalDocumentation()
                        .description("API接口文档（JSON规范）")
                        .url(AppUtils.getAppURL() + "/v3/api-docs")
                );
    }
}