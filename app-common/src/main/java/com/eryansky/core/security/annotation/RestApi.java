/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 */
package com.eryansky.core.security.annotation;

import com.eryansky.core.security._enum.Logical;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.eryansky.core.security._enum.Logical.AND;

/**
 * 需要的REST权限
 * @author Eryan
 * @date : 2020-09-09
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestApi {

    /**
     * 用户是否需要授权访问 是：true 否：false 默认值：true
     * @return
     */
    boolean required() default true;

    String[] value() default {};

    Logical logical() default AND;

}