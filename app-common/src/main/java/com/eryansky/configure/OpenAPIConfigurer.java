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
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfigurer {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/public/**","/f/**")
                .build();
    }
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/a/**","/m/**","/rest/**","/api/**")
                .addOpenApiMethodFilter(method -> method.isAnnotationPresent(RequiresUser.class) || method.isAnnotationPresent(RequiresPermissions.class) || method.isAnnotationPresent(RequiresRoles.class))
                .build();
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title(AppConstants.getAppFullName())
                        .description("")
                        .version("V"+AppConstants.getAppVersion())
                        .license(new License().name("Apache 2.0").url(AppConstants.getAppProductURL())))
                .externalDocs(new ExternalDocumentation()
                        .description("API接口文档")
                        .url(AppUtils.getAppURL()+"/v3/api-docs"));
    }

}