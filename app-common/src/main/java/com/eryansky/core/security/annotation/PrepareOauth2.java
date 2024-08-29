/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Oauth2注解
 * @author Eryan
 * @date : 2021-11-04
 */
@Target({ElementType.TYPE, ElementType.METHOD,ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrepareOauth2 {

    String DEFAULT_AUTH_TYPE = "user";
    /**
     * 是否启用 是：true 否：false 默认值：true
     * @return
     */
    boolean enable() default true;

    /**
     * 认证用户类型
     * @return
     */
    String authType() default DEFAULT_AUTH_TYPE;

}