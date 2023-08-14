/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 */
package com.eryansky.core.security.annotation;

import com.eryansky.core.security._enum.Logical;

import java.lang.annotation.*;

import static com.eryansky.core.security._enum.Logical.*;

/**
 * 需要的权限
 * @author Eryan
 * @date : 2014-06-11 20:06
 */
@Target({ElementType.TYPE, ElementType.METHOD,ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {

    String[] value();

    Logical logical() default AND;

}